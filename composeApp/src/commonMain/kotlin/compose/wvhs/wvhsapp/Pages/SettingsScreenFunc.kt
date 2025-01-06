package compose.wvhs.wvhsapp.Pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.russhwolf.settings.set
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import compose.wvhs.wvhsapp.Utils.createNotificationService

@Composable
fun SettingsScreenFunc(studentSharedViewModel: StudentSharedViewModel, navController: NavController){
    var useNotifications by mutableStateOf(studentSharedViewModel.settings?.getBoolean("useNotifications", false) == true)
    var useLiveActivities by mutableStateOf(studentSharedViewModel.settings?.getBoolean("useLiveActivities", false) == true)


    Scaffold {
        innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)){
            // Use Notifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", textAlign = TextAlign.Left)
                Switch(
                    checked = useNotifications,
                    onCheckedChange = {
                        studentSharedViewModel.settings?.set("useNotifications", !useNotifications)
                        if (useNotifications) {
                            val notifications = createNotificationService()
                            notifications.clearNotifications()
                        }
                        useNotifications = !useNotifications
                    }
                )
            }

            // Use Live Activities (iOS only)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Live Activities (iOS only)", textAlign = TextAlign.Left)
                Switch(
                    checked = useLiveActivities,
                    onCheckedChange = {
                        studentSharedViewModel.settings?.set("useLiveActivities", !useLiveActivities)
                        useLiveActivities = !useLiveActivities
                    }
                )
            }

            HorizontalDivider()

            // Logout
            Button(
                onClick = {
                    studentSharedViewModel.store?.deleteObject("password")
                    studentSharedViewModel.store?.deleteObject("username")
                    studentSharedViewModel.resetAllData()
                    navController.navigate(LoginScreen)
                    val notifications = createNotificationService()
                    notifications.clearNotifications()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.padding(vertical = 30.dp, horizontal = 10.dp).fillMaxWidth()
            ) {
                if (studentSharedViewModel.student != null) {
                    Text("Log Out")
                } else {
                    Text("Return to login")
                }
            }
        }
    }
}