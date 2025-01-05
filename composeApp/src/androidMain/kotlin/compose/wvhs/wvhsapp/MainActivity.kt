package compose.wvhs.wvhsapp

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import compose.wvhs.wvhsapp.Pages.App
import compose.wvhs.wvhsapp.Utils.AndroidContextProvider
import compose.wvhs.wvhsapp.Utils.ContextHolder
import compose.wvhs.wvhsapp.Utils.initializeContext

class MainActivity : ComponentActivity() {
    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "5",
                "High priority notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }

        isDarkMode = resources.configuration.isNightModeActive
        enableEdgeToEdge(transparentBarStyle, transparentBarStyle)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Initialize the ContextHolder with the AndroidContextProvider
        ContextHolder.contextProvider = AndroidContextProvider(this)
        initializeContext(this)
        setContent {
            App()
        }
    }

    private var isDarkMode = false
    private val transparentBarStyle = SystemBarStyle.auto(
        lightScrim = Color.Transparent.value.toInt(),
        darkScrim = Color.Transparent.value.toInt(),
        detectDarkMode = { isDarkMode },
    )

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isDarkMode != newConfig.isNightModeActive) {
            isDarkMode = newConfig.isNightModeActive
            enableEdgeToEdge(transparentBarStyle, transparentBarStyle)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}