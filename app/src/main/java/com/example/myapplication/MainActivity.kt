package com.example.myapplication

import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.*
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import org.apache.http.util.EncodingUtils
import java.lang.Thread.sleep
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        val DEBUG = false
        val ACTION_USB_PERMISSION = "com.mti.rfid.minime.USB_PERMISSION"
        val PID = 49193
        val VID = 4901
    }

    private var mMtiCmd: MtiCmd? = null

    private val mUsbCommunication = UsbCommunication.newInstance()
    private var mManager: UsbManager? = null
    private val bSavedInst = false
    private var mPermissionIntent: PendingIntent? = null

    val textView by lazy { findViewById<TextView>(R.id.textView1) }

    private var mSharedpref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mManager = getSystemService(USB_SERVICE) as UsbManager

        mSharedpref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        mPermissionIntent =
            PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED) // will intercept by system

        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        val deviceList = mManager!!.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()
            if (device.productId == PID && device.vendorId == VID) {
                if (!mManager!!.hasPermission(device)) {
                    mManager!!.requestPermission(
                        device,
                        mPermissionIntent
                    )
                } else {
                    mUsbCommunication.setUsbInterface(mManager, device)
                    setUsbState(true)
                }
                break
            }
        }
    }

    // #### run inventory ####
    fun onInventoryClick() {
        val handler = Handler()
        val scantimes = mSharedpref!!.getString("cfg_inventory_times", "25")!!.toInt()
        if (mUsbState) {
            val mProgDlg = ProgressDialog.show(this, "Inventory", "Searching ...", true)
            object : Thread() {
                var numTags: Byte = 0
                var tagId: String? = null
                var tg = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
                override fun run() {
                    FRAG_Tag.tagList.clear()
                    for (i in 0 until scantimes) {
                        mMtiCmd = CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory(mUsbCommunication)
                        val finalCmd: CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory =
                            mMtiCmd as CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory
                        if (finalCmd.setCmd(CMD_Iso18k6cTagAccess.Action.StartInventory)) {
                            tagId = finalCmd.tagId
                            if (finalCmd.tagNumber > 0) {
                                tg.startTone(ToneGenerator.TONE_PROP_BEEP)
                                if (!FRAG_Tag.tagList.contains(tagId)) FRAG_Tag.tagList.add(tagId)
                                //								finalCmd.setCmd(CMD_Iso18k6cTagAccess.Action.GetAllTags);
                            }
                            numTags = finalCmd.tagNumber
                            while (numTags > 1) {
                                if (finalCmd.setCmd(CMD_Iso18k6cTagAccess.Action.NextTag)) {
                                    tagId = finalCmd.tagId
                                    if (!FRAG_Tag.tagList.contains(tagId)) {
                                        FRAG_Tag.tagList.add(tagId)
                                    }
                                }
                                numTags--
                            }
                            Collections.sort(FRAG_Tag.tagList)

                        } else {
                            // #### process error ####
                        }
                    }
                    mProgDlg.dismiss()
                    setPowerState()
                }

            }.start()
        } else Toast.makeText(this, "The Reader is not connected", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (textView.text == "Connected") mUsbCommunication.setUsbInterface(null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                // Process the messages array..
            }
        }
    }

    var mUsbState: Boolean = false

    private fun setUsbState(state: Boolean) {
        mUsbState = state
        if (state) {

            textView.setText("Connected")
            textView.setTextColor(Color.GREEN)
            onInventoryClick()
        } else {
            textView.setText("Disconnected")
            textView.setTextColor(Color.RED)
        }
    }

    private fun setPowerLevel() {
        val mMtiCmd: MtiCmd = CMD_AntPortOp.RFID_AntennaPortSetPowerLevel(mUsbCommunication)
        val finalCmd: CMD_AntPortOp.RFID_AntennaPortSetPowerLevel =
            mMtiCmd as CMD_AntPortOp.RFID_AntennaPortSetPowerLevel
        finalCmd.setCmd(18.toByte())
    }

    private fun setPowerState() {
        val mMtiCmd: MtiCmd = CMD_PwrMgt.RFID_PowerEnterPowerState(mUsbCommunication)
        val finalCmd: CMD_PwrMgt.RFID_PowerEnterPowerState =
            mMtiCmd as CMD_PwrMgt.RFID_PowerEnterPowerState
        if (mSharedpref?.getBoolean("cfg_sleep_mode", false) == true) {
            finalCmd.setCmd(CMD_PwrMgt.PowerState.Sleep)
            sleep(200)
        }
    }

    private fun getReaderSn(bState: Boolean) {
        var mMtiCmd: MtiCmd
        if (bState) {
            val bSN = ByteArray(16)
            for (i in -1..15) {
                mMtiCmd = CMD_FwAccess.RFID_MacReadOemData(mUsbCommunication)
                val finalCmd: CMD_FwAccess.RFID_MacReadOemData =
                    mMtiCmd as CMD_FwAccess.RFID_MacReadOemData
                if (finalCmd.setCmd(i + 0x50)) if (i >= 0) bSN[i] = finalCmd.getData()
            }
            mSharedpref!!.edit().putString("about_reader_sn_sum", EncodingUtils.getAsciiString(bSN))
                .commit()
        } else mSharedpref!!.edit().putString("about_reader_sn_sum", "n/a").commit()
    }

    // #### broadcast receiver ####
    var usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (DEBUG) Toast.makeText(
                context,
                "Broadcast Receiver",
                Toast.LENGTH_SHORT
            ).show()
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {                    // will intercept by system
                if (DEBUG) Toast.makeText(context, "USB Attached", Toast.LENGTH_SHORT)
                    .show()
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                mUsbCommunication.setUsbInterface(mManager, device)
                setUsbState(true)
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                if (DEBUG) Toast.makeText(context, "USB Detached", Toast.LENGTH_SHORT)
                    .show()
                mUsbCommunication.setUsbInterface(null, null)
                setUsbState(false)
                //				getReaderSn(false);
            } else if (ACTION_USB_PERMISSION == action) {
                if (DEBUG) Toast.makeText(
                    context,
                    "USB Permission",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(UsbCommunication.TAG, "permission")
                synchronized(this) {
                    val device =
                        intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        mUsbCommunication.setUsbInterface(mManager, device)
                        setUsbState(true)
                        if (bSavedInst) getReaderSn(true)
                        setPowerLevel()
                        setPowerState()
                    } else {
                        finish()
                    }
                }
            }
        }
    }
}