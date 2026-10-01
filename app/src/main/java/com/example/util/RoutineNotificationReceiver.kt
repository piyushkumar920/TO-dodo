package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.preferences.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class RoutineNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TASK_REMINDER = "com.example.ACTION_TASK_REMINDER"
        const val ACTION_UPCOMING_TASK = "com.example.ACTION_UPCOMING_TASK"
        const val ACTION_MORNING_SUMMARY = "com.example.ACTION_MORNING_SUMMARY"

        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        const val EXTRA_TASK_START_TIME = "EXTRA_TASK_START_TIME"
        const val EXTRA_TASK_CATEGORY = "EXTRA_TASK_CATEGORY"
        const val EXTRA_MINUTES_BEFORE = "EXTRA_MINUTES_BEFORE"
        const val EXTRA_DATE = "EXTRA_DATE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsManager = SettingsManager(context)
                val isMasterEnabled = settingsManager.notificationsEnabledFlow.first()
                if (!isMasterEnabled) {
                    pendingResult.finish()
                    return@launch
                }

                val db = AppDatabase.getDatabase(context)
                val dao = db.routineDao()

                when (action) {
                    ACTION_TASK_REMINDER -> {
                        val taskRemindersEnabled = settingsManager.taskRemindersEnabledFlow.first()
                        if (!taskRemindersEnabled) {
                            pendingResult.finish()
                            return@launch
                        }

                        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: ""
                        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
                        val startTime = intent.getStringExtra(EXTRA_TASK_START_TIME) ?: ""
                        val category = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: "Personal"
                        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 10)
                        val date = intent.getStringExtra(EXTRA_DATE) ?: RoutineTimeEngine.getLogicalDate().toString()

                        // Verify task exists and is enabled
                        val task = dao.getTaskById(taskId)
                        if (task == null || !task.isEnabled) {
                            pendingResult.finish()
                            return@launch
                        }

                        // Verify task is not already completed for this date
                        val completion = dao.getCompletion(date, taskId)
                        if (completion != null && completion.completed) {
                            pendingResult.finish()
                            return@launch
                        }

                        NotificationHelper.showTaskReminder(
                            context = context,
                            taskId = taskId,
                            title = title,
                            startTime = startTime,
                            category = category,
                            minutesBefore = minutesBefore
                        )
                    }

                    ACTION_UPCOMING_TASK -> {
                        val upcomingEnabled = settingsManager.upcomingTaskEnabledFlow.first()
                        if (!upcomingEnabled) {
                            pendingResult.finish()
                            return@launch
                        }

                        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: ""
                        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
                        val startTime = intent.getStringExtra(EXTRA_TASK_START_TIME) ?: ""
                        val category = intent.getStringExtra(EXTRA_TASK_CATEGORY) ?: "Personal"
                        val date = intent.getStringExtra(EXTRA_DATE) ?: RoutineTimeEngine.getLogicalDate().toString()

                        // Verify task exists and is enabled
                        val task = dao.getTaskById(taskId)
                        if (task == null || !task.isEnabled) {
                            pendingResult.finish()
                            return@launch
                        }

                        // Verify task is not already completed for this date
                        val completion = dao.getCompletion(date, taskId)
                        if (completion != null && completion.completed) {
                            pendingResult.finish()
                            return@launch
                        }

                        NotificationHelper.showUpcomingTask(
                            context = context,
                            taskId = taskId,
                            title = title,
                            startTime = startTime,
                            category = category
                        )
                    }

                    ACTION_MORNING_SUMMARY -> {
                        val morningSummaryEnabled = settingsManager.morningSummaryEnabledFlow.first()
                        if (!morningSummaryEnabled) {
                            pendingResult.finish()
                            return@launch
                        }

                        val userName = settingsManager.userNameFlow.first()
                        val logicalDate = RoutineTimeEngine.getLogicalDate()
                        val dateStr = logicalDate.toString()
                        val dayOfWeek = logicalDate.dayOfWeek.name

                        val allTasks = dao.getAllTasks().first()
                        val todaysTasks = allTasks.filter {
                            it.isEnabled && it.isEffectiveOn(dateStr) && it.repeatsOn(dayOfWeek)
                        }.sortedBy { it.sortOrder }

                        val isEmptyRoutine = allTasks.isEmpty()
                        val isFreeDay = !isEmptyRoutine && todaysTasks.isEmpty()
                        val taskTitles = todaysTasks.map { it.title }

                        NotificationHelper.showMorningSummary(
                            context = context,
                            userName = userName,
                            taskTitles = taskTitles,
                            isFreeDay = isFreeDay,
                            isEmptyRoutine = isEmptyRoutine
                        )

                        // Schedule next morning summary
                        RoutineNotificationScheduler.scheduleMorningSummary(context)
                    }

                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        RoutineNotificationScheduler.scheduleNotifications(context)
                    }
                }
            } catch (e: Exception) {
                // Graceful failure
            } finally {
                pendingResult.finish()
            }
        }
    }
}
