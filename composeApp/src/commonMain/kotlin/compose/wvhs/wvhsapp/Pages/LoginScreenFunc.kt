package compose.wvhs.wvhsapp.Pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import compose.wvhs.wvhsapp.ViewModels.StudentSharedViewModel
import compose.wvhs.wvhsapp.StudentVUE
import compose.wvhs.wvhsapp.Utils.provideKVault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreenFunc(navController: NavController, studentSharedViewModel: StudentSharedViewModel) {
    // Create initial variables
    val scope = rememberCoroutineScope()
    var isLoggedIn by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var initialLoad by remember { mutableStateOf(true) }
    var savePassword by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Initialize the studentSharedViewModel store
    if (studentSharedViewModel.store == null){
        studentSharedViewModel.setViewmodelStore(provideKVault())
    }

    // When the app is first opened
    if (initialLoad) {
        if (
            studentSharedViewModel.store?.existsObject("username") == true &&
            studentSharedViewModel.store?.existsObject("password") == true &&
            !isLoggedIn
        ) {
            savePassword = true
            isLoading = true
            username = studentSharedViewModel.store?.string("username")!!
            password = studentSharedViewModel.store?.string("password")!!
            scope.launch {
                val student = StudentVUE(username, password,"sis.powayusd.com")
                val response = withContext(Dispatchers.IO) { student.login() }
                isLoggedIn = response == "Login Successful!"
                if (isLoggedIn) {
                    studentSharedViewModel.changeStudent(StudentVUE(username, password, "sis.powayusd.com"))
                    navController.navigate(ScheduleScreen)
                } else {
                    errorMessage = "Failed to automatically log in"
                    showDialog = true
                    initialLoad = false
                    studentSharedViewModel.store?.deleteObject("username")
                    studentSharedViewModel.store?.deleteObject("password")
                }
                isLoading = false
            }
        }
        initialLoad = false
    }
    // Create main column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .imePadding()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { keyboardController?.hide() }
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Username text field
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
        )

        // Spacer
        Spacer(modifier = Modifier.padding(10.dp))

        // Password text field
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Save password checkbox
        Row (verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = savePassword, onCheckedChange = { savePassword = it }, )
            Text("Stay logged in")
        }

        // Button to submit
        Button(onClick = {
            // Launch a coroutine to check for password
            keyboardController?.hide()
            isLoading = true
            val student = StudentVUE(username, password, "sis.powayusd.com")
            scope.launch {
                val response = withContext(Dispatchers.IO) { student.login() }
                isLoggedIn = response == "Login Successful!"
                if (isLoggedIn) {
                    studentSharedViewModel.changeStudent(StudentVUE(username, password, "sis.powayusd.com"))
                    if (savePassword) {
                        studentSharedViewModel.store?.set(key = "username", stringValue = username)
                        studentSharedViewModel.store?.set(key = "password", stringValue = password)
                    }
                    navController.navigate(ScheduleScreen)
                } else {
                    showDialog = true
                    errorMessage = when (response) {
                        "Something went wrong." -> "Something went wrong."
                        else -> "Incorrect password"
                    }
                }
                isLoading = false
            }
        }) {
            if (isLoading) {
                Text("Loading...")
            } else {
                Text("Submit")
            }
        }

        TextButton(
            onClick = {
                navController.navigate(ScheduleScreen)
            }
        ) {
            Text("Continue without login")
        }

        // Show dialog if there's an error
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
