package compose.wvhs.wvhsapp
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.defaultTimeZone

actual fun is24HourFormat(): Boolean {
    val dateFormatter = NSDateFormatter()
    dateFormatter.locale = NSLocale.currentLocale
    dateFormatter.dateFormat = "HH:mm" // 24-hour format
    val is24Hour = dateFormatter.dateFromString("12:00") != null
    return is24Hour
}
