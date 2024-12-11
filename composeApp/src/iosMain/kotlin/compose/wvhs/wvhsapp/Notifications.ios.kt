package compose.wvhs.wvhsapp

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationSound.Companion.defaultCriticalSound


private class IOSNotificationService: NotificationService {
    override fun scheduleNotification(title: String, message: String, timeInMillis: Long) {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted, error ->
            if (granted) {
                val content = UNMutableNotificationContent().apply {
                    setTitle(title)
                    setBody(message)
                    setSound(defaultCriticalSound)
                }
                try {
                    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInMillis.toDouble()/1000, false)
                    val request = UNNotificationRequest.requestWithIdentifier(identifier = title, content = content, trigger = trigger)

                    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request, null)
                } catch(e: Exception) {
                    println(e)
                }
            } else {
                println(error)
            }
        }
    }

    override fun clearNotifications() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
        UNUserNotificationCenter.currentNotificationCenter().removeAllDeliveredNotifications()
    }
}

actual fun createNotificationService(): NotificationService {
    return IOSNotificationService()
}