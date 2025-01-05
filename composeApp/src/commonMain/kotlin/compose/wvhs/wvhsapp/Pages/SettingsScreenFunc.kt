package compose.wvhs.wvhsapp.Pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import compose.wvhs.wvhsapp.Utils.createNotificationService

@Composable
fun SettingsScreenFunc(studentSharedViewModel: StudentSharedViewModel, navController: NavController){
    Button(onClick = {
        studentSharedViewModel.store?.deleteObject("password")
        studentSharedViewModel.store?.deleteObject("username")
        studentSharedViewModel.resetAllData()
        navController.navigate(LoginScreen)
        val notifications = createNotificationService()
        notifications.clearNotifications()
    }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.padding(vertical = 30.dp, horizontal = 10.dp).fillMaxWidth()) {
        if (studentSharedViewModel.student != null) {
            Text("Log Out")
        } else {
            Text("Return to login")
        }
    }
}