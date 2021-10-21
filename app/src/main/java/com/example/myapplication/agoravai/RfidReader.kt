package com.example.myapplication.agoravai

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.example.myapplication.*
import com.example.myapplication.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RfidReader(val context: Context, val usbManager: UsbManager) {
    companion object {
        val DEBUG = false
        val ACTION_USB_PERMISSION = "com.mti.rfid.minime.USB_PERMISSION"
        val PID = 49193
        val VID = 4901
    }

    var connectionChangeListener: (Boolean) -> Unit = { _ -> }
    val usbCommunication = UsbCommunication.newInstance()
    var isConnected = false
        private set(value) {
            connectionChangeListener?.invoke(value)
            field = value
        }

    val permissionIntent =
        PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), 0)


    private fun setPowerLevel() {

        val mMtiCmd: MtiCmd = CMD_AntPortOp.RFID_AntennaPortSetPowerLevel(usbCommunication)
        val finalCmd: CMD_AntPortOp.RFID_AntennaPortSetPowerLevel =
            mMtiCmd as CMD_AntPortOp.RFID_AntennaPortSetPowerLevel
        finalCmd.setCmd(18.toByte())
    }

    private fun setPowerState() {
//        val mMtiCmd: MtiCmd = CMD_PwrMgt.RFID_PowerEnterPowerState(mUsbCommunication)
//        val finalCmd: CMD_PwrMgt.RFID_PowerEnterPowerState =
//            mMtiCmd as CMD_PwrMgt.RFID_PowerEnterPowerState
//
    }

    fun onConnectionChange(listener: (Boolean) -> Unit) {
        connectionChangeListener = listener
    }

    fun handleResume() {
        val deviceList = usbManager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()
            if (device.productId == MainActivity.PID && device.vendorId == MainActivity.VID) {
                if (!usbManager.hasPermission(device)) {
                    usbManager.requestPermission(
                        device,
                        permissionIntent
                    )
                } else {
                    usbCommunication.setUsbInterface(usbManager, device)
                    isConnected = true
                }
                break
            }
        }
    }

    fun handleReceive(context: Context, intent: Intent): Boolean {
        val action = intent.action
        var result = true

        if (MainActivity.DEBUG) Toast.makeText(
            context,
            "Broadcast Receiver",
            Toast.LENGTH_SHORT
        ).show()
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {                    // will intercept by system
            if (MainActivity.DEBUG) Toast.makeText(context, "USB Attached", Toast.LENGTH_SHORT)
                .show()
            val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            usbCommunication.setUsbInterface(usbManager, device)
            isConnected = true
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
            if (MainActivity.DEBUG) Toast.makeText(context, "USB Detached", Toast.LENGTH_SHORT)
                .show()
            usbCommunication.setUsbInterface(null, null)
            isConnected = false
            //				getReaderSn(false);
        } else if (MainActivity.ACTION_USB_PERMISSION == action) {
            if (MainActivity.DEBUG) Toast.makeText(
                context,
                "USB Permission",
                Toast.LENGTH_SHORT
            ).show()
            Log.d(UsbCommunication.TAG, "permission")


            runBlocking {
                launch(Dispatchers.Main) {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        usbCommunication.setUsbInterface(usbManager, device)
                        isConnected = true

                        setPowerLevel()
                        setPowerState()
                    } else {
                        result = false
                    }

                }
            }

        }
        return result
    }

    fun handlePause() {
        if (isConnected) {
            usbCommunication.setUsbInterface(null, null)
            isConnected = false
        }
    }


}