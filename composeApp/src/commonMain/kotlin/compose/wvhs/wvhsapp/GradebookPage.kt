package compose.wvhs.wvhsapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Gradebook page function
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradebookPage(navController: NavController, studentSharedViewModel: StudentSharedViewModel) {
    // Create initial variables
    var isLoading by remember { mutableStateOf(true) } // Variable to see whether the program is loading in info
    val scope = rememberCoroutineScope() // Create a coroutine
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    var isExpanded by remember { mutableStateOf(false) }
    var showProgressIndicatorWithoutTimer by remember { mutableStateOf(false) }

    // Student information
    var selectedGradingPeriodName by
        mutableStateOf(studentSharedViewModel.selectedGradingPeriod?.let { studentSharedViewModel.gradingPeriods?.get(it) })


    // Get the gradebook and grading periods in a coroutine
    LaunchedEffect(Unit) {
        // Get the gradebook
        if (studentSharedViewModel.gradebook == null) {
            try {
                studentSharedViewModel.student?.requestOrUpdateGradebook(studentSharedViewModel = studentSharedViewModel)
                studentSharedViewModel.student?.requestAttendance(studentSharedViewModel = studentSharedViewModel)
            } finally {

            }
        }

        // Get the grading periods
        if (studentSharedViewModel.gradingPeriods == null) {
            try {
                studentSharedViewModel.student?.requestGradingPeriods(studentSharedViewModel = studentSharedViewModel)
            } finally {

            }
        }
        // Set isLoading to false after initial loading finishes
        isLoading = false
    }

    if (pullRefreshState.distanceFraction > 1f && !isRefreshing) {
        isRefreshing = true
        showProgressIndicatorWithoutTimer = true
        scope.launch {
            pullRefreshState.snapTo(1f)
        }
    }

    // If the page is refreshed, vibrate the phone
    if (isRefreshing) {
        scope.launch {
            vibrate("short")
            try {
                studentSharedViewModel.student?.requestOrUpdateGradebook(gradingPeriod = studentSharedViewModel.selectedGradingPeriod, studentSharedViewModel = studentSharedViewModel)
                studentSharedViewModel.student?.requestAttendance(studentSharedViewModel = studentSharedViewModel)
            } finally {
                delay(1000)
                isRefreshing = false
                delay(1000)
                showProgressIndicatorWithoutTimer = false
            }
        }
    }

    // Main data
    Scaffold (
        // The top bar with the selected grading period
        topBar = {
            // Create the top bar
            CenterAlignedTopAppBar(
                // Set the title
                title = {
                    // Create the dropdown menu large box
                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded }
                    ) {
                        // Create the default value box that is clickable to show the dropdown menu
                        selectedGradingPeriodName?.let { name ->
                            DisableSelection { // Make sure the user can't select the text field
                                TextField(
                                    value =
                                        if (isLoading) {
                                            "Loading..."
                                        } else {
                                            "Grades for $name"
                                        },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable).focusProperties { canFocus = false },
                                    colors = TextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        focusedTrailingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 15.sp, textAlign = TextAlign.Center)
                                )
                            }
                        }

                        // The actual dropdown
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = {isExpanded = false},

                        ) {
                            if (!isLoading) {
                                // Iterate through the grading periods
                                studentSharedViewModel.gradingPeriods?.forEachIndexed { indexOfPeriod, nameOfPeriod ->
                                    // Create an item for the grading period
                                    DropdownMenuItem(
                                        text = { Text(text = nameOfPeriod) },
                                        onClick = {
                                            isLoading = true // Make sure the page is loading
                                            // Set the selected gradebook period
                                            studentSharedViewModel.changeSelectedGradingPeriod(indexOfPeriod)
                                            selectedGradingPeriodName =
                                                studentSharedViewModel.gradingPeriods!![indexOfPeriod]

                                            // Update the gradebook with the new information
                                            scope.launch {
                                                studentSharedViewModel.student?.requestOrUpdateGradebook(
                                                    gradingPeriod = studentSharedViewModel.selectedGradingPeriod,
                                                    studentSharedViewModel = studentSharedViewModel
                                                )
                                                isLoading = false
                                            }
                                            isExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }
            )
    }) { innerPadding ->

        // Gradebook Data
        // Check if loading, then have a circular progress indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(50.dp)
                )
            }
        }
        else if (studentSharedViewModel.student == null) {
            TextButton(
                onClick = {navController.navigate(LoginScreen)},
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Log in to view grades",
                    textAlign = TextAlign.Center
                )
            }
        }
        // Otherwise have the actual gradebook
        else {
            // Safe call to gradebook
            studentSharedViewModel.gradebook?.let { safeGradebook ->
                // Create the scrollable column with pull to refresh
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(15.dp)
                        .pullToRefresh(
                            isRefreshing = isRefreshing,
                            state = pullRefreshState,
                            onRefresh = {
                                isRefreshing = true
                            }
                        ),
                    userScrollEnabled = !isRefreshing
                ) {
                    // Have the loading indicator for refreshing
                    item {
                        Box(
                            modifier = Modifier.animateContentSize()
                        ) {
                            AnimatedVisibility(
                                visible = isRefreshing || pullRefreshState.distanceFraction > 0,
                                enter = slideInVertically() + fadeIn(),
                                exit = slideOutVertically() + fadeOut()
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(30.dp)) {
                                    if (isRefreshing || showProgressIndicatorWithoutTimer) {
                                        CircularProgressIndicator(
                                            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                                            modifier = Modifier.align(Alignment.Center).size(50.dp)
                                        )
                                    } else if (!showProgressIndicatorWithoutTimer){
                                        CircularProgressIndicator(
                                            progress = {pullRefreshState.distanceFraction},
                                            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                                            modifier = Modifier.align(Alignment.Center).size(50.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Create the gradebook classes entries
                    if (studentSharedViewModel.gradebook?.classes?.entries?.toList()?.isNotEmpty() == true) {
                        items(safeGradebook.classes.entries.toList()) { classEntry ->
                            classEntry.key?.let { ClassCard(it, classEntry.value, navController) }
                        }
                    }
                    // Otherwise say that the data couldn't be found
                    else {
                        item {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(30.dp)) {
                                Text(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                                    text = "Gradebook Data Not Found",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } ?:
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(15.dp)
                    .pullToRefresh(
                        isRefreshing = isRefreshing,
                        state = pullRefreshState,
                        onRefresh = {
                            isRefreshing = true
                            scope.launch {
                                try {
                                    studentSharedViewModel.student?.requestOrUpdateGradebook(gradingPeriod = studentSharedViewModel.selectedGradingPeriod, studentSharedViewModel = studentSharedViewModel)
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        }
                    )
            ) {
                // Have the loading indicator for refreshing
                item {
                    Box(modifier = Modifier.animateContentSize()) {
                        AnimatedVisibility(
                            visible = isRefreshing,
                            enter = slideInVertically(),
                            exit = slideOutVertically()
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(30.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center).size(50.dp)
                                )
                            }
                        }
                    }
                }

                // Have the gradebook data not found
                item {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(30.dp)
                    ) {
                        Text(
                            modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                            text = "Gradebook Data Not Found",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Class card function
@Composable
fun ClassCard(className: String, classInfo: ClassInfo, navController: NavController) {
    // Access the colors from the MaterialTheme
    val backgroundColor = MaterialTheme.colorScheme.secondaryContainer // Lightened primary color for background
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer // Text color for better contrast

    // Main Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = {
                navController.navigate(DetailedGradesScreen(className))
            }),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column one: Period/class name and teacher
            Column(
                modifier = Modifier.weight(0.7F)
            ) {
                Text(text = "${classInfo.period}: $className", fontSize = 15.sp, color = textColor) // Period number: Class name
                classInfo.teacher?.let { Text(text = it, fontSize = 12.sp, color = textColor) } // Teacher
            }
            // Column two: letter grade and percentage
            Column(
                modifier = Modifier.weight(0.3F),
                horizontalAlignment = Alignment.End
            ) {
                classInfo.overallLetterGrade?.let { Text(text = it, fontSize = 18.sp, color = textColor) }
                classInfo.overallPercentageGrade?.let { Text(text = it, fontSize = 12.sp, color = textColor) }
            }
        }
    }
}