package compose.wvhs.wvhsapp.Pages

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
import compose.wvhs.wvhsapp.Utils.format24htoAmPm
import compose.wvhs.wvhsapp.ViewModels.ScheduleViewModel
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import compose.wvhs.wvhsapp.getSchedule
import compose.wvhs.wvhsapp.Utils.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val listOfDays = listOf("MonFri Bell", "Tues-Thurs Bell", "Wed Bell")

    LaunchedEffect(className) {
        if (className != "" && studentSharedViewModel.settings?.getBoolean("useLiveActivities", false)==true) {
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
            studentSharedViewModel = studentSharedViewModel
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
                                    selectedDayPage = if (listOfDays.indexOf(selectedDayPage) != index) {
                                        listOfDays[index]
                                    } else {
                                        null
                                    }
                                },
                                selected = index == listOfDays.indexOf(selectedDayPage)
                            ) {
                                Text(label)
                            }
                        }
                    }
                    if (studentSharedViewModel.currentBellScheduleType != listOfDays[0]
                        && studentSharedViewModel.currentBellScheduleType != listOfDays[1]
                        && studentSharedViewModel.currentBellScheduleType != listOfDays[2]
                        && studentSharedViewModel.currentBellScheduleType != null
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            SegmentedButton(
                                shape = RoundedCornerShape(30.dp),
                                onClick = { selectedDayPage = studentSharedViewModel.currentBellScheduleType },
                                selected = studentSharedViewModel.currentBellScheduleType == selectedDayPage
                            ) {
                                Text(
                                    when (studentSharedViewModel.currentBellScheduleType) {
                                        else -> studentSharedViewModel.currentBellScheduleType?:"Today"
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
                        getSchedule(classList = classList, dayOfWeek = listOfDays.indexOf(selectedDayPage), studentSharedViewModel = studentSharedViewModel)
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
                                    text = "Log in to view schedule and accurate bell schedule",
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