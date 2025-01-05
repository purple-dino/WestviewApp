package compose.wvhs.wvhsapp

import androidx.compose.runtime.Composable
import compose.wvhs.wvhsapp.ViewModels.ScheduleViewModel.Period
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import kotlinx.datetime.LocalTime

enum class Days (val value: Int){
    MondayFriday(0),
    TuesdayThursday(1),
    Wednesday(2)
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

@Composable
expect fun DisplayWebPage(webPage: String)