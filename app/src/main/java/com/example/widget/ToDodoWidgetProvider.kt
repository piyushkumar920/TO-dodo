package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.util.RoutineTimeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Home Screen Widget for to-dodo.
 * Displays:
 * - Current routine state (e.g. "Currently: Study" or "Between tasks")
 * - Next task information ("Up next: Project at 4:00 PM")
 * - Daily task completion progress
 * Powered by RoutineTimeEngine.
 */
class ToDodoWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ToDodoWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                updateAllWidgets(context, appWidgetManager, appWidgetIds)
            }
        }

        private fun updateAllWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.routineDao()
                    val now = LocalDateTime.now()
                    val logicalDate = RoutineTimeEngine.getLogicalDate(now)
                    val logicalDateStr = logicalDate.toString()
                    val dayName = logicalDate.dayOfWeek.name

                    val allTasks = dao.getAllTasks().first()
                    val tasksForDay = RoutineTimeEngine.getEffectiveTasksForLogicalDate(allTasks, logicalDateStr)

                    val allCompletions = dao.getCompletionsForDate(logicalDateStr).first()
                    val completedCount = tasksForDay.count { task ->
                        allCompletions.any { it.taskId == task.id && it.completed }
                    }
                    val totalCount = tasksForDay.size
                    val percentage = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0

                    val routineStatus = RoutineTimeEngine.calculateRoutineStatus(tasksForDay, now)

                    val views = buildRemoteViews(
                        context = context,
                        pendingIntent = pendingIntent,
                        totalCount = totalCount,
                        completedCount = completedCount,
                        percentage = percentage,
                        routineStatus = routineStatus
                    )
                    for (widgetId in appWidgetIds) {
                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                } catch (e: Exception) {
                    // Fail gracefully
                }
            }
        }

        fun buildRemoteViews(
            context: Context,
            pendingIntent: PendingIntent,
            totalCount: Int,
            completedCount: Int,
            percentage: Int,
            routineStatus: RoutineTimeEngine.RoutineStatusInfo
        ): RemoteViews {
            return RemoteViews(context.packageName, R.layout.widget_tododo).apply {
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                if (totalCount == 0) {
                    setTextViewText(R.id.widget_progress_text, "Free Day 🌿")
                    setProgressBar(R.id.widget_progress_bar, 100, 0, false)
                } else {
                    setTextViewText(R.id.widget_progress_text, "$completedCount/$totalCount • $percentage%")
                    setProgressBar(R.id.widget_progress_bar, 100, percentage, false)
                }

                setTextViewText(R.id.widget_status_headline, routineStatus.headline)

                val nextTask = routineStatus.nextTask
                if (nextTask != null) {
                    setTextViewText(R.id.widget_next_task_text, "Up next: ${nextTask.title} at ${RoutineTimeEngine.formatTimeForDisplay(nextTask.startTime)}")
                } else {
                    setTextViewText(R.id.widget_next_task_text, routineStatus.subtitle)
                }
            }
        }
    }
}
