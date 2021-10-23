package com.example.myapplication

class CommandAntenaPortOperation {
    enum class Mask(val bMask: Byte) {
        NoSpecificValue(0x00.toByte()),
        RssiScan(0x01.toByte()),
        ReflectedPowerScan(0x02.toByte()),
        AddFrequency(0x04.toByte()),
        ClearChannelList(0x08.toByte());
    }

    enum class Modulation(val bModulation: Byte) {
        Disable(0x00.toByte()),
        Enable(0x01.toByte());
    }

    enum class Operation(val bOperation: Byte) {
        Disable(0x00.toByte()),
        Enable(0x01.toByte());
    }

    enum class State(val bState: Byte) {
        PowerOff(0x00.toByte()),
        PowerOn(0xFF.toByte());
    }

    /************************************************************
     * RFID_AntennaPortSetPowerLevel				*
     */
    class RFIDAntennaPortSetPowerLevel(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(powerLevel: Byte): Boolean {
            mParam.add(powerLevel)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_AntennaPortSetPowerLevel
        }
    }

    /************************************************************
     * RFID_AntennaPortGetPowerLevel				*
     */
    class RFIDAntennaPortGetPowerLevel(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(): Boolean {
            composeCmd()
            delay(200)
            return checkStatus()
        }

        val powerLevel: Int
            get() = response!![3].toInt()

        init {
            mCommandHeader = CommandHeader.RFID_AntennaPortGetPowerLevel
        }
    }

    /************************************************************
     * RFID_AntennaPortSetFrequency				*
     */
    class RFIDAntennaPortSetFrequency(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(mask: Mask, freq: Int, rssi: Byte, padding: ByteArray): Boolean {
            mParam.add(mask.bMask)
            processFreq(freq)
            mParam.add(rssi)
            processPadding(padding)
            composeCmd()
            return true
        }

        private fun processFreq(freq: Int) {
            for (i in 0..2) mParam.add((freq shr i * 8).toByte())
        }

        private fun processPadding(padding: ByteArray) {
            for (i in padding.indices) mParam.add(padding[i])
        }

        init {
            mCommandHeader = CommandHeader.RFID_AntennaPortSetFrequency
        }
    }

    /************************************************************
     * RFID_AntennaPortSetOperation				*
     */
    class RFIDAntennaPortSetOperation(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(modulation: Modulation, operation: Operation): Boolean {
            mParam.add(modulation.bModulation)
            mParam.add(operation.bOperation)
            composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_AntennaPortSetOperation
        }
    }

    /************************************************************
     * RFID_AntennaPortCtrlPowerState				*
     */
    class RFIDAntennaPortCtrlPowerState(usbComm: UsbCommunication?) : MTIBaseCommand(
        usbComm!!
    ) {
        fun setCmd(state: State): Boolean {
            mParam.add(state.bState)
            composeCmd()
            return true
        }

        init {
            mCommandHeader = CommandHeader.RFID_AntennaPortCtrlPowerState
        }
    }

    /************************************************************
     * RFID_AntennaPortTransmitPattern				*
     */
    class RFIDAntennaPortTransmitPattern(usbComm: UsbCommunication?) : MTIBaseCommand(
        usbComm!!
    ) {
        fun setCmd(dwellTime: Int): Boolean {
            processDwellTime(dwellTime)
            composeCmd()
            return true
        }

        private fun processDwellTime(dwellTime: Int) {
            for (i in 1 downTo 0) mParam.add((dwellTime shr i * 8).toByte())
        }

        init {
            mCommandHeader = CommandHeader.RFID_AntennaPortTransmitPattern
        }
    }

    /************************************************************
     * RFID_AntennaPortTransmitPulse				*
     */
    class RFIDAntennaPortTransmitPulse(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(dwellTime: Int): Boolean {
            processDwellTime(dwellTime)
            composeCmd()
            return true
        }

        private fun processDwellTime(dwellTime: Int) {
            for (i in 1 downTo 0) mParam.add((dwellTime shr i * 8).toByte())
        }

        init {
            mCommandHeader = CommandHeader.RFID_AntennaPortTransmitPulse
        }
    }
}