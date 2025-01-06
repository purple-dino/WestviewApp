package compose.wvhs.wvhsapp.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import compose.wvhs.wvhsapp.Pages.className
import compose.wvhs.wvhsapp.Utils.createNotifications
import compose.wvhs.wvhsapp.Pages.endTime
import compose.wvhs.wvhsapp.Pages.startTime
import compose.wvhs.wvhsapp.Utils.vibrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// the schedule time remaining
class ScheduleViewModel(studentSharedViewModel: StudentSharedViewModel) {
    // Create initial variables
    data class Period(val name: String, val start: LocalTime, val end: LocalTime)

    private val _remainingTime = MutableStateFlow("")
    val remainingTime = _remainingTime.asStateFlow()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Create the schedule
    val schoolSchedule = studentSharedViewModel.todaysBellSchedule ?: listOf()

    var currentPeriod by mutableStateOf(-1)

    // Init function
    init {
        // Start the actual timer
        startUpdating()

        // Create the notifications
        if (schoolSchedule.isNotEmpty() && studentSharedViewModel.settings?.getBoolean("useNotifications", false) == true) {
            createNotifications(schoolSchedule)
        }
    }

    // Function to start the timer
    private fun startUpdating() {
        // Starts the coroutine for the updating
        coroutineScope.launch {
            while (true) {
                _remainingTime.value = getRemainingTime(schoolSchedule)
                delay(1000) // Update every second
            }
        }
    }

    // Function to get the remaining time of the period, automatically finds the correct period
    private suspend fun getRemainingTime(schedule: List<Period>): String {
        // Create the initial variables
        if (schedule.isEmpty()) {
            return "There's no school today!"
        }

        val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        val schoolStartTime = schedule[0].start
        val schoolEndTime = LocalTime(15, 35)

        // Check if school is over
        if (currentTime > schoolEndTime) {
            return "School's over!"
        }

        // Check if school hasn't started yet
        if (currentTime < schoolStartTime) {
            val timeUntilStart = schoolStartTime.toSecondOfDay() - currentTime.toSecondOfDay()
            // Vibrate the phone when there are 5 minutes and 1 second left. Does not work when phone is off.
            if (timeUntilStart == 300 or 1) {
                vibrate("long")
            }
            return formatTimeDuration(timeUntilStart) + " until school starts"
        }

        // Determine the current period
        for (period in schedule) {
            if (currentTime in period.start..period.end) { // Check if the current time is in the period
                val timeLeft = period.end.toSecondOfDay() - currentTime.toSecondOfDay()
                startTime = (period.start.toSecondOfDay()-currentTime.toSecondOfDay()).toLong()
                endTime = timeLeft.toLong()
                className = period.name
                // Vibrate the phone when there's a minute or 1 second to go. Does not work when phone is off.
                if (timeLeft == 60 or 1) {
                    vibrate("long")
                }
                currentPeriod = schedule.indexOf(period)
                return formatTimeDuration(timeLeft) + " left in ${period.name}"
            }
        }

        // If no period is active, return a default message
        return "No active period"
    }

    // Format the time. Take in a duration in seconds and output a time in "HH:mm:ss" or "mm:ss"
    private fun formatTimeDuration(seconds: Int): String {
        var hours = (seconds / 3600).toString()
        var minutes = ((seconds % 3600) / 60).toString()
        var remainingSeconds = (seconds % 60).toString()
        if (hours.length == 1) {
            hours = "0$hours"
        }
        if (minutes.length == 1) {
            minutes = "0$minutes"
        }
        if (remainingSeconds.length == 1) {
            remainingSeconds = "0$remainingSeconds"
        }
        return if (hours == "00") {
            "$minutes:$remainingSeconds"
        } else {
            "$hours:$minutes:$remainingSeconds"
        }
    }
}