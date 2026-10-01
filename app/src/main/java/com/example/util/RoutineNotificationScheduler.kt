package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.db.AppDatabase
import com.example.data.db.TaskEntity
import com.example.data.preferences.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs

object RoutineNotificationScheduler {

    const val MORNING_SUMMARY_REQUEST_CODE = 99901

    fun getReminderRequestCode(taskId: String, dateStr: String): Int {
        return abs(("REMINDER_${taskId}_${dateStr}").hashCode())
    }

    fun getUpcomingRequestCode(taskId: String, dateStr: String): Int {
        return abs(("UPCOMING_${taskId}_${dateStr}").hashCode())
    }

    fun scheduleNotifications(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsManager = SettingsManager(context)
                val isMasterEnabled = settingsManager.notificationsEnabledFlow.first()
                if (!isMasterEnabled) {
                    cancelAll(context)
                    return@launch
                }

                val taskRemindersEnabled = settingsManager.taskRemindersEnabledFlow.first()
                val upcomingTaskEnabled = settingsManager.upcomingTaskEnabledFlow.first()
                val morningSummaryEnabled = settingsManager.morningSummaryEnabledFlow.first()
                val reminderTimingMinutes = settingsManager.reminderTimingMinutesFlow.first()
                val morningSummaryTime = settingsManager.morningSummaryTimeFlow.first()

                val db = AppDatabase.getDatabase(context)
                val dao = db.routineDao()
                val allTasks = dao.getAllTasks().first()
                val allCompletions = dao.getAllCompletions().first()

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return@launch
                val currentLogicalDate = RoutineTimeEngine.getLogicalDate()
                val nowMillis = System.currentTimeMillis()
                val zoneId = ZoneId.systemDefault()

                // Schedule task reminders & upcoming alerts for rolling 7-day window
                for (dayOffset in 0..6) {
                    val targetLogicalDate = currentLogicalDate.plusDays(dayOffset.toLong())
                    val dateStr = targetLogicalDate.toString()
                    val dayOfWeekName = targetLogicalDate.dayOfWeek.name

                    val tasksForDay = allTasks.filter { task ->
                        task.isEnabled && task.isEffectiveOn(dateStr) && task.repeatsOn(dayOfWeekName)
                    }

                    val completedTaskIds = allCompletions
                        .filter { it.date == dateStr && it.completed }
                        .map { it.taskId }
                        .toSet()

                    for (task in tasksForDay) {
                        if (task.id in completedTaskIds) continue

                        val startLogicalMins = RoutineTimeEngine.toLogicalMinutes(task.startTime)
                        val startDateTime = RoutineTimeEngine.getTaskStartLocalDateTime(targetLogicalDate, startLogicalMins)
                        val startMillis = startDateTime.atZone(zoneId).toInstant().toEpochMilli()

                        // A. Task Reminder
                        if (taskRemindersEnabled && task.reminderEnabled) {
                            val reminderMillis = startMillis - (reminderTimingMinutes * 60 * 1000L)
                            if (reminderMillis > nowMillis) {
                                val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
                                    action = RoutineNotificationReceiver.ACTION_TASK_REMINDER
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_ID, task.id)
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_TITLE, task.title)
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_START_TIME, task.startTime)
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_CATEGORY, task.category)
                                    putExtra(RoutineNotificationReceiver.EXTRA_MINUTES_BEFORE, reminderTimingMinutes)
                                    putExtra(RoutineNotificationReceiver.EXTRA_DATE, dateStr)
                                }
                                val pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    getReminderRequestCode(task.id, dateStr),
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                setAlarmSafely(alarmManager, reminderMillis, pendingIntent)
                            }
                        }

                        // B. Upcoming Task Notification
                        if (upcomingTaskEnabled) {
                            val upcomingOffsetMinutes = (reminderTimingMinutes + 20).coerceAtLeast(30)
                            val upcomingMillis = startMillis - (upcomingOffsetMinutes * 60 * 1000L)
                            if (upcomingMillis > nowMillis) {
                                val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
                                    action = RoutineNotificationReceiver.ACTION_UPCOMING_TASK
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_ID, task.id)
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_TITLE, task.title)
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_START_TIME, task.startTime)
                                    putExtra(RoutineNotificationReceiver.EXTRA_TASK_CATEGORY, task.category)
                                    putExtra(RoutineNotificationReceiver.EXTRA_DATE, dateStr)
                                }
                                val pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    getUpcomingRequestCode(task.id, dateStr),
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                                setAlarmSafely(alarmManager, upcomingMillis, pendingIntent)
                            }
                        }
                    }
                }

                // C. Morning Summary
                if (morningSummaryEnabled) {
                    scheduleMorningSummary(context, morningSummaryTime)
                } else {
                    cancelMorningSummary(context)
                }

            } catch (e: Exception) {
                // Graceful failure
            }
        }
    }

    fun scheduleMorningSummary(context: Context, preferredTimeStr: String = "08:00") {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val zoneId = ZoneId.systemDefault()
            val now = LocalDateTime.now()

            val parts = preferredTimeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            var targetDateTime = now.toLocalDate().atTime(hour, minute)
            if (targetDateTime.isBefore(now) || targetDateTime.isEqual(now)) {
                // Already passed for today, schedule for tomorrow morning
                targetDateTime = targetDateTime.plusDays(1)
            }

            val triggerMillis = targetDateTime.atZone(zoneId).toInstant().toEpochMilli()

            val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
                action = RoutineNotificationReceiver.ACTION_MORNING_SUMMARY
                putExtra(RoutineNotificationReceiver.EXTRA_DATE, targetDateTime.toLocalDate().toString())
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                MORNING_SUMMARY_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setAlarmSafely(alarmManager, triggerMillis, pendingIntent)
        } catch (e: Exception) {
            // Graceful failure
        }
    }

    fun cancelTaskReminder(context: Context, taskId: String, dateStr: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val reminderIntent = Intent(context, RoutineNotificationReceiver::class.java).apply {
                action = RoutineNotificationReceiver.ACTION_TASK_REMINDER
            }
            val reminderPending = PendingIntent.getBroadcast(
                context,
                getReminderRequestCode(taskId, dateStr),
                reminderIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            reminderPending?.let {
                alarmManager.cancel(it)
                it.cancel()
            }

            val upcomingIntent = Intent(context, RoutineNotificationReceiver::class.java).apply {
                action = RoutineNotificationReceiver.ACTION_UPCOMING_TASK
            }
            val upcomingPending = PendingIntent.getBroadcast(
                context,
                getUpcomingRequestCode(taskId, dateStr),
                upcomingIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            upcomingPending?.let {
                alarmManager.cancel(it)
                it.cancel()
            }

            // Also dismiss from system tray if currently shown
            NotificationHelper.cancelTaskNotification(context, taskId)
        } catch (e: Exception) {
            // Graceful failure
        }
    }

    fun cancelMorningSummary(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, RoutineNotificationReceiver::class.java).apply {
                action = RoutineNotificationReceiver.ACTION_MORNING_SUMMARY
            }
            val pending = PendingIntent.getBroadcast(
                context,
                MORNING_SUMMARY_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pending?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        } catch (e: Exception) {
            // Graceful failure
        }
    }

    fun cancelAll(context: Context) {
        cancelMorningSummary(context)
    }

    private fun setAlarmSafely(alarmManager: AlarmManager, triggerMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            // Fallback if SCHEDULE_EXACT_ALARM is not granted on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
