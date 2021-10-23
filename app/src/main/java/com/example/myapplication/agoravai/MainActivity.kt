package com.example.myapplication.agoravai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    val usbManager by lazy { getSystemService(USB_SERVICE) as UsbManager }

    val rfidReader by lazy { RfidReader(this, usbManager) }
    protected val scope = CoroutineScope(Dispatchers.Main)

    val editText by lazy { findViewById<EditText>(R.id.editTextTextPersonName) }
    val textView by lazy { findViewById<TextView>(R.id.textView) }
    val btnRead by lazy { findViewById<TextView>(R.id.btnRead) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED) // will intercept by system

        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(RfidReader.ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        rfidReader.onConnectionChange {
            textView.text = if (it) "Connected" else "Disconnected"
        }

        btnRead.setOnClickListener {
            scope.launch {
                readTag()
            }
        }

    }

    suspend fun readTag() {
        val tags = rfidReader.readTag()
        Log.d("AA", tags.size.toString())
        if (tags.isNotEmpty()) editText.setText(tags.first())
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