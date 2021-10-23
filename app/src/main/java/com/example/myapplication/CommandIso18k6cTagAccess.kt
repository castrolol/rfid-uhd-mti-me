package com.example.myapplication

class CommandIso18k6cTagAccess {
    enum class LinkFreqSet(val bLinkFreqSet: Byte) {
        DontChange(0x00.toByte()),
        Change(0x01.toByte());
    }

    enum class LinkFreqValue(val bLinkFreqValue: Byte) {
        _40kHz(0x00.toByte()),
        _160kHz(0x06.toByte()),
        _213kHz(0x08.toByte()),
        _256kHz(0x09.toByte()),
        _320kHz(0x0C.toByte()),
        _640kHz(0x0F.toByte());
    }

    enum class MillerSet(val bMillerSet: Byte) {
        DontChange(0x00.toByte()),
        Change(0x01.toByte());
    }

    enum class MillerValue(val bMillerValue: Byte) {
        FM0Baseband(0x00.toByte()),
        Miller2Subcarrier(0x01.toByte()),
        Miller4Subcarrier(0x02.toByte()),
        Miller8Subcarrier(0x03.toByte());
    }

    enum class SessionSet(val bSessionSet: Byte) {
        DontChange(0x00.toByte()),
        Change(0x01.toByte());
    }

    enum class SessionValue(val bSessionValue: Byte) {
        S0Session(0x00.toByte()),
        S1Session(0x01.toByte()),
        S2Session(0x02.toByte()),
        S3Session(0x03.toByte()),
        SL(0x04.toByte());
    }

    enum class TRextSet(val bTRextSet: Byte) {
        DontChange(0x00.toByte()),
        Change(0x01.toByte());
    }

    enum class TRextValue(val bTRextValue: Byte) {
        NoPilotTone(0x00.toByte()),
        UsePilotTone(0x01.toByte());
    }

    enum class QBeginSet(val bQBeginSet: Byte) {
        DontChange(0x00.toByte()),
        Change(0x01.toByte());
    }

    enum class SensitivitySet(val bSensitivitySet: Byte) {
        DontChange(0x00.toByte()),
        Change(0x01.toByte());
    }

    enum class Action(val bAction: Byte) {
        StartInventory(0x01.toByte()),
        NextTag(0x02.toByte()),
        GetAllTags(0x03.toByte());
    }

    enum class MemoryBank(val bMemoryBank: Byte) {
        Reserved(0x00.toByte()),
        EPC(0x01.toByte()),
        TID(0x02.toByte()),
        User(0x03.toByte());
    }

    enum class LockAction(val bLockAction: Byte) {
        Accessible(0x00.toByte()),
        AlwaysAccessable(0x01.toByte()),
        PasswordAccessible(0x02.toByte()),
        AlwaysNotAccessible(0x03.toByte());
    }

    enum class MemorySpace(val bMemorySpace: Byte) {
        ReservedKillPassword(0x00.toByte()),
        ReservedAccessPassword(0x01.toByte()),
        EPC(0x02.toByte()), TID(0x03.toByte()),
        User(0x04.toByte());
    }

    enum class NXPCommand(val bNXPCommand: Byte) {
        EASStatus(0x01.toByte()), ReadProtectStauts(0x02.toByte()), ConfigWord(0x09.toByte());
    }

    enum class BitStatus(val bBitStatus: Byte) {
        Reset(0x00.toByte()), Set(0x01.toByte());
    }

    /************************************************************
     * RFID_18K6CSetQueryParameter					*
     */
    class RFID18K6CSetQueryParameter(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCommand(
            linkFreqSet: LinkFreqSet, linkFreqValue: LinkFreqValue,
            millerSet: MillerSet, millerValue: MillerValue,
            sessionSet: SessionSet, sessionValue: SessionValue,
            trextSet: TRextSet, trextValue: TRextValue,
            qBeginSet: QBeginSet, qBeginValue: Byte,
            sensitivitySet: SensitivitySet, sensivityValue: Byte
        ): Boolean {
            mParam.add(linkFreqSet.bLinkFreqSet)
            mParam.add(linkFreqValue.bLinkFreqValue)
            mParam.add(millerSet.bMillerSet)
            mParam.add(millerValue.bMillerValue)
            mParam.add(sessionSet.bSessionSet)
            mParam.add(sessionValue.bSessionValue)
            mParam.add(trextSet.bTRextSet)
            mParam.add(trextValue.bTRextValue)
            mParam.add(qBeginSet.bQBeginSet)
            mParam.add(qBeginValue)
            mParam.add(sensitivitySet.bSensitivitySet)
            mParam.add(sensivityValue)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        fun setCommand(
            linkFreqSet: Byte, linkFreqValue: Byte, millerSet: Byte, millerValue: Byte,
            sessionSet: Byte, sessionValue: Byte, trextSet: Byte, trextValue: Byte,
            qBeginSet: Byte, qBeginValue: Byte, sensitivitySet: Byte, sensivityValue: Byte
        ): Boolean {
            mParam.add(linkFreqSet)
            mParam.add(linkFreqValue)
            mParam.add(millerSet)
            mParam.add(millerValue)
            mParam.add(sessionSet)
            mParam.add(sessionValue)
            mParam.add(trextSet)
            mParam.add(trextValue)
            mParam.add(qBeginSet)
            mParam.add(qBeginValue)
            mParam.add(sensitivitySet)
            mParam.add(sensivityValue)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        fun setCommand(): Boolean {
            for (i in 0..11) mParam.add(0x00.toByte())
            composeCmd()
            delay(200)
            return checkStatus()
        }

        val sensivity: Int
            get() = response!![14].toInt()
        val linkFrequency: Int
            get() = response!![4].toInt()
        val session: Int
            get() = response!![8].toInt()
        val coding: Int
            get() = response!![6].toInt()
        val qBegin: Int
            get() = response!![12].toInt()

        init {
            mCommandHeader = CommandHeader.RFID_18K6CSetQueryParameter
        }
    }

    /************************************************************
     * RFID_18K6CTagInventory					*
     */
    class RFID18K6CTagInventory(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(action: Action): Boolean {
            mParam.clear()
            mParam.add(action.bAction)
            composeCmd()
            if (action == Action.StartInventory) delay(100) else delay(50)
            return checkStatus()
        }

        val tagNumber: Byte
            get() = response!![3]

        // #### minus 2 bytes, bcz epc data = pc + epc ####
        val tagId: String
            get() {
                val iEpcLength =
                    response!![4] - 2 // #### minus 2 bytes, bcz epc data = pc + epc ####
                val tagId = ByteArray(if (iEpcLength > 0) iEpcLength else 0)
                for (i in 0 until iEpcLength) {
                    tagId[i] = response!![i + 7]
                }
                return strCmd(tagId)
            }
        val tag: String
            get() = strCmd(response)

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagInventory
        }
    }

    /************************************************************
     * RFID_18K6CTagInventoryRSSI					*
     */
    class RFID18K6CTagInventoryRSSI(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(action: Action): Boolean {
            mParam.add(action.bAction)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagInventoryRSSI
        }
    }

    /************************************************************
     * RFID_18K6CTagSelect						*
     */
    class RFID18K6CTagSelect(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(maskData: ByteArray): Boolean {
            mParam.clear()
            mParam.add(maskData.size.toByte())
            for (data in maskData) mParam.add(data)
            composeCmd()
            delay(50)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagSelect
        }
    }

    /************************************************************
     * RFID_18K6CTagRead						*
     */
    class RFID18K6CTagRead(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(
            memoryBank: MemoryBank,
            memoryAddress: Byte,
            accessPassword: Long,
            tagDataLength: Byte
        ): Boolean {
            mParam.add(memoryBank.bMemoryBank)
            mParam.add(memoryAddress)
            for (i in 3 downTo 0) mParam.add((accessPassword shr i * 8).toByte())
            mParam.add(tagDataLength)
            composeCmd()
            delay(200)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagRead
        }
    }

    /************************************************************
     * RFID_18K6CTagWrite						*
     */
    class RFID18K6CTagWrite(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(
            memoryBank: MemoryBank,
            memoryAddress: Byte,
            accessPassword: Long,
            tagData: ByteArray
        ): Boolean {
            mParam.add(memoryBank.bMemoryBank)
            mParam.add(memoryAddress)
            for (i in 3 downTo 0) mParam.add((accessPassword shr i * 8).toByte())
            mParam.add((tagData.size / 2).toByte())
            for (data in tagData) mParam.add(data)
            composeCmd()
            delay(500)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagWrite
        }
    }

    /************************************************************
     * RFID_18K6CTagKill						*
     */
    class RFID18K6CTagKill(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(accessPassword: Long): Boolean {
            for (i in 3 downTo 0) mParam.add((accessPassword shr i * 8).toByte())
            composeCmd()
            delay(500)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagKill
        }
    }

    /************************************************************
     * RFID_18K6CTagLock						*
     */
    class RFID18K6CTagLock(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(
            lockAction: LockAction,
            memorySpace: MemorySpace,
            accessPassword: Long
        ): Boolean {
            mParam.add(lockAction.bLockAction)
            mParam.add(memorySpace.bMemorySpace)
            for (i in 3 downTo 0) mParam.add((accessPassword shr i * 8).toByte())
            composeCmd()
            delay(1000)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagLock
        }
    }

    /************************************************************
     * RFID_18K6CTagBlockWrite						*
     */
    class RFID18K6CTagBlockWrite(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(
            memoryBank: MemoryBank,
            memoryAddress: Byte,
            accessPassword: Long,
            tagData: ByteArray,
            delayTime: Int
        ): Boolean {
            mParam.add(memoryBank.bMemoryBank)
            mParam.add(memoryAddress)
            for (i in 3 downTo 0) mParam.add((accessPassword shr i * 8).toByte())
            mParam.add((tagData.size / 2).toByte())
            for (data in tagData) mParam.add(data)
            composeCmd()
            delay(500)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagBlockWrite
        }
    }

    /************************************************************
     * RFID_18K6CTagNXPCommand					*
     */
    class RFID18K6CTagNXPCommand(usbComm: UsbCommunication?) : MTIBaseCommand(usbComm!!) {
        fun setCmd(
            nxpCommand: NXPCommand,
            bitStatus: BitStatus,
            accessPassword: Long,
            configWord: Short
        ): Boolean {
            mParam.add(nxpCommand.bNXPCommand)
            mParam.add(bitStatus.bBitStatus)
            for (i in 3 downTo 0) mParam.add((accessPassword shr i * 8).toByte())
            for (i in 1 downTo 0) mParam.add((configWord.toInt() shr i * 8).toByte())
            composeCmd()
            delay(500)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagNXPCommand
        }
    }

    /************************************************************
     * RFID_18K6CTagNXPTriggerEASAlarm				*
     */
    class RFID18K6CTagNXPTriggerEASAlarm(usbComm: UsbCommunication?) : MTIBaseCommand(
        usbComm!!
    ) {
        fun setCmd(): Boolean {
            composeCmd()
            delay(500)
            return checkStatus()
        }

        init {
            mCommandHeader = CommandHeader.RFID_18K6CTagNXPTriggerEASAlarm
        }
    }
}