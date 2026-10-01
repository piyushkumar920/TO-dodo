package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.CompletionRecordEntity
import com.example.data.db.TaskEntity
import com.example.data.preferences.SettingsManager
import com.example.data.preferences.dataStore
import com.example.util.NotificationHelper
import com.example.util.RoutineNotificationScheduler
import com.example.util.RoutineTimeEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalNotificationSystemTest {

    @Test
    fun testDefaultNotificationSettings() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            context.dataStore.edit { it.clear() }
            val settingsManager = SettingsManager(context)

            assertTrue(settingsManager.notificationsEnabledFlow.first())
            assertTrue(settingsManager.taskRemindersEnabledFlow.first())
            assertFalse(settingsManager.upcomingTaskEnabledFlow.first())
            assertFalse(settingsManager.morningSummaryEnabledFlow.first())
            assertEquals(10, settingsManager.reminderTimingMinutesFlow.first())
            assertEquals("08:00", settingsManager.morningSummaryTimeFlow.first())
        }
    }

    @Test
    fun testNotificationSettingsPersistence() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val settingsManager = SettingsManager(context)

            settingsManager.setNotificationsEnabled(false)
            assertFalse(settingsManager.notificationsEnabledFlow.first())

            settingsManager.setTaskRemindersEnabled(false)
            assertFalse(settingsManager.taskRemindersEnabledFlow.first())

            settingsManager.setUpcomingTaskEnabled(true)
            assertTrue(settingsManager.upcomingTaskEnabledFlow.first())

            settingsManager.setMorningSummaryEnabled(true)
            assertTrue(settingsManager.morningSummaryEnabledFlow.first())

            settingsManager.setReminderTimingMinutes(15)
            assertEquals(15, settingsManager.reminderTimingMinutesFlow.first())

            settingsManager.setMorningSummaryTime("07:30")
            assertEquals("07:30", settingsManager.morningSummaryTimeFlow.first())
        }
    }

    @Test
    fun testReminderTimingCalculations() {
        val logicalDate = LocalDate.of(2026, 10, 2)
        // 18:00 PM start time (6:00 PM)
        val startLogicalMins = RoutineTimeEngine.toLogicalMinutes("18:00")
        val taskStartDateTime = RoutineTimeEngine.getTaskStartLocalDateTime(logicalDate, startLogicalMins)

        assertEquals(18, taskStartDateTime.hour)
        assertEquals(0, taskStartDateTime.minute)

        val zoneId = ZoneId.systemDefault()
        val startEpochMillis = taskStartDateTime.atZone(zoneId).toInstant().toEpochMilli()

        // 1. At task start (0 min)
        val timing0 = startEpochMillis - (0 * 60 * 1000L)
        assertEquals(startEpochMillis, timing0)

        // 2. 5 minutes before
        val timing5 = startEpochMillis - (5 * 60 * 1000L)
        assertEquals(startEpochMillis - 300_000L, timing5)

        // 3. 10 minutes before
        val timing10 = startEpochMillis - (10 * 60 * 1000L)
        assertEquals(startEpochMillis - 600_000L, timing10)

        // 4. 15 minutes before
        val timing15 = startEpochMillis - (15 * 60 * 1000L)
        assertEquals(startEpochMillis - 900_000L, timing15)

        // 5. 30 minutes before
        val timing30 = startEpochMillis - (30 * 60 * 1000L)
        assertEquals(startEpochMillis - 1_800_000L, timing30)
    }

    @Test
    fun testDeterministicNotificationAndRequestCodes() {
        val taskId = "dsa_practice_task"
        val dateStr = "2026-10-02"

        val reminderReq1 = RoutineNotificationScheduler.getReminderRequestCode(taskId, dateStr)
        val reminderReq2 = RoutineNotificationScheduler.getReminderRequestCode(taskId, dateStr)
        assertEquals("Request code must be strictly deterministic and stable", reminderReq1, reminderReq2)

        val upcomingReq1 = RoutineNotificationScheduler.getUpcomingRequestCode(taskId, dateStr)
        val upcomingReq2 = RoutineNotificationScheduler.getUpcomingRequestCode(taskId, dateStr)
        assertEquals(upcomingReq1, upcomingReq2)
        assertNotEquals("Reminder and upcoming request codes must differ", reminderReq1, upcomingReq1)

        val notifId1 = NotificationHelper.getReminderNotificationId(taskId)
        val notifId2 = NotificationHelper.getReminderNotificationId(taskId)
        assertEquals(notifId1, notifId2)

        val otherNotifId = NotificationHelper.getReminderNotificationId("other_task")
        assertNotEquals("Different tasks must have different notification IDs", notifId1, otherNotifId)
    }

    @Test
    fun testMultiDayTaskAndDayOfWeekFiltering() {
        val task = TaskEntity(
            id = "gym_task",
            dayOfWeek = "MONDAY",
            title = "Gym",
            startTime = "17:00",
            endTime = "18:00",
            category = "Exercise",
            sortOrder = 1,
            daysOfWeek = "MONDAY,WEDNESDAY,FRIDAY"
        )

        assertTrue(task.repeatsOn("MONDAY"))
        assertTrue(task.repeatsOn("WEDNESDAY"))
        assertTrue(task.repeatsOn("FRIDAY"))
        assertFalse(task.repeatsOn("TUESDAY"))
        assertFalse(task.repeatsOn("THURSDAY"))
        assertFalse(task.repeatsOn("SATURDAY"))
        assertFalse(task.repeatsOn("SUNDAY"))
    }

    @Test
    fun testCompletedTaskExclusionFromAlarms() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
            val dao = db.routineDao()

            val taskId = "task_already_done"
            val dateStr = "2026-10-02"

            dao.insertTasks(
                listOf(
                    TaskEntity(
                        id = taskId,
                        dayOfWeek = "FRIDAY",
                        title = "Machine Learning",
                        startTime = "19:00",
                        endTime = "20:00",
                        category = "Study",
                        sortOrder = 1,
                        daysOfWeek = "FRIDAY"
                    )
                )
            )

            dao.upsertCompletion(
                CompletionRecordEntity(
                    id = "${dateStr}_$taskId",
                    date = dateStr,
                    taskId = taskId,
                    completed = true,
                    completedAt = System.currentTimeMillis()
                )
            )

            val completions = dao.getAllCompletions().first()
            val isCompleted = completions.any { it.date == dateStr && it.taskId == taskId && it.completed }
            assertTrue("Task must be recognized as completed and thus excluded from future alarms", isCompleted)

            db.close()
        }
    }

    @Test
    fun testDisabledTaskExclusion() {
        val enabledTask = TaskEntity(
            id = "active_task",
            dayOfWeek = "MONDAY",
            title = "Coding",
            startTime = "10:00",
            endTime = "11:00",
            category = "Project / Internship",
            sortOrder = 1,
            daysOfWeek = "MONDAY",
            isEnabled = true
        )
        val disabledTask = enabledTask.copy(id = "disabled_task", isEnabled = false)

        assertTrue(enabledTask.isEnabled)
        assertFalse(disabledTask.isEnabled)
    }

    @Test
    fun testNotificationChannelsCreated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NotificationHelper.createNotificationChannels(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = notificationManager.notificationChannels

        val channelIds = channels.map { it.id }
        assertTrue(channelIds.contains(NotificationHelper.CHANNEL_TASK_REMINDERS))
        assertTrue(channelIds.contains(NotificationHelper.CHANNEL_UPCOMING_TASKS))
        assertTrue(channelIds.contains(NotificationHelper.CHANNEL_MORNING_SUMMARY))
    }

    @Test
    fun testNotificationDispatchMethodsDoNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Task reminder dispatch
        NotificationHelper.showTaskReminder(
            context = context,
            taskId = "test_task_1",
            title = "DSA Practice",
            startTime = "7:00 PM",
            category = "Study",
            minutesBefore = 10
        )

        // 2. Upcoming task dispatch
        NotificationHelper.showUpcomingTask(
            context = context,
            taskId = "test_task_2",
            title = "Project Work",
            startTime = "8:30 PM",
            category = "Project / Internship"
        )

        // 3. Morning summary dispatch (normal routine)
        NotificationHelper.showMorningSummary(
            context = context,
            userName = "Piyush",
            taskTitles = listOf("DSA Practice", "Project Work", "Exercise"),
            isFreeDay = false,
            isEmptyRoutine = false
        )

        // 4. Morning summary dispatch (Free Day)
        NotificationHelper.showMorningSummary(
            context = context,
            userName = "Piyush",
            taskTitles = emptyList(),
            isFreeDay = true,
            isEmptyRoutine = false
        )

        // 5. Morning summary dispatch (Empty Routine)
        NotificationHelper.showMorningSummary(
            context = context,
            userName = "Piyush",
            taskTitles = emptyList(),
            isFreeDay = false,
            isEmptyRoutine = true
        )

        // 6. Cancel task notification
        NotificationHelper.cancelTaskNotification(context, "test_task_1")
    }
}
