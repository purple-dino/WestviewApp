package compose.wvhs.wvhsapp.Utils

import compose.wvhs.wvhsapp.ViewModels.ScheduleViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface NotificationService {
    fun scheduleNotification(title: String, message: String, timeInMillis: Long)
    fun clearNotifications()
}

expect fun createNotificationService(): NotificationService

fun createNotifications(schoolSchedule: List<ScheduleViewModel.Period>) {
    val notifications = createNotificationService()
    notifications.clearNotifications()
    val currentTime =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    val schoolStartTime = schoolSchedule[0].start

    // Create the start of school
    if (schoolStartTime.toSecondOfDay() - currentTime.toSecondOfDay().toLong() > 0) {
        // Creates the notification for 5 minutes until school starts
        if (schoolStartTime.toSecondOfDay() - currentTime.toSecondOfDay().toLong() > 600) {
            notifications.scheduleNotification(
                title = "School starts soon!",
                message = "5 minutes left",
                timeInMillis = ((schoolStartTime.toSecondOfDay() - currentTime.toSecondOfDay() - 600) * 1000).toLong()
            )
        }

        // Create the notification for when school starts
        else {
            notifications.scheduleNotification(
                title = "School's started!",
                message = "Time to go to ${schoolSchedule[0].name}.",
                timeInMillis = ((schoolStartTime.toSecondOfDay() - currentTime.toSecondOfDay()) * 1000).toLong()
            )
        }
    }

    // Creates the notifications for each period
    for (period in schoolSchedule) {
        // Create the notifications for 2 minutes before for classes and 1 minute before for passing
        if (period.name == "Passing") {
            if (period.end.toSecondOfDay() - currentTime.toSecondOfDay().toLong() > 60) {
                notifications.scheduleNotification(
                    title = period.name + " ends soon!",
                    message = "1 minute left",
                    timeInMillis = ((period.end.toSecondOfDay() - currentTime.toSecondOfDay() - 60) * 1000).toLong()
                )
            }
        } else {
            if (period.end.toSecondOfDay() - currentTime.toSecondOfDay().toLong() > 120) {
                notifications.scheduleNotification(
                    title = period.name + " ends soon!",
                    message = "2 minutes left",
                    timeInMillis = ((period.end.toSecondOfDay() - currentTime.toSecondOfDay() - 120) * 1000).toLong()
                )
            }
        }

        // Create the  notifications for when the period ends
        if (period.end.toSecondOfDay() - currentTime.toSecondOfDay().toLong() > 0) {
            notifications.scheduleNotification(
                title = period.name + " is over!",
                // Gives the next period that's not passing to the user, otherwise says, "have a good day"
                message = if (schoolSchedule.indexOf(period) == schoolSchedule.size - 1) {
                    "Have a good day!"
                } else {
                    "${
                        if (schoolSchedule[schoolSchedule.indexOf(period) + 1].name != "Passing") {
                            schoolSchedule[schoolSchedule.indexOf(period) + 1].name
                        } else {
                            schoolSchedule[schoolSchedule.indexOf(period) + 2].name

                        }
                    } is up next."

                },
                timeInMillis = ((period.end.toSecondOfDay() - currentTime.toSecondOfDay()) * 1000).toLong()
            )
        }
    }
}

expect fun startActivity()