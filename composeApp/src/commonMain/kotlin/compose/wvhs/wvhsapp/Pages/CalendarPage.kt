package compose.wvhs.wvhsapp.Pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.wvhs.wvhsapp.DataClasses.DecodedEvent
import compose.wvhs.wvhsapp.DataClasses.FinalCalendar
import compose.wvhs.wvhsapp.Utils.format24htoAmPm
import compose.wvhs.wvhsapp.Utils.getAthleticsCalendar
import compose.wvhs.wvhsapp.Utils.getHumanDate
import compose.wvhs.wvhsapp.Utils.getSchoolCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreenFunc(selectedCalendar: String) {
    // Create initial variables
    val backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
    var calendar: FinalCalendar? by remember { mutableStateOf(null) }
    var orderedCalendar: Map<LocalDate, List<DecodedEvent>>? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load in the calendar
    LaunchedEffect(Unit) {
        if (selectedCalendar == "Athletics Calendar") {
            calendar = getAthleticsCalendar()
            orderedCalendar = calendar?.events?.groupBy { it.start?.date ?: it.startDate?: LocalDate(0,0,2000)}
        } else if (selectedCalendar == "Westview Calendar") {
            calendar = getSchoolCalendar()
            orderedCalendar = calendar?.events?.groupBy { it.start?.date ?: it.startDate?: LocalDate(0,0,2000)}
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(50.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding()
        ) {
            // Iterate through each populated date in the calendar
            orderedCalendar?.forEach { (date, info) ->
                // Create the header for the date
                stickyHeader {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Text(
                            text = getHumanDate(date),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )
                    }
                }

                // Iterate through the items
                info.forEachIndexed { _, it ->
                    item (key = it.id!!){
                        var showDetail by mutableStateOf(false)
                        // Create the event
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 5.dp)
                                .clickable(onClick = { showDetail = !showDetail }),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = backgroundColor)
                        ) {
                            // Details about the event
                            Column(
                                modifier = Modifier.padding(15.dp)
                            ) {
                                // Summary/title of the event
                                Text(
                                    text = it.summary ?: "",
                                    fontSize = 17.sp,
                                    color = textColor
                                )
                                HorizontalDivider()
                                // Time of event
                                Text(
                                    text = (
                                        if (it.start?.time == null) {
                                            if (it.startDate == it.endDate) {
                                                getHumanDate(
                                                    it.startDate!!, false
                                                )
                                            } else {
                                                getHumanDate(it.startDate!!, false) + " to " + getHumanDate(it.endDate!!, false)
                                            }
                                        } else {
                                            if (it.start.time != it.end?.time) {
                                                "${format24htoAmPm(it.start.time, true)} - ${
                                                    format24htoAmPm(
                                                        it.end?.time ?: LocalTime(0, 0),
                                                        true
                                                    )
                                                }"
                                            } else {
                                                format24htoAmPm(it.start.time, true)
                                            }
                                        }
                                    ),
                                    textAlign = TextAlign.Left
                                )
                                if (it.location != null) {
                                    // Location of event
                                    Text(
                                        text = it.location.toString(),
                                        textAlign = TextAlign.Left
                                    )
                                } else {
                                    Text(
                                        text = "No location specified",
                                        textAlign = TextAlign.Left
                                    )
                                }
                                if (it.description != null) {
                                    AnimatedVisibility(visible = showDetail) {
                                        HorizontalDivider()
                                        Text(
                                            text = it.description.toString(),
                                            textAlign = TextAlign.Left
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
}