package com.example.myapplication.agoravai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class MainActivity : AppCompatActivity() {

    val usbManager by lazy { getSystemService(USB_SERVICE) as UsbManager }

    val rfidReader by lazy { RfidReader(this, usbManager) }

    val textView by lazy { findViewById<TextView>(R.id.textView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED) // will intercept by system

        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(MainActivity.ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        rfidReader.onConnectionChange {
            textView.text = if (it) "Connected" else "Disconnected"
        }
    }

    override fun onResume() {
        super.onResume()
        rfidReader.handleResume()
    }

    override fun onPause() {
        super.onPause()
        rfidReader.handlePause()
    }

    var usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            rfidReader.handleReceive(context, intent)
        }
    }
}