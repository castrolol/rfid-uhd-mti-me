package com.example.myapplication

import android.util.Log
import java.lang.Exception
import java.util.*

abstract class MTIBaseCommand(private val usb: UsbCommunication) {
    private val TAG = MTIBaseCommand.javaClass.name
    private var mStatus: Byte = 0
    private var mStrStatus: String? = null

    @JvmField
    protected var mCommandHeader: CommandHeader? = null

    @JvmField
    protected var mParam = ArrayList<Byte>()
    protected var mFinalCmd = ByteArray(CMD_LENGTH)
    var response: ByteArray? = ByteArray(RESPONSE_LENGTH)
        protected set

    protected fun composeCmd() {
        mFinalCmd[0] = mCommandHeader!!.get1stCmd()
        mFinalCmd[1] = (mParam.size + 2).toByte()
        for (i in mParam.indices) {
            mFinalCmd[i + 2] = mParam[i]
        }
        usb.sendCmd(mFinalCmd, mParam.size + 2)

        // #### log whole command for debug ####
        if (DEBUG) Log.d(UsbCommunication.TAG, "TX: " + strCmd(mFinalCmd))
    }

    fun checkStatus(): Boolean {
        var result = false
        try {

            dataFromUsb
            mStatus = response!![2]
            status
            result = if (response!![0] == (mFinalCmd[0] + 1).toByte()) {
                mStatus.toInt() == 0x00
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking status: " + e.message)
        }
        return result
    }

    // #### log whole command for debug ####
    private val dataFromUsb: ByteArray?
        get() {
            response = usb.response

            // #### log whole command for debug ####
            if (DEBUG) Log.d(
                UsbCommunication.TAG, "RX: " + strCmd(
                    response
                )
            )
            return response
        }

    fun responseData(length: Int): String {
        var hexResult = ""
        for (i in 0 until length * 2) {
            hexResult += ((if (response!![i + 4] < 0 || response!![i + 4] > 15) Integer.toHexString(
                0xff and response!![i + 4]
                    .toInt()
            ) else "0" + Integer.toHexString(0xff and response!![i + 4].toInt()))
                    + if (i % 2 == 1) " " else "")
        }
        return hexResult.toUpperCase()
    }

    val status: String?
        get() {
            when (mStatus) {
                0x00.toByte() -> mStrStatus = "RFID_STATUS_OK"
                0x0E.toByte() -> mStrStatus = "RFID_ERROR_CMD_INVALID_DATA_LENGTH"
                0x0F.toByte() -> mStrStatus = "RFID_ERROR_CMD_INVALID_PARAMETER"
                0x0A.toByte() -> mStrStatus = "RFID_ERROR_SYS_CHANNEL_TIMEOUT"
                0xFE.toByte() -> mStrStatus = "RFID_ERROR_SYS_SECURITY_FAILURE"
                0xFF.toByte() -> mStrStatus = "RFID_ERROR_SYS_MODULE_FAILURE"
                0xA0.toByte() -> mStrStatus = "RFID_ERROR_HWOPT_READONLY_ADDRESS"
                0xA1.toByte() -> mStrStatus = "RFID_ERROR_HWOPT_UNSUPPORTED_REGION"
                0x01.toByte() -> mStrStatus = "RFID_ERROR_18K6C_REQRN"
                0x02.toByte() -> mStrStatus = "RFID_ERROR_18K6C_ACCESS"
                0x03.toByte() -> mStrStatus = "RFID_ERROR_18K6C_KILL"
                0x04.toByte() -> mStrStatus = "RFID_ERROR_18K6C_NOREPLY"
                0x05.toByte() -> mStrStatus = "RFID_ERROR_18K6C_LOCK"
                0x06.toByte() -> mStrStatus = "RFID_ERROR_18K6C_BLOCKWRITE"
                0x07.toByte() -> mStrStatus = "RFID_ERROR_18K6C_BLOCKERASE"
                0x08.toByte() -> mStrStatus = "RFID_ERROR_18K6C_READ"
                0x09.toByte() -> mStrStatus = "RFID_ERROR_18K6C_SELECT"
                0x20.toByte() -> mStrStatus = "RFID_ERROR_18K6C_EASCODE"
                0x11.toByte() -> mStrStatus = "RFID_ERROR_18K6B_INVALID_CRC"
                0x12.toByte() -> mStrStatus = "RFID_ERROR_18K6B_RFICREG_FIFO"
                0x13.toByte() -> mStrStatus = "RFID_ERROR_18K6B_NO_RESPONSE"
                0x14.toByte() -> mStrStatus = "RFID_ERROR_18K6B_NO_ACKNOWLEDGE"
                0x15.toByte() -> mStrStatus = "RFID_ERROR_18K6B_PREAMBLE"
                0x80.toByte() -> mStrStatus = "RFID_ERROR_6CTAG_OTHER_ERROR"
                0x83.toByte() -> mStrStatus = "RFID_ERROR_6CTAG_MEMORY_OVERRUN"
                0x84.toByte() -> mStrStatus = "RFID_ERROR_6CTAG_MEMORY_LOCKED"
                0x8B.toByte() -> mStrStatus = "RFID_ERROR_6CTAG_INSUFFICIENT_POWER"
                0x8F.toByte() -> mStrStatus = "RFID_ERROR_6CTAG_NONSPECIFIC_ERROR"
            }
            return mStrStatus
        }

    fun strCmd(BtoS: ByteArray?): String {
        var hexResult = ""
        for (i in BtoS!!.indices) {
            hexResult += ((if (BtoS[i] < 0 || BtoS[i] > 15) Integer.toHexString(
                0xff and BtoS[i]
                    .toInt()
            ) else "0" + Integer.toHexString(0xff and BtoS[i].toInt()))
                    + if (i == BtoS.size) "" else " ")
        }
        return hexResult.toUpperCase()
    }

    fun byteCmd(StoB: String): ByteArray {
        var subStr: String
        val iLength = StoB.length / 2
        val bytes = ByteArray(iLength)
        for (i in 0 until iLength) {
            subStr = StoB.substring(2 * i, 2 * i + 2)
            bytes[i] = subStr.toInt(16).toByte()
        }
        return bytes
    }

    // #### delay ####
    protected fun delay(milliSecond: Int) {
        try {
            Thread.sleep(milliSecond.toLong())
        } catch (e: InterruptedException) {
        }
    }

    companion object {
        /*	
	// RFID Module Configuration
	private static final byte[] RFID_RadioSetRegion					= {(byte)0xA8, 0x03};
	private static final byte[] RFID_RadioGetRegion					= {(byte)0xAA, 0x02};
	// Antenna Port Operation
	private static final byte[] RFID_AntennaPortSetPowerLevel		= {(byte)0xC0, 0x03};
	private static final byte[] RFID_AntennaPortGetPowerLevel		= {(byte)0xC2, 0x02};
	private static final byte[] RFID_AntennaPortSetFrequency		= {		 0x41, 0x09};
	private static final byte[] RFID_AntennaPortSetOperation		= {(byte)0xE4, 0x04};
	private static final byte[] RFID_AntennaPortCtrlPowerState		= {		 0x18, 0x03};
	private static final byte[] RFID_AntennaPortTransmitPattern		= {(byte)0xE6, 0x04};
	private static final byte[] RFID_AntennaPortTransmitPulse		= {(byte)0xEA, 0x04};
	// ISO 18000-6C Tag Access
	private static final byte[] RFID_18K6CSetQueryParameter			= {		 0x59, 0x0E};
	private static final byte[] RFID_18K6CTagInventory				= {		 0x31, 0x03};
	private static final byte[] RFID_18K6CTagInventoryRSSI			= {		 0x43, 0x03};
	private static final byte[] RFID_18K6CTagSelect					= {		 0x33, 0x03};		// 0x03~0x40
	private static final byte[] RFID_18K6CTagRead					= {		 0x37, 0x09};
	private static final byte[] RFID_18K6CTagWrite					= {		 0x35, 0x0B};		// 0x0B~0x3F
	private static final byte[] RFID_18K6CTagKill					= {		 0x3D, 0x06};
	private static final byte[] RFID_18K6CTagLock					= {		 0x3B, 0x08};
	private static final byte[] RFID_18K6CTagBlockWrite				= {		 0x70, 0x0B};		// 0x0B~0x3F
	private static final byte[] RFID_18K6CTagNXPCommand				= {		 0x45, 0x0A};
	private static final byte[] RFID_18K6CTagNXPTriggerEASAlarm		= {		 0x72, 0x02};
	// ISO 18000-6B Tag Access
	private static final byte[] RFID_18K6BTagInventory				= {		 0x3F, 0x0D};
	private static final byte[] RFID_18K6BTagRead					= {		 0x49, 0x0C};
	private static final byte[] RFID_18K6BTagWrite					= {		 0x47, 0x0C};		// 0x0C~0x40
	// RFID Module Firmware Access
	private static final byte[] RFID_MacGetModuleID					= {		 0x10, 0x03};
	private static final byte[] RFID_MacGetDebugValue				= {(byte)0xA2, 0x02};
	private static final byte[] RFID_MacBypassWriteRegister			= {		 0x1A, 0x06};
	private static final byte[] RFID_MacBypassReadRegister			= {		 0x1C, 0x03};
	private static final byte[] RFID_MacWriteOemData				= {(byte)0xA4, 0x05};
	private static final byte[] RFID_MacReadOemData					= {(byte)0xA6, 0x04};
	private static final byte[] RFID_MacSoftReset					= {(byte)0xA0, 0x02};
	private static final byte[] RFID_MacEnterUpdateMode				= {(byte)0xD0, 0x02};
	// RFID Module Manufacturer Engineering
	private static final byte[] RFID_EngSetExternalPA				= {(byte)0xE0, 0x03};
	private static final byte[] RFID_EngGetAmbientTemp				= {(byte)0xE2, 0x02};
	private static final byte[] RFID_EngGetForwardRFPower			= {(byte)0xEC, 0x02};
	private static final byte[] RFID_EngTransmitSerialPattern		= {(byte)0xE8, 0x07};
	private static final byte[] RFID_EngWriteFullOemData			= {(byte)0xEE, 0x05};
*/
        private const val DEBUG = false
        private const val CMD_LENGTH = 64
        private const val RESPONSE_LENGTH = 64
    }

    init {
        mParam.clear()
    }
}