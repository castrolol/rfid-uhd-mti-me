package com.example.myapplication.agoravai


fun convertEPC(epc: String): String {

    val bits = convert(epc).padStart(96, '0');

    val serialNumber = bits.substring(60).toLong(2);

    return serialNumber.toString()

}

private fun convert(hexaDecimal: String): String {
    val hexaDecimalN = hexaDecimal.replace(" ", "")

    if (checkHexaDecimalNumber(hexaDecimalN)) {

        var i = 0
        var binaryNum = ""
        while (i < hexaDecimalN.length) {
            when (hexaDecimalN[i]) {
                '0' -> binaryNum += "0000"
                '1' -> binaryNum += "0001"
                '2' -> binaryNum += "0010"
                '3' -> binaryNum += "0011"
                '4' -> binaryNum += "0100"
                '5' -> binaryNum += "0101"
                '6' -> binaryNum += "0110"
                '7' -> binaryNum += "0111"
                '8' -> binaryNum += "1000"
                '9' -> binaryNum += "1001"
                'A', 'a' -> binaryNum += "1010"
                'B', 'b' -> binaryNum += "1011"
                'C', 'c' -> binaryNum += "1100"
                'D', 'd' -> binaryNum += "1101"
                'E', 'e' -> binaryNum += "1110"
                'F', 'f' -> binaryNum += "1111"
            }
            i++
        }
        return binaryNum
    } else {
        println("$hexaDecimalN is not a hexaDecimal number")
    }
    return ""
}

fun checkHexaDecimalNumber(hexaDecimalNum: String): Boolean {
    var isHexaDecimalNum = true

    for (charAtPos in hexaDecimalNum) {
        if (!(
                    ((charAtPos >= '0') && (charAtPos <= '9'))
                            || ((charAtPos >= 'A') && (charAtPos <= 'F'))
                            || ((charAtPos >= 'a') && (charAtPos <= 'f'))
                    )
        ) {
            isHexaDecimalNum = false
            break
        }
    }
    return isHexaDecimalNum
}