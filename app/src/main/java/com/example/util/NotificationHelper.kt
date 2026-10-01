package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import kotlin.math.abs

object NotificationHelper {
    const val CHANNEL_TASK_REMINDERS = "channel_task_reminders"
    const val CHANNEL_UPCOMING_TASKS = "channel_upcoming_tasks"
    const val CHANNEL_MORNING_SUMMARY = "channel_morning_summary"

    const val MORNING_SUMMARY_NOTIF_ID = 99901

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_TASK_REMINDERS,
                "Task reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders before scheduled routine tasks"
            }

            val upcomingChannel = NotificationChannel(
                CHANNEL_UPCOMING_TASKS,
                "Upcoming tasks",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for upcoming scheduled tasks"
            }

            val morningChannel = NotificationChannel(
                CHANNEL_MORNING_SUMMARY,
                "Morning summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning overview of planned routine tasks"
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, upcomingChannel, morningChannel))
        }
    }

    private fun getLaunchPendingIntent(context: Context, taskId: String? = null): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            taskId?.let { putExtra("EXTRA_NAV_TASK_ID", it) }
        }
        val requestCode = if (taskId != null) abs(taskId.hashCode()) else 1000
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, requestCode, intent, flags)
    }

    fun getCategoryEmoji(category: String): String {
        return when (category.trim().lowercase()) {
            "study & learning", "study", "college" -> "📚"
            "work" -> "💼"
            "health & fitness", "exercise" -> "🏃"
            "projects", "project / internship" -> "💻"
            "hobbies" -> "🎨"
            "guitar" -> "🎸"
            "flute" -> "🎶"
            "reading" -> "📖"
            "family" -> "👨‍👩‍👧"
            "self growth" -> "🌱"
            "errands" -> "🛒"
            "rest" -> "☕"
            "personal" -> "🌱"
            "other" -> "⭐"
            else -> "⭐"
        }
    }

    fun getReminderNotificationId(taskId: String): Int {
        return abs(("NOTIF_REMINDER_$taskId").hashCode())
    }

    fun getUpcomingNotificationId(taskId: String): Int {
        return abs(("NOTIF_UPCOMING_$taskId").hashCode())
    }

    fun showTaskReminder(
        context: Context,
        taskId: String,
        title: String,
        startTime: String,
        category: String = "Personal",
        minutesBefore: Int = 10
    ) {
        createNotificationChannels(context)

        val emoji = getCategoryEmoji(category)
        val notifTitle = "$emoji $title"
        val notifText = if (minutesBefore <= 0) {
            "Starts now at $startTime."
        } else {
            "Starts in $minutesBefore minutes ($startTime)."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(notifTitle)
            .setContentText(notifText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getLaunchPendingIntent(context, taskId))
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(getReminderNotificationId(taskId), builder.build())
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+, fails gracefully
        }
    }

    fun showUpcomingTask(
        context: Context,
        taskId: String,
        title: String,
        startTime: String,
        category: String = "Personal"
    ) {
        createNotificationChannels(context)

        val emoji = getCategoryEmoji(category)
        val notifTitle = "🔔 Up next"
        val notifText = "$emoji $title starts at $startTime."

        val builder = NotificationCompat.Builder(context, CHANNEL_UPCOMING_TASKS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(notifTitle)
            .setContentText(notifText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getLaunchPendingIntent(context, taskId))
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(getUpcomingNotificationId(taskId), builder.build())
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+, fails gracefully
        }
    }

    fun showMorningSummary(
        context: Context,
        userName: String,
        taskTitles: List<String>,
        isFreeDay: Boolean,
        isEmptyRoutine: Boolean
    ) {
        createNotificationChannels(context)

        val notifTitle = "☀️ Good morning, $userName!"
        val notifText: String
        val bigText: String

        when {
            isEmptyRoutine -> {
                notifText = "Your routine is empty today."
                bigText = "Your routine is empty today. Tap to create tasks and make time for what matters 🌱"
            }
            isFreeDay || taskTitles.isEmpty() -> {
                notifText = "Today is a free day. Enjoy your break 🌱"
                bigText = "Today is a free day. No scheduled tasks today. Rest & enjoy your break 🌱"
            }
            else -> {
                notifText = "${taskTitles.size} tasks planned today. Have a good day 🌱"
                val taskListFormatted = taskTitles.joinToString("\n") { "• $it" }
                bigText = "Today's routine:\n$taskListFormatted\n\n${taskTitles.size} tasks planned today. Have a good day 🌱"
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_MORNING_SUMMARY)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(notifTitle)
            .setContentText(notifText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getLaunchPendingIntent(context, null))
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(MORNING_SUMMARY_NOTIF_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+, fails gracefully
        }
    }

    fun cancelTaskNotification(context: Context, taskId: String) {
        try {
            val manager = NotificationManagerCompat.from(context)
            manager.cancel(getReminderNotificationId(taskId))
            manager.cancel(getUpcomingNotificationId(taskId))
        } catch (e: Exception) {
            // Graceful failure
        }
    }

    fun showReminderNotification(context: Context, title: String, message: String, notificationId: Int = 1001) {
        createNotificationChannels(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getLaunchPendingIntent(context, null))
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+, fails gracefully
        }
    }
}
