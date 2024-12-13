package compose.wvhs.wvhsapp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

var startTime: Long = 0
var endTime: Long = 0
var className by mutableStateOf("")

// Main function for schedule screen
@Composable
fun SchedulePageFunc(studentSharedViewModel: StudentSharedViewModel, navController: NavController) {
    // Declare initial variables
    var isLoading by remember { mutableStateOf(true) } // Create variable to see if data is loading
    val scope = rememberCoroutineScope() // create a coroutine
    var selectedDayPage by mutableStateOf(studentSharedViewModel.currentBellScheduleType)
    val options = listOf("Mon/Fri", "Tue/Thu", "Wed")
    val listState = rememberLazyListState()

    LaunchedEffect(className) {
        if (className != "") {
            startActivity()
        }

    }

    // Create a coroutine to load in classes
    scope.launch {
        // Only load in classes if classes haven't been loaded yet
        if (studentSharedViewModel.classes == null) {
            withContext(Dispatchers.IO) {
                studentSharedViewModel.student?.requestClassListAndBellSchedule(
                    studentSharedViewModel
                )
            }?.let {
                studentSharedViewModel.setClassList(
                    it
                )
            }
        }
        // Set loading state to false when classes load
        withContext(Dispatchers.Main) {
            isLoading = false
        }
    }

    // Loading screen
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
            )
        }
    }

    // Create the actual timer
    else {
        // Ensure classes are not null before passing to TimerScreenFunc
        val classList = studentSharedViewModel.classes?.classes ?: emptyList()
        val viewModel = ScheduleViewModel(
            classList,
            studentSharedViewModel.currentBellScheduleType
        ) // Pass the class list to the ViewModel

        Scaffold(
            topBar = { TimerPageTopBar(viewModel) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                state = listState
            ) {
                // Create the selector for which schedule to show (Monday/Friday, etc)
                item {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    ) {
                        options.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = options.size
                                ),
                                onClick = {
                                    selectedDayPage = if (selectedDayPage != index) {
                                        index
                                    } else {
                                        null
                                    }
                                },
                                selected = index == selectedDayPage
                            ) {
                                Text(label)
                            }
                        }
                    }
                    if (studentSharedViewModel.currentBellScheduleType != Days.MondayFriday.value
                        && studentSharedViewModel.currentBellScheduleType != Days.TuesdayThursday.value
                        && studentSharedViewModel.currentBellScheduleType != Days.Wednesday.value
                        && studentSharedViewModel.currentBellScheduleType != null
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            SegmentedButton(
                                shape = RoundedCornerShape(30.dp),
                                onClick = { selectedDayPage = 3 },
                                selected = studentSharedViewModel.currentBellScheduleType == selectedDayPage
                            ) {
                                Text(
                                    when (studentSharedViewModel.currentBellScheduleType) {
                                        Days.ExtendedHomeroom.value -> "Extended Homeroom"
                                        Days.RallySchedule.value -> "Rally Schedule"
                                        Days.FirstDayOfTerm.value -> "First Day of School"
                                        Days.StateTesting.value -> "State Testing"
                                        Days.MinimumDay.value -> "Minimum Day"
                                        Days.Finals.value -> "Finals"
                                        else -> "Today"
                                    }
                                )
                            }
                        }
                    }
                }

                // Current day of week
                if (selectedDayPage == studentSharedViewModel.currentBellScheduleType && studentSharedViewModel.currentBellScheduleType != null) {
                    // Create the class list
                    items(viewModel.schoolSchedule.toList()) { period ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                .background(
                                    if (viewModel.schoolSchedule.indexOf(period) == viewModel.currentPeriod) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    }, shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row {
                                // Period name
                                Text(
                                    period.name,
                                    modifier = Modifier
                                        .weight(0.6F)
                                        .padding(10.dp),
                                    color =
                                    if (viewModel.schoolSchedule.indexOf(period) == viewModel.currentPeriod) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )

                                // Period start
                                Text(
                                    format24htoAmPm(period.start),
                                    modifier = Modifier
                                        .weight(0.2F)
                                        .padding(10.dp), color =
                                    if (viewModel.schoolSchedule.indexOf(period) == viewModel.currentPeriod) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )

                                // Period end
                                Text(
                                    format24htoAmPm(period.end),
                                    modifier = Modifier
                                        .weight(0.2F)
                                        .padding(10.dp),
                                    color =
                                    if (viewModel.schoolSchedule.indexOf(period) == viewModel.currentPeriod) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )
                            }
                        }
                    }

                    scope.launch {
                        listState.animateScrollToItem(viewModel.currentPeriod + 1)
                    }
                }

                // Non-current days of week
                else if (selectedDayPage != studentSharedViewModel.currentBellScheduleType && selectedDayPage != null){
                    items(
                        getSchedule(
                            classList = classList,
                            dayOfWeek = selectedDayPage
                        )
                    ) { period ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                .background(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row {
                                // Period name
                                Text(
                                    period.name,
                                    modifier = Modifier
                                        .weight(0.6F)
                                        .padding(10.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                // Period start
                                Text(
                                    format24htoAmPm(period.start),
                                    modifier = Modifier
                                        .weight(0.2F)
                                        .padding(10.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                // Period end
                                Text(
                                    format24htoAmPm(period.end),
                                    modifier = Modifier
                                        .weight(0.2F)
                                        .padding(10.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                else {
                    if (studentSharedViewModel.classes == null) {
                        item {
                            TextButton(
                                onClick = {navController.navigate(LoginScreen)},
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Log in to view schedule",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(studentSharedViewModel.classes!!.classes) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                    .background(
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Row {
                                    // Period name
                                    Text(
                                        it,
                                        modifier = Modifier
                                            .weight(0.6F)
                                            .padding(10.dp),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerPageTopBar(viewModel: ScheduleViewModel) {
    val remainingTime by viewModel.remainingTime.collectAsState() // Collect the state as a state variable

    // Display the remaining time
    CenterAlignedTopAppBar(title = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp)
                )
                .animateContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = remainingTime,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp),
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    })
}


// the schedule time remaining
class ScheduleViewModel(classList: List<String>?, dayOfWeek: Int?) {
    // Create initial variables
    data class Period(val name: String, val start: LocalTime, val end: LocalTime)

    private val _remainingTime = MutableStateFlow("")
    val remainingTime = _remainingTime.asStateFlow()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Create the schedule
    val schoolSchedule = getSchedule(classList, dayOfWeek = dayOfWeek)

    var currentPeriod by mutableStateOf(-1)

    // Init function
    init {
        // Start the actual timer
        startUpdating()

        // Create the notifications
        if (schoolSchedule.isNotEmpty()) {
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