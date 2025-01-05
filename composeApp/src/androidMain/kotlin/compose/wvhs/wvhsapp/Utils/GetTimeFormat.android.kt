package compose.wvhs.wvhsapp.Utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

actual fun is24HourFormat(): Boolean {
    val dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    val timeFormat = (dateFormat as? SimpleDateFormat)?.toPattern()

    return timeFormat?.contains("HH") == true
}