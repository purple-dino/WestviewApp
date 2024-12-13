package compose.wvhs.wvhsapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private fun createNotificationChannel(context: Context, channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Notifications", NotificationManager.IMPORTANCE_HIGH)
        channel.enableLights(true)
        channel.enableVibration(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel("Notifications") == null) {
            notificationManager.createNotificationChannel(channel)
        }
    }
}

private class AndroidNotificationService(private val context: Context): NotificationService {
    @SuppressLint("ScheduleExactAlarm")
    override fun scheduleNotification(title: String, message: String, timeInMillis: Long) {
        val channelId = "notification_channel_$timeInMillis" // Unique channel ID
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationID", timeInMillis.toInt())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, intent.getIntExtra("notificationID", timeInMillis.toInt()), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+timeInMillis, pendingIntent)

        createNotificationChannel(context, channelId) // Create channel for this notification
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun clearNotifications() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancelAll()
    }
}

// Don't forget to create a BroadcastReceiver to handle the notification
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NotificationReceiver::WakeLock").apply {
                acquire(3000) // 30 seconds
            }
        }

        val title = intent.getStringExtra("title") ?: "Default Title"
        val message = intent.getStringExtra("message") ?: "Default Message"

        val notificationBuilder = NotificationCompat.Builder(context, intent.getStringExtra("notificationId") ?: "Notifications")
            .setSmallIcon(R.mipmap.wv)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(intent.getIntExtra("notificationID", 0), notificationBuilder.build())
        }

        wakeLock.release()
    }
}

actual fun createNotificationService(): NotificationService{
    return AndroidNotificationService(ContextHolder.contextProvider.getApplicationContext() as Context)
}

actual fun startActivity() {

}