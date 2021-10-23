package com.example.myapplication

enum class CommandHeader(private val byte1st: Byte) {
    // RFID Module Configuration
    RFID_RadioSetRegion(0xA8.toByte()),
    RFID_RadioGetRegion(0xAA.toByte()),

    // Antenna Port Operation
    RFID_AntennaPortSetPowerLevel(0xC0.toByte()),
    RFID_AntennaPortGetPowerLevel(0xC2.toByte()),
    RFID_AntennaPortSetFrequency(0x41.toByte()),
    RFID_AntennaPortSetOperation(0xE4.toByte()),
    RFID_AntennaPortCtrlPowerState(0x18.toByte()),
    RFID_AntennaPortTransmitPattern(0xE6.toByte()),
    RFID_AntennaPortTransmitPulse(0xEA.toByte()),

    // ISO 18000-6C Tag Access
    RFID_18K6CSetQueryParameter(0x59.toByte()),
    RFID_18K6CTagInventory(0x31.toByte()),
    RFID_18K6CTagInventoryRSSI(0x43.toByte()),
    RFID_18K6CTagSelect(0x33.toByte()),
    RFID_18K6CTagRead(0x37.toByte()),
    RFID_18K6CTagWrite(0x35.toByte()),
    RFID_18K6CTagKill(0x3D.toByte()),
    RFID_18K6CTagLock(0x3B.toByte()),
    RFID_18K6CTagBlockWrite(0x70.toByte()),
    RFID_18K6CTagNXPCommand(0x45.toByte()),
    RFID_18K6CTagNXPTriggerEASAlarm(0x72.toByte()),

    // ISO 18000-6B Tag Access
    RFID_18K6BTagInventory(0x3F.toByte()),
    RFID_18K6BTagRead(0x49.toByte()),
    RFID_18K6BTagWrite(0x47.toByte()),

    // RFID Module Firmware Access
    RFID_MacGetModuleID(0x10.toByte()),
    RFID_MacGetDebugValue(0xA2.toByte()),
    RFID_MacBypassWriteRegister(0x1A.toByte()),
    RFID_MacBypassReadRegister(0x1C.toByte()),
    RFID_MacWriteOemData(0xA4.toByte()),
    RFID_MacReadOemData(0xA6.toByte()),
    RFID_MacSoftReset(0xA0.toByte()),
    RFID_MacEnterUpdateMode(0xD0.toByte()),

    // RFID Module Manufacturer Engineering
    RFID_EngSetExternalPA(0xE0.toByte()),
    RFID_EngGetAmbientTemp(0xE2.toByte()),
    RFID_EngGetForwardRFPower(0xEC.toByte()),
    RFID_EngTransmitSerialPattern(0xE8.toByte()),
    RFID_EngWriteFullOemData(0xEE.toByte()),

    // RFID Module Power Management
    RFID_PowerEnterPowerState(0x02.toByte()),
    RFID_PowerSetIdleTime(0x04.toByte()),
    RFID_PowerGetIdleTime(0x06.toByte());

    fun get1stCmd(): Byte {
        return byte1st
    }
}