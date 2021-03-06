package com.example.myapplication

enum class MsgStatus(val rtnStatus: Byte) {
    RFID_STATUS_OK(0x00.toByte()),
    RFID_ERROR_CMD_INVALID_DATA_LENGTH(0x0E.toByte()),
    RFID_ERROR_CMD_INVALID_PARAMETER(0x0F.toByte()),
    RFID_ERROR_SYS_CHANNEL_TIMEOUT(0x0A.toByte()),
    RFID_ERROR_SYS_SECURITY_FAILURE(0xFE.toByte()),
    RFID_ERROR_SYS_MODULE_FAILURE(0xFF.toByte()),
    RFID_ERROR_HWOPT_READONLY_ADDRESS(0xA0.toByte()),
    RFID_ERROR_HWOPT_UNSUPPORTED_REGION(0xA1.toByte()),
    RFID_ERROR_18K6C_REQRN(0x01.toByte()),
    RFID_ERROR_18K6C_ACCESS(0x02.toByte()),
    RFID_ERROR_18K6C_KILL(0x03.toByte()),
    RFID_ERROR_18K6C_NOREPLY(0x04.toByte()),
    RFID_ERROR_18K6C_LOCK(0x05.toByte()),
    RFID_ERROR_18K6C_BLOCKWRITE(0x06.toByte()),
    RFID_ERROR_18K6C_BLOCKERASE(0x07.toByte()),
    RFID_ERROR_18K6C_READ(0x08.toByte()),
    RFID_ERROR_18K6C_SELECT(0x09.toByte()),
    RFID_ERROR_18K6C_EASCODE(0x20.toByte()),
    RFID_ERROR_18K6B_INVALID_CRC(0x11.toByte()),
    RFID_ERROR_18K6B_RFICREG_FIFO(0x12.toByte()),
    RFID_ERROR_18K6B_NO_RESPONSE(0x13.toByte()),
    RFID_ERROR_18K6B_NO_ACKNOWLEDGE(0x14.toByte()),
    RFID_ERROR_18K6B_PREAMBLE(0x15.toByte()),
    RFID_ERROR_6CTAG_OTHER_ERROR(0x80.toByte()),
    RFID_ERROR_6CTAG_MEMORY_OVERRUN(0x83.toByte()),
    RFID_ERROR_6CTAG_MEMORY_LOCKED(0x84.toByte()),
    RFID_ERROR_6CTAG_INSUFFICIENT_POWER(0x8B.toByte()),
    RFID_ERROR_6CTAG_NONSPECIFIC_ERROR(0x8F.toByte());

}