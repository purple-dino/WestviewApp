package compose.wvhs.wvhsapp.Utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

expect fun is24HourFormat(): Boolean


// Function to format time to 24 hour or AM/PM
fun format24htoAmPm(localTime: LocalTime, addAmPm: Boolean? = false): String {
    return if (is24HourFormat()) {
        localTime.toString()
    } else {
        val hour = if (localTime.hour > 12) localTime.hour - 12 else localTime.hour
        val formattedMinute = localTime.minute.toString().padStart(2, '0')

        "$hour:$formattedMinute" + if (localTime.hour>12 && addAmPm == true) " PM" else if (addAmPm == true) " AM" else ""
    }
}

// Convert a localdate (2020-07-12) to a easily readable string
fun getHumanDate(date: LocalDate, showDayOfWeek: Boolean? = true): String {
    println(date)
    return if (showDayOfWeek == true) {
        "${date.dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }}, ${date.month.toString().lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}"
    } else {
        "${date.month.toString().lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth.toString().lowercase().replaceFirstChar { it.uppercase() }}"
    }
}