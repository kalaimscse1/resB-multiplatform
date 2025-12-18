package com.warriortech.resb.util

fun generateNextBillNo(prefix: String, currentNumber: String): String {
    val numericPart = currentNumber.toInt()
    val digitCount = currentNumber.length
    val nextNumberStr = numericPart.toString().padStart(digitCount, '0')
    return "$prefix$nextNumberStr"
}