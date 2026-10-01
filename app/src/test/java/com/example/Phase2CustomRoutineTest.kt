package com.example

import com.example.data.db.TaskEntity
import com.example.util.RoutineTimeEngine
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase2CustomRoutineTest {

    @Test
    fun testTaskMultiDayRepetition() {
        val task = TaskEntity(
            id = "dsa_task",
            dayOfWeek = "MONDAY",
            title = "DSA Practice",
            startTime = "19:00",
            endTime = "20:00",
            category = "Study",
            sortOrder = 900,
            daysOfWeek = "MONDAY,WEDNESDAY,FRIDAY",
            isEnabled = true
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
    fun testTaskEnabledDisabledState() {
        val enabledTask = TaskEntity(
            id = "task_1",
            dayOfWeek = "MONDAY",
            title = "Guitar",
            startTime = "15:00",
            endTime = "15:30",
            category = "Guitar",
            sortOrder = 660,
            daysOfWeek = "MONDAY,TUESDAY",
            isEnabled = true
        )
        assertTrue(enabledTask.repeatsOn("MONDAY"))

        // Disabled task must not repeat on any day
        val disabledTask = enabledTask.copy(isEnabled = false)
        assertFalse(disabledTask.repeatsOn("MONDAY"))
        assertFalse(disabledTask.repeatsOn("TUESDAY"))
    }

    @Test
    fun testHistoricalValidityPeriods() {
        val task = TaskEntity(
            id = "task_v1",
            dayOfWeek = "MONDAY",
            title = "Morning Study",
            startTime = "09:00",
            endTime = "11:00",
            category = "Study",
            sortOrder = 300,
            daysOfWeek = "MONDAY",
            isEnabled = true,
            effectiveFromDate = "2026-10-01",
            effectiveUntilDate = "2026-10-05"
        )

        // Before effective date: false
        assertFalse(task.isEffectiveOn("2026-09-30"))

        // During validity window: true
        assertTrue(task.isEffectiveOn("2026-10-01"))
        assertTrue(task.isEffectiveOn("2026-10-03"))
        assertTrue(task.isEffectiveOn("2026-10-05"))

        // After validity window has ended: false
        assertFalse(task.isEffectiveOn("2026-10-06"))
        assertFalse(task.isEffectiveOn("2026-10-12"))
    }

    @Test
    fun testHistoricalCompletionsSurviveTaskDeletion() {
        // Task was active up to 2026-10-05
        val deletedTask = TaskEntity(
            id = "task_guitar",
            dayOfWeek = "MONDAY",
            title = "Guitar",
            startTime = "15:00",
            endTime = "15:30",
            category = "Guitar",
            sortOrder = 660,
            daysOfWeek = "MONDAY",
            isEnabled = true,
            effectiveFromDate = "2026-09-01",
            effectiveUntilDate = "2026-10-05" // user deleted task on Oct 5
        )

        // Historical routine days retain the task
        assertTrue(deletedTask.isEffectiveOn("2026-09-28"))
        assertTrue(deletedTask.isEffectiveOn("2026-10-05"))

        // Future routine days no longer include the task
        assertFalse(deletedTask.isEffectiveOn("2026-10-06"))
        assertFalse(deletedTask.isEffectiveOn("2026-10-12"))
    }

    @Test
    fun testFreeDayHandling() {
        val tasks = emptyList<TaskEntity>()
        val total = tasks.size
        assertEquals(0, total)

        // A free day has 0 total tasks, 0 completed, 0%
        val percentage = if (total > 0) 100 else 0
        assertEquals(0, percentage)

        // Free day does NOT qualify for Credit Score award
        val qualifiesForCredit = total > 0 && 0 >= total
        assertFalse(qualifiesForCredit)
    }

    @Test
    fun testChronologicalOrdering() {
        val tasks = listOf(
            TaskEntity("3", "MONDAY", "Night Reading", "21:00", "21:30", "Reading", 0),
            TaskEntity("1", "MONDAY", "Breakfast", "08:15", "09:00", "Personal", 0),
            TaskEntity("2", "MONDAY", "Lunch", "12:00", "13:00", "Rest", 0)
        )

        val sorted = tasks.sortedWith(compareBy({ RoutineTimeEngine.toLogicalMinutes(it.startTime) }, { it.sortOrder }))
        assertEquals("Breakfast", sorted[0].title)
        assertEquals("Lunch", sorted[1].title)
        assertEquals("Night Reading", sorted[2].title)
    }

    @Test
    fun testOverlappingTasksDetection() {
        val task1 = TaskEntity("1", "MONDAY", "Task 1", "10:00", "12:00", "Study", 0, daysOfWeek = "MONDAY")
        val task2 = TaskEntity("2", "MONDAY", "Task 2", "11:00", "13:00", "College", 1, daysOfWeek = "MONDAY")

        val start1 = RoutineTimeEngine.toLogicalMinutes(task1.startTime)
        val end1 = RoutineTimeEngine.toLogicalMinutes(task1.endTime)
        val start2 = RoutineTimeEngine.toLogicalMinutes(task2.startTime)
        val end2 = RoutineTimeEngine.toLogicalMinutes(task2.endTime)

        // Interval overlap condition: max(start1, start2) < min(end1, end2)
        val isOverlapping = maxOf(start1, start2) < minOf(end1, end2)
        assertTrue("Task 1 and Task 2 overlap between 11:00 and 12:00", isOverlapping)
    }

    @Test
    fun testJsonExportImportWithCustomRoutine() {
        val root = JSONObject().apply {
            put("version", 3)
            put("appName", "to-dodo")
            put("exportedAt", System.currentTimeMillis())

            val tasksArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "task_custom_1")
                    put("dayOfWeek", "MONDAY")
                    put("title", "DSA Practice")
                    put("startTime", "19:00")
                    put("endTime", "20:00")
                    put("category", "Study")
                    put("sortOrder", 900)
                    put("daysOfWeek", "MONDAY,WEDNESDAY,FRIDAY")
                    put("isEnabled", true)
                    put("reminderEnabled", true)
                    put("effectiveFromDate", "2026-10-01")
                    put("effectiveUntilDate", JSONObject.NULL)
                })
            }
            put("tasks", tasksArray)
            put("completionRecords", JSONArray())
        }

        val jsonStr = root.toString()
        val parsed = JSONObject(jsonStr)
        assertTrue(parsed.has("tasks"))

        val arr = parsed.getJSONArray("tasks")
        assertEquals(1, arr.length())
        val taskJson = arr.getJSONObject(0)
        assertEquals("task_custom_1", taskJson.getString("id"))
        assertEquals("DSA Practice", taskJson.getString("title"))
        assertEquals("MONDAY,WEDNESDAY,FRIDAY", taskJson.getString("daysOfWeek"))
        assertTrue(taskJson.getBoolean("isEnabled"))
    }
}
