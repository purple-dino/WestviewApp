package compose.wvhs.wvhsapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsPageFunc() {
    var selectedEventPage by remember { mutableStateOf("Weekly Newsletter") }
    var selectedEventPageIndex by remember { mutableStateOf(0) }
    val options = listOf("Weekly Newsletter", "Counseling", "Westview Calendar", "Athletics Calendar")
    var isExpanded by mutableStateOf(false)

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                // Set the title
                title = {
                    // Create the dropdown menu large box
                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Create the default value box that is clickable to show the dropdown menu
                        DisableSelection { // Make sure the user can't select the text field
                            TextField(
                                value = selectedEventPage,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(fontSize = 15.sp, textAlign = TextAlign.Center),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                                modifier = Modifier.fillMaxSize().menuAnchor(type = MenuAnchorType.PrimaryNotEditable).focusProperties { canFocus = false },
                                colors = TextFieldDefaults.colors(focusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    focusedTrailingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                singleLine = true
                            )
                        }

                        // The actual dropdown
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = {isExpanded = false}
                        ) {
                            // Iterate through the grading periods
                            options.forEachIndexed { indexOfNewsletter, nameOfNewsletter ->
                                // Create an item for the grading period
                                DropdownMenuItem(
                                    text = { Text(text = nameOfNewsletter) },
                                    onClick = {
                                        // Set the selected gradebook period
                                        selectedEventPageIndex = indexOfNewsletter
                                        selectedEventPage = nameOfNewsletter
                                        isExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ){ innerPadding ->
        val padding = innerPadding.calculateTopPadding()
        Box (
            modifier = Modifier
                .padding(top = padding)
                .fillMaxSize()
        ){
            when (selectedEventPage ) {
                "Weekly Newsletter" -> DisplayWebPage("https://secure.smore.com/n/g8yvq")
                "Counseling" -> DisplayWebPage("https://westview.powayusd.com/apps/pages/newsletters")
                "Athletics Calendar" -> CalendarScreenFunc(selectedEventPage)
                "Westview Calendar" -> CalendarScreenFunc(selectedEventPage)
            }
        }
    }
}