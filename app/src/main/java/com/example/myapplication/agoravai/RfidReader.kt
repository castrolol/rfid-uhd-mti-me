package com.example.myapplication.agoravai

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.myapplication.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class RfidReader(val context: Context, val usbManager: UsbManager) {
    companion object {
        val TAG = RfidReader.javaClass.name
        val DEBUG = true
        val ACTION_USB_PERMISSION = "com.mti.rfid.minime.USB_PERMISSION"
        val PID = 49193
        val VID = 4901
    }

    protected val scope = CoroutineScope(Dispatchers.IO)

    var connectionChangeListener: (Boolean) -> Unit = { _ -> }
    val usbCommunication = UsbCommunication.newInstance()
    var isConnected = false
        private set(value) {
            connectionChangeListener?.invoke(value)
            field = value
        }

    val permissionIntent =
        PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)

   fun readTag(tries: Int = 10) = scope.run {

        val tags = mutableListOf<String>()
        var numTags: Byte = 0
        val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        var hasError = false;
        for (i in 0..tries) {

            val command = CommandIso18k6cTagAccess.RFID18K6CTagInventory(usbCommunication)
            if (command.setCmd(CommandIso18k6cTagAccess.Action.StartInventory)) {
                if (command.tagNumber > 0) {
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP)
                    tags.add(command.tagId)
                }
                numTags = command.tagNumber
                while (numTags > 1) {
                    if (command.setCmd(CommandIso18k6cTagAccess.Action.NextTag)) {
                        tags.add(command.tagId)
                    }
                    numTags--
                }
                if (tags.isNotEmpty()) break;
            } else {

                // #### process error ####
                hasError = true;
                Log.d(TAG, "Process on Command reading tag")
            }
        }

        if (tags.isEmpty() && hasError) tg.startTone(ToneGenerator.TONE_PROP_NACK)

        return@run runCatching {
            return@runCatching tags.map { convertEPC(it) }
        }.getOrElse { listOf() }

    }


    private fun setPowerLevel() {
        CommandAntenaPortOperation
            .RFIDAntennaPortSetPowerLevel(usbCommunication)
            .setCmd(18.toByte())
    }

    private fun setPowerState() {
        val cmd = CommandPowerManagement.RFIDPowerEnterPowerState(usbCommunication)
        cmd.setCmd(CommandPowerManagement.PowerState.Ready)
    }

    fun onConnectionChange(listener: (Boolean) -> Unit) {
        connectionChangeListener = listener
    }

    fun handleResume() {
        Log.d("RfidReader", "handleResume():")

        val deviceList = usbManager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        Log.d("RfidReader", "deviceList: ${deviceList.size} devices")
        if (deviceList.size <= 0) {
            notifyConnected()
            return;
        }
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()
            Log.d(
                "RfidReader",
                "reading device product=${device.productId} vendor=${device.vendorId}"
            )
            if (device.productId == PID && device.vendorId == VID) {
                if (!usbManager.hasPermission(device)) {
                    Log.d(
                        "RfidReader",
                        "without permission"
                    )
                    usbManager.requestPermission(
                        device,
                        permissionIntent
                    )
                } else {
                    Log.d(
                        "RfidReader",
                        "with permission"
                    )
                    usbCommunication?.setUsbInterface(usbManager, device)
                    isConnected = true
                }
                break
            }
        }
    }

    private fun notifyConnected() {
        connectionChangeListener?.invoke(isConnected)
    }

    fun handleReceive(context: Context, intent: Intent): Boolean {
        val action = intent.action
        var result = true


        when {
            UsbManager.ACTION_USB_DEVICE_ATTACHED == action -> {                    // will intercept by system
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                usbCommunication?.setUsbInterface(usbManager, device)
                isConnected = true
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED == action -> {

                usbCommunication?.setUsbInterface(null, null)
                isConnected = false
                //				getReaderSn(false);
            }
            ACTION_USB_PERMISSION == action -> {

                Log.d(UsbCommunication.TAG, "permission")

                scope.launch(Dispatchers.Main) {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        usbCommunication?.setUsbInterface(usbManager, device)
                        isConnected = true

                        setPowerLevel()
                        setPowerState()
                    } else {
                        runCatching {
                            result = false
                        }

                    }
                }

            }
        }
        return result
    }

    fun handlePause() {
        if (isConnected) {
            usbCommunication?.setUsbInterface(null, null)
            isConnected = false
        }
    }


}