package compose.wvhs.wvhsapp.Pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import compose.wvhs.wvhsapp.Utils.getHumanDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedGradesScreenFunc(studentSharedViewModel: StudentSharedViewModel, activeClass: String, navController: NavController) {
    // Declare initial variables
    val detailedGradebook = studentSharedViewModel.gradebook?.classes?.get(activeClass)

    // Create font and colors
    val classNameFontSize =  25.sp
    val teacherFontSize = 15.sp
    val overallLetterGradeFontSize = 50.sp
    val overallNumberGradeFontSize = 15.sp
    val testSize = 13.sp
    val classGradeBackgroundColor = MaterialTheme.colorScheme.secondaryContainer
    val classGradeLetterBackgroundColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer

    // Variables for showing/hiding the header (class name)
    val listState = rememberLazyListState()
    val showTopClassname by remember {
        derivedStateOf {
            listState.canScrollBackward
        }
    }

    // Main content
    Scaffold(
        // Top bar
        topBar = {
            CenterAlignedTopAppBar(
                title = { AnimatedVisibility(showTopClassname) {
                    Column (horizontalAlignment = Alignment.CenterHorizontally){
                        // Class title
                        Text(
                            text = activeClass,
                            fontSize = classNameFontSize,
                            fontWeight = FontWeight.Bold
                        )

                        // Teacher name
                        detailedGradebook?.teacher?.let { teacher ->
                            Text(
                                text = teacher,
                                fontSize = teacherFontSize
                            )
                        }
                    }

                }},
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(GradesScreen) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
            )
        }
    ) {
        innerPadding ->
        // Main column
        Column (
            modifier = Modifier.pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // Check if the drag amount is significant enough to consider it a swipe
                    if (dragAmount > 20) { // Adjust threshold as needed
                        navController.navigate(GradesScreen) // Navigate back
                    }
                }
            }
        ) {
            // Create the main page
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(15.dp)
            ) {
                // Class info (for example, math 1 with Mr. James)
                item {
                    // Create variable to show detail or not
                    var showDetailedOverallClassInfo by remember { mutableStateOf(false) }

                    // Top class info (overall grade, etc)
                    Box(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .background(
                                color = classGradeBackgroundColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .fillMaxWidth()
                            .clickable(onClick = { showDetailedOverallClassInfo = !showDetailedOverallClassInfo })
                    ) {
                        // Column of the info
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                        ) {
                            // Class title
                            Text(
                                text = activeClass,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = classNameFontSize,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            // Teacher name
                            detailedGradebook?.teacher?.let { teacher ->
                                Text(
                                    text = teacher,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontSize = teacherFontSize,
                                    color = textColor
                                )
                            }

                            // Spacer between title/teacher and overall grade
                            Spacer(modifier = Modifier.padding(5.dp))

                            // Create the large overall grade
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = classGradeLetterBackgroundColor,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .fillMaxWidth(0.5F)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    // Create the large letter grade
                                    Text(
                                        text = detailedGradebook?.overallLetterGrade ?: "N/A",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontSize = overallLetterGradeFontSize,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    // Create the smaller percentage grade
                                    Text(
                                        text = detailedGradebook?.overallPercentageGrade ?: "N/A",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontSize = overallNumberGradeFontSize,
                                        color = textColor
                                    )
                                }
                            }

                            // Show detailed class information (breakdown of the grade)
                            Spacer(modifier = Modifier.padding(6.dp))

                            // Only show the info if the top is clicked on
                            AnimatedVisibility(
                                visible = showDetailedOverallClassInfo
                            ) {
                                // Column (ex work, tests, etc)
                                // Also has absences
                                Column {
                                    // Spacer at top
                                    if (detailedGradebook != null) {
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.padding(6.dp))
                                    }

                                    // Detailed grade breakdown with points
                                    detailedGradebook?.gradeBreakdown?.forEach { breakdown ->
                                        // Rows of the information
                                        Row {
                                            // Type of weighting
                                            Text(
                                                text = breakdown.type ?: "",
                                                modifier = Modifier
                                                    .border(1.dp, Color.Black)
                                                    .weight(0.4F)
                                                    .padding(2.dp)
                                                    .heightIn(max = 30.dp),
                                                overflow = TextOverflow.Ellipsis, color = textColor
                                            )

                                            // Percentage of weighting
                                            Text(
                                                text = breakdown.weight.toString(),
                                                modifier = Modifier
                                                    .border(1.dp, Color.Black)
                                                    .weight(0.2F)
                                                    .padding(2.dp), color = textColor
                                            )

                                            // Points the student has
                                            Text(
                                                text = breakdown.points.toString(),
                                                modifier = Modifier
                                                    .border(1.dp, Color.Black)
                                                    .weight(0.2F)
                                                    .padding(2.dp), color = textColor
                                            )

                                            // Points possible in the class
                                            Text(
                                                text = breakdown.pointsPossible.toString(),
                                                modifier = Modifier
                                                    .border(1.dp, Color.Black)
                                                    .weight(0.2F)
                                                    .padding(2.dp), color = textColor
                                            )
                                        }
                                    }

                                    // Absences
                                    studentSharedViewModel.attendance?.absences.let { allAbsences ->
                                        if (allAbsences != null) {
                                            // Iterate through and create the info
                                            for (absence in allAbsences) {
                                                if (absence.periodsAffected?.contains(activeClass) == true) {
                                                    Text(
                                                        text = "${absence.type?.get(absence.periodsAffected.indexOf(activeClass))} on ${getHumanDate(absence.date!!)}",
                                                        color = textColor
                                                    )
                                                }
                                            }
                                            for (absence in allAbsences) {
                                                if (absence.periodsAffected?.contains(activeClass) == true) {
                                                    // Absences spacer
                                                    // Probably a better way to do this
                                                    Spacer(modifier = Modifier.padding(6.dp))
                                                    HorizontalDivider()
                                                    Spacer(modifier = Modifier.padding(6.dp))
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Iterate through assignments and create each assignment item (a button)
                detailedGradebook?.assignments?.forEach { assignment ->
                    // Assignment button
                    item {
                        // Variable to show the assignment details
                        var showAssignmentDetails by remember { mutableStateOf(false) }

                        // Create the assignment button
                        Button(
                            onClick = { showAssignmentDetails = showAssignmentDetails.not() },
                            colors = ButtonDefaults.buttonColors(containerColor = classGradeBackgroundColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Column(modifier = Modifier) {
                                // The brief view with assignment name, date, and points
                                Row(
                                    modifier = Modifier
                                        .padding(4.dp)
                                ) {
                                    // Date of assignment
                                    Text(
                                        text = assignment.date.toString(),
                                        modifier = Modifier.weight(0.2F).padding(2.dp),
                                        fontSize = testSize, color = textColor
                                    )

                                    // Title of assignment
                                    Text(
                                        text = assignment.title.toString(),
                                        modifier = Modifier.weight(0.6F).padding(2.dp),
                                        fontSize = testSize, color = textColor
                                    )

                                    // Points of the assignment (100/100)
                                    Text(
                                        text = assignment.score.toString(),
                                        modifier = Modifier.weight(0.2F).padding(2.dp),
                                        fontSize = testSize, color = textColor
                                    )
                                }

                                // Display the shortened assignment notes if there are any
                                AnimatedVisibility (
                                    visible = !showAssignmentDetails && assignment.notes.toString() != ""
                                ) {
                                    Row(
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        // Notes of assignment (shortened)
                                        Row {
                                            Text(
                                                text = "Notes: ",
                                                modifier = Modifier.padding(2.dp),
                                                fontSize = testSize, color = textColor,
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = assignment.notes.toString(),
                                                modifier = Modifier.padding(2.dp),
                                                fontSize = testSize, color = MaterialTheme.colorScheme.error,
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                // Detailed view
                                AnimatedVisibility(
                                    visible = showAssignmentDetails
                                ) {
                                    Column {
                                        // Divider bar
                                        HorizontalDivider()

                                        // Detailed info header
                                        Row(
                                            modifier = Modifier
                                                .padding(4.dp)
                                        ) {
                                            // Weighting category header
                                            Text(
                                                text = "Type",
                                                modifier = Modifier.weight(0.4F).padding(2.dp),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = testSize, color = textColor
                                            )

                                            // Score type header
                                            Text(
                                                text = "Score Type",
                                                modifier = Modifier.weight(0.3F).padding(2.dp),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = testSize, color = textColor
                                            )

                                            // Points header
                                            Text(
                                                text = "Points",
                                                modifier = Modifier.weight(0.3F).padding(2.dp),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = testSize, color = textColor
                                            )
                                        }

                                        // Detailed info
                                        Row(
                                            modifier = Modifier
                                                .padding(4.dp)
                                        ) {
                                            // Weighting type of assignment
                                            Text(
                                                text = assignment.type.toString(),
                                                modifier = Modifier.weight(0.3F).padding(2.dp),
                                                fontSize = testSize, color = textColor
                                            )

                                            // Type of assignment
                                            Text(
                                                text = assignment.scoreType.toString(),
                                                modifier = Modifier.weight(0.4F).padding(2.dp),
                                                fontSize = testSize, color = textColor
                                            )

                                            // Points of assignment
                                            Text(
                                                text = assignment.points.toString(),
                                                modifier = Modifier.weight(0.3F).padding(2.dp),
                                                fontSize = testSize, color = textColor
                                            )
                                        }

                                        // Spacer
                                        Spacer(modifier = Modifier.padding(3.dp))

                                        // Notes
                                        Row {
                                            Text(
                                                text = "Notes: ",
                                                modifier = Modifier.padding(2.dp),
                                                fontSize = testSize, color = textColor,
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = assignment.notes.toString(),
                                                modifier = Modifier.padding(2.dp),
                                                fontSize = testSize, color = MaterialTheme.colorScheme.error,
                                                overflow = TextOverflow.Ellipsis,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // End spacer
                        Spacer(modifier = Modifier.padding(7.dp))
                    }
                }
            }
        }
    }
}