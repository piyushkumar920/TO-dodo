package com.example

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.TaskEntity
import com.example.util.RoutineTimeEngine
import com.example.widget.ToDodoWidgetProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ToDodoWidgetTest {

    @Test
    fun testWidgetFreeDayRendering() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pendingIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val status = RoutineTimeEngine.RoutineStatusInfo(
            state = RoutineTimeEngine.RoutineStatusType.NO_TASKS_TODAY,
            currentTask = null,
            nextTask = null,
            headline = "Free Day 🌿",
            subtitle = "No scheduled tasks today. Rest & enjoy!"
        )

        val remoteViews = ToDodoWidgetProvider.buildRemoteViews(
            context = context,
            pendingIntent = pendingIntent,
            totalCount = 0,
            completedCount = 0,
            percentage = 0,
            routineStatus = status
        )

        val view = remoteViews.apply(context, null)
        val progressText = view.findViewById<TextView>(R.id.widget_progress_text)
        val progressBar = view.findViewById<ProgressBar>(R.id.widget_progress_bar)
        val headlineText = view.findViewById<TextView>(R.id.widget_status_headline)
        val nextTaskText = view.findViewById<TextView>(R.id.widget_next_task_text)

        assertEquals("Free Day 🌿", progressText.text.toString())
        assertEquals(0, progressBar.progress)
        assertEquals("Free Day 🌿", headlineText.text.toString())
        assertEquals("No scheduled tasks today. Rest & enjoy!", nextTaskText.text.toString())
    }

    @Test
    fun testWidgetActiveAndNextTaskRendering() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pendingIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentTask = TaskEntity(
            id = "task_current",
            dayOfWeek = "MONDAY",
            title = "DSA Practice",
            startTime = "14:00",
            endTime = "15:00",
            category = "Study",
            sortOrder = 1
        )
        val nextTask = TaskEntity(
            id = "task_next",
            dayOfWeek = "MONDAY",
            title = "Guitar",
            startTime = "15:30",
            endTime = "16:00",
            category = "Guitar",
            sortOrder = 2
        )

        val status = RoutineTimeEngine.RoutineStatusInfo(
            state = RoutineTimeEngine.RoutineStatusType.CURRENT_TASK,
            currentTask = currentTask,
            nextTask = nextTask,
            headline = "Currently: DSA Practice",
            subtitle = "Up next: Guitar at 15:30"
        )

        val remoteViews = ToDodoWidgetProvider.buildRemoteViews(
            context = context,
            pendingIntent = pendingIntent,
            totalCount = 4,
            completedCount = 2,
            percentage = 50,
            routineStatus = status
        )

        val view = remoteViews.apply(context, null)
        val progressText = view.findViewById<TextView>(R.id.widget_progress_text)
        val progressBar = view.findViewById<ProgressBar>(R.id.widget_progress_bar)
        val headlineText = view.findViewById<TextView>(R.id.widget_status_headline)
        val nextTaskText = view.findViewById<TextView>(R.id.widget_next_task_text)

        assertEquals("2/4 • 50%", progressText.text.toString())
        assertEquals(50, progressBar.progress)
        assertEquals("Currently: DSA Practice", headlineText.text.toString())
        assertEquals("Up next: Guitar at 15:30", nextTaskText.text.toString())
    }

    @Test
    fun testWidgetClickIntentOpensMainActivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pendingIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val status = RoutineTimeEngine.RoutineStatusInfo(
            state = RoutineTimeEngine.RoutineStatusType.BETWEEN_TASKS,
            currentTask = null,
            nextTask = null,
            headline = "Between tasks",
            subtitle = "Take a breath!"
        )

        val remoteViews = ToDodoWidgetProvider.buildRemoteViews(
            context = context,
            pendingIntent = pendingIntent,
            totalCount = 3,
            completedCount = 3,
            percentage = 100,
            routineStatus = status
        )

        val view = remoteViews.apply(context, null)
        val root = view.findViewById<View>(R.id.widget_root)
        assertTrue(root.hasOnClickListeners())
    }

    @Test
    fun testProviderUpdateWidgetsSafeExecution() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Calling updateWidgets should safely execute without throwing
        ToDodoWidgetProvider.updateWidgets(context)
    }
}
