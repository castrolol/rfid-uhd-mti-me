package com.example.myapplication

class CommandPowerManagement {
    enum class PowerState(val bPowerState: Byte) {
        Ready(0x00.toByte()),
        Standby(0x01.toByte()),
        Sleep(0x02.toByte());
    }

    /************************************************************
     * RFID_PowerEnterPowerState				*
     */
    internal class RFIDPowerEnterPowerState(usbComm: UsbCommunication?) : MTIBaseCommand(
        usbComm!!
    ) {
        fun setCmd(powerState: PowerState): Boolean {
            mParam.clear()
            mParam.add(powerState.bPowerState)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_PowerEnterPowerState
        }
    }

    /************************************************************
     * RFID_PowerSetIdleTime					*
     */
    internal class RFIDPowerSetIdleTime(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(powerState: PowerState): Boolean {
            mParam.clear()
            mParam.add(powerState.bPowerState)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_PowerSetIdleTime
        }
    }

    /************************************************************
     * RFID_PowerGetIdleTime					*
     */
    internal class RFIDPowerGetIdleTime(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(powerState: PowerState): Boolean {
            mParam.clear()
            mParam.add(powerState.bPowerState)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_PowerGetIdleTime
        }
    }
}