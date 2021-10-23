package com.example.myapplication

import android.app.Application
import android.hardware.usb.UsbRequest
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbConstants
import android.util.Log
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.util.*

class UsbCommunication : Application() {
    private val mInRequestPool = LinkedList<UsbRequest>()
    fun setUsbInterface(manager: UsbManager?, device: UsbDevice?): Boolean {

        if (device != null) {
            if (mDeviceConnection != null) {
                if (mInterface != null) {
                    mDeviceConnection!!.releaseInterface(mInterface)
                    mInterface = null
                }
                stopThread()
                mDeviceConnection!!.close()
                mDeviceConnection = null
                usbDevice = null
            }
            val deviceInterface = device.getInterface(0)
            val connection = manager?.openDevice(device)
            if (connection != null) {
                if (DEBUG) Log.d(TAG, "open succeeded")
                if (connection.claimInterface(deviceInterface, true)) {
                    if (DEBUG) Log.d(TAG, "claim interface succeeded")
                    usbDevice = device
                    mInterface = deviceInterface
                    mDeviceConnection = connection
                    var epOut: UsbEndpoint? = null
                    var epIn: UsbEndpoint? = null
                    for (i in 0 until deviceInterface.endpointCount) {
                        val ep = deviceInterface.getEndpoint(i)
                        if (ep.direction == UsbConstants.USB_DIR_OUT) epOut =
                            ep else if (ep.direction == UsbConstants.USB_DIR_IN) epIn = ep
                    }
                    if (epOut == null || epIn == null) {
                        Log.e(TAG, "not all endpoints found")
                        throw IllegalArgumentException("not all endpoints found.")
                    }
                    mEndpointOut = epOut
                    mEndpointIn = epIn
                    startThread()
                    clearBuffer()
                    return true
                } else {
                    Log.e(TAG, "claim interface failed")
                    connection.close()
                }
            } else {
                Log.e(TAG, "open failed")
            }
        } else {
            clearBuffer()
            stopThread()
            mDeviceConnection = null
            mInterface = null
            usbDevice = null
        }
        return false
    }

    fun clearBuffer() {
        try {
            sleep(1500)
            response
            synchronized(mInRequestPool) { mInRequestPool.clear() }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error on clearing buffer: " + e.message)
        }
    }

    fun sendCmd(message: ByteArray?, length: Int): Int {
        var sendBytes = 0
        synchronized(this) {
            if (mDeviceConnection != null) {
                sendBytes = mDeviceConnection!!.controlTransfer(
                    0x21, 0x09, 0x00, 0x00, message, length, 2000
                )
            }
        }
        return sendBytes
    }

    val response: ByteArray?
        get() {
            try {
                val request = inRequest
                request!!.queue(mDataBuffer, DATA_LENGTH)
                if (mDataBuffer.hasArray()) {
                    if (DEBUG) Log.d(TAG, "buffer have data")
                    return mDataBuffer.array()
                }
                return null
            } catch (e: Exception) {
                Log.e(TAG, "Error on capture response: " + e.message)
                return null
            }
        }
    private val inRequest: UsbRequest?
        private get() {
            synchronized(mInRequestPool) {
                return if (mInRequestPool.isEmpty()) {
                    if (DEBUG) Log.d(
                        TAG,
                        "pool is empty"
                    )
                    val request = UsbRequest()
                    if (mDeviceConnection == null) return null
                    request.initialize(
                        mDeviceConnection,
                        mEndpointIn
                    )
                    request
                } else {
                    mInRequestPool.removeFirst()
                }
            }
        }

    private fun startThread(): Thread? {
        if (DEBUG) Log.d(TAG, "startThread")
        mStop = false
        mThread = Thread(NewRunnable())
        mThread!!.start()
        return mThread
    }

    private fun stopThread() {
        mStop = true
        try {
            mThread?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class NewRunnable : Runnable {
        override fun run() {
            while (true) {
                if (mStop) {
                    Log.d(TAG, "Stop Thread")
                    return
                }
                if (mDeviceConnection == null) return;
                val request = mDeviceConnection!!.requestWait() ?: break
                request.clientData = null
                synchronized(mInRequestPool) { mInRequestPool.add(request) }
            }
        }
    }

    private fun sleep(millisecond: Int) {
        try {
            Thread.sleep(millisecond.toLong())
        } catch (e: InterruptedException) {
        }
    }

    companion object {
        const val TAG = "UsbControl"
        private const val DEBUG = false
        var instance: UsbCommunication? = null
            private set
        var usbDevice: UsbDevice? = null
            private set
        private var mInterface: UsbInterface? = null
        private var mDeviceConnection: UsbDeviceConnection? = null
        private var mEndpointOut: UsbEndpoint? = null
        private var mEndpointIn: UsbEndpoint? = null
        var tagMode = 0
        var mStop = false
        private var mThread: Thread? = null
        private const val DATA_LENGTH = 64
        private val mDataBuffer = ByteBuffer.allocate(DATA_LENGTH)

        @JvmStatic
        fun newInstance(): UsbCommunication? {
            instance = UsbCommunication()
            return instance
        }
    }
}