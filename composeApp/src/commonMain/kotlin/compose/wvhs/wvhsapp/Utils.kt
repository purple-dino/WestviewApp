package compose.wvhs.wvhsapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.liftric.kvault.KVault
import compose.wvhs.wvhsapp.ScheduleViewModel.Period
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

enum class Days (val value: Int){
    MondayFriday(0),
    TuesdayThursday(1),
    Wednesday(2)
}

// Create shared view model
class StudentSharedViewModel : ViewModel() {
    var gradebook: Gradebook? = null
    var classes: Classes? = null
    var store: KVault? = null
    var gradingPeriods: List<String>? by mutableStateOf(null)
    var selectedGradingPeriod: Int? by mutableStateOf(null)
    var student: StudentVUE? by mutableStateOf(null)
    var currentBellScheduleType: Int? by mutableStateOf(null)
    var attendance: Attendance? by mutableStateOf(null)
    var todaysBellSchedule: List<Period>? by mutableStateOf(null)

    fun changeTimesOfBellSchedule(newSchedule: List<Period>) {
        todaysBellSchedule = newSchedule
    }
    fun changeGradebook(newGradebook: Gradebook) {
        gradebook = newGradebook
    }
    fun setClassList(newClasses: Classes) {
        classes = newClasses
    }
    fun setViewmodelStore(newStore: KVault) {
        store = newStore
    }
    fun changeGradingPeriods(newGradingPeriods: List<String>) {
        gradingPeriods = newGradingPeriods
    }
    fun changeSelectedGradingPeriod(newGradingPeriod: Int) {
        selectedGradingPeriod = newGradingPeriod
    }
    fun changeStudent(newStudent: StudentVUE) {
        student = newStudent
    }
    fun changeBellSchedule(newBellSchedule: String?) {
        currentBellScheduleType = when (newBellSchedule) {
            "MonFri Bell" -> Days.MondayFriday.value
            "Tues-Thurs Bell" -> Days.TuesdayThursday.value
            "Wed Bell" -> Days.Wednesday.value
            else -> {
                when (Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault()).dayOfWeek.isoDayNumber) {
                    1 -> Days.MondayFriday.value
                    2 -> Days.TuesdayThursday.value
                    3 -> Days.Wednesday.value
                    4 -> Days.TuesdayThursday.value
                    5 -> Days.MondayFriday.value
                    else -> null
                }
            }
        }
    }
    fun changeAttendance(newAttendance: Attendance) {
        attendance = newAttendance
    }
    fun resetAllData() {
        gradebook = null
        classes = null
        store = null
        gradingPeriods = null
        selectedGradingPeriod = null
        student = null
        attendance = null
    }
}

// Function to get other days of the week
fun getSchedule(classList: List<String>?, dayOfWeek: Int?, studentSharedViewModel: StudentSharedViewModel): List<Period> {
    return when (dayOfWeek) {
        Days.MondayFriday.value -> listOf(
            Period(classList?.getOrNull(0) ?: "Class 1", LocalTime(8, 35), LocalTime(10, 1)),
            Period("Passing", LocalTime(10, 1), LocalTime(10, 7)),
            Period(classList?.getOrNull(4) ?: "Homeroom", LocalTime(10, 7), LocalTime(10, 24)),
            Period("Passing", LocalTime(10, 24), LocalTime(10, 30)),
            Period(classList?.getOrNull(1) ?: "Class 2", LocalTime(10, 30), LocalTime(11, 56)),
            Period("Lunch", LocalTime(11, 56), LocalTime(12, 31)),
            Period("Passing", LocalTime(12, 31), LocalTime(12, 37)),
            Period(classList?.getOrNull(2) ?: "Class 3", LocalTime(12, 37), LocalTime(14, 3)),
            Period("Passing", LocalTime(14, 3), LocalTime(14, 9)),
            Period(classList?.getOrNull(3) ?: "Class 4", LocalTime(14, 9), LocalTime(15, 35))
        )
        Days.TuesdayThursday.value -> listOf(
            Period(classList?.getOrNull(0) ?: "Class 1", LocalTime(8, 35), LocalTime(9, 54)),
            Period("Wolverine Time", LocalTime(9, 54), LocalTime(10, 30)),
            Period("Passing", LocalTime(10, 30), LocalTime(10, 36)),
            Period(classList?.getOrNull(1) ?: "Class 2", LocalTime(10, 36), LocalTime(11, 55)),
            Period("Lunch", LocalTime(11, 55), LocalTime(12, 30)),
            Period("Passing", LocalTime(12, 30), LocalTime(12, 36)),
            Period("SSH", LocalTime(12, 36), LocalTime(12, 51)),
            Period(classList?.getOrNull(2) ?: "Class 3", LocalTime(12, 51), LocalTime(14, 10)),
            Period("Passing", LocalTime(14, 10), LocalTime(14, 16)),
            Period(classList?.getOrNull(3) ?: "Class 4", LocalTime(14, 16), LocalTime(15, 35))
        )
        Days.Wednesday.value -> listOf(
            Period(classList?.getOrNull(0) ?: "Class 1", LocalTime(9, 35), LocalTime(10, 44)),
            Period("Passing", LocalTime(10, 44), LocalTime(10, 50)),
            Period(classList?.getOrNull(1) ?: "Class 2", LocalTime(10, 50), LocalTime(11, 59)),
            Period("Lunch", LocalTime(11, 59), LocalTime(12, 34)),
            Period("Passing", LocalTime(12, 34), LocalTime(12, 40)),
            Period(classList?.getOrNull(2) ?: "Class 3", LocalTime(12, 40), LocalTime(13, 49)),
            Period("Wolverine Time", LocalTime(13, 49), LocalTime(14, 20)),
            Period("Passing", LocalTime(14, 20), LocalTime(14, 26)),
            Period(classList?.getOrNull(3) ?: "Class 4", LocalTime(14, 26), LocalTime(15, 35))
        )
        else -> studentSharedViewModel.todaysBellSchedule ?: listOf()
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

@Composable
expect fun DisplayWebPage(webPage: String)