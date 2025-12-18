package com.warriortech.resb.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun getCurrentDateModern(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Customize your date format
    return currentDate.format(formatter)
}

fun getCurrentTimeModern(): String {
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern(
        "hh:mm a",
        Locale.US
    ) // Customize your time format (e.g., "hh:mm a" for 12-hour format)
    return currentTime.format(formatter)
}

fun getCurrentDateTimeWithAmPm(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a", Locale("en", "IN"))
    return currentDateTime.format(formatter)
}


fun getCurrentTimeAsFloat(): Float {
    val now = java.time.LocalTime.now()
    return now.hour + now.minute / 60f
}