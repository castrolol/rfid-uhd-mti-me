package com.example.myapplication

class CommandModeConfiguration {
    enum class Region(val bRegion: Byte) {
        US_CA(0.toByte()),
        EU(1.toByte()),
        TW(2.toByte()),
        CN(3.toByte()),
        KR(4.toByte()),
        AU_NZ(5.toByte()),
        EU2(6.toByte()),
        BR(7.toByte()),
        HK(8.toByte()),
        MY(9.toByte()),
        SG(10.toByte()),
        TH(11.toByte()),
        IL(12.toByte()),
        RU(13.toByte()),
        IN(14.toByte()),
        SA(15.toByte()),
        JO(16.toByte()),
        MX(17.toByte());
    }

    /************************************************************
     * RFID_RadioSetRegion						*
     */
    internal class RFIDRadioSetRegion(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(region: Region): Boolean {
            mParam.add(region.bRegion)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        fun setCmd(region: Byte): Boolean {
            mParam.add(region)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_RadioSetRegion
        }
    }

    /************************************************************
     * RFID_RadioGetRegion						*
     */
    internal class RFIDRadioGetRegion(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(): Boolean {
            composeCmd()
            delay(200)
            return checkStatus()
        }

        val region: Byte
            get() = response!![3]

        init {
            mCommandHeader = CommandHeader.RFID_RadioGetRegion
        }
    }
}