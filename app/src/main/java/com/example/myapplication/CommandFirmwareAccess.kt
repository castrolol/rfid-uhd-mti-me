package com.example.myapplication

class CommandFirmwareAccess {
    enum class ModuleId(val bModuleId: Byte) {
        FirmwareId(0x00.toByte()),
        HardwareId(0x01.toByte()),
        OEMCfgId(0x02.toByte()),
        OEMCfgUpdateId(0x03.toByte());
    }

    /************************************************************
     * RFID_MacGetModuleID						*
     */
    internal class RFIDMacGetModuleID(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(moduleId: ModuleId): Boolean {
            mParam.add(moduleId.bModuleId)
            super.composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_MacGetModuleID
        }
    }

    /************************************************************
     * RFID_MacGetDebugValue					*
     */
    internal class RFIDMacGetDebugValue(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(): Boolean {
            super.composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_MacGetDebugValue
        }
    }

    /************************************************************
     * RFID_MacBypassWriteRegister					*
     */
    internal class RFIDMacBypassWriteRegister(usbComm: UsbCommunication?) : MTIBaseCommand(
        usbComm!!
    ) {
        fun setCmd(regAddress: Byte, regData: ByteArray): Boolean {
            mParam.add(regAddress)
            for (register in regData) mParam.add(register)
            super.composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_MacBypassWriteRegister
        }
    }

    /************************************************************
     * RFID_MacBypassReadRegister					*
     */
    internal class RFIDMacBypassReadRegister(usbComm: UsbCommunication?) : MTIBaseCommand(
        usbComm!!
    ) {
        fun setCmd(regAddress: Byte): Boolean {
            mParam.add(regAddress)
            super.composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_MacBypassReadRegister
        }
    }

    /************************************************************
     * RFID_MacWriteOemData					*
     */
    internal class RFIDMacWriteOemData(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(oemCfgAddress: Int, oemCfgData: Byte): Boolean {
            if (oemCfgAddress < 0x0080 || oemCfgAddress > 0x07ff) {
                // error
                return false
            } else {
                for (i in 0..1) mParam.add((oemCfgAddress shr i * 8).toByte())
                mParam.add(oemCfgData)
                super.composeCmd()
            }
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_MacWriteOemData
        }
    }

    /************************************************************
     * RFID_MacReadOemData						*
     */
    internal class RFIDMacReadOemData(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(oemCfgAddress: Int): Boolean {
            if (oemCfgAddress < 0x0000 || oemCfgAddress > 0x1fff) {
                // error
                return false
            } else {
                for (i in 1 downTo 0) mParam.add((oemCfgAddress shr i * 8).toByte())
                super.composeCmd()
            }
            delay(50)
            return checkStatus()
        }

        val data: Byte
            get() = response!![3]

        init {
            mCommandHeader = CommandHeader.RFID_MacReadOemData
        }
    }

    /************************************************************
     * RFID_MacSoftReset						*
     */
    internal class RFIDMacSoftReset(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(): Boolean {
            super.composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_MacSoftReset
        }
    }

    /************************************************************
     * RFID_MacEnterUpdateMode					*
     */
    internal class RFIDMacEnterUpdateMode(usbComm: UsbCommunication?) : MTIBaseCommand(
        usbComm!!
    ) {
        fun setCmd(): Boolean {
            super.composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_MacEnterUpdateMode
        }
    }
}