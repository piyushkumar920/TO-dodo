package com.example

import com.example.data.db.TaskEntity
import com.example.util.RoutineTimeEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoutineTimeEngineTest {

    @Test
    fun testGreetingTimeBoundaries() {
        // 05:00 → Good morning
        val t0500 = LocalTime.of(5, 0)
        assertEquals("Good morning", RoutineTimeEngine.getGreetingInfo(t0500).greeting)
        assertEquals("☀️", RoutineTimeEngine.getGreetingInfo(t0500).emoji)

        // 11:59 → Good morning
        val t1159 = LocalTime.of(11, 59)
        assertEquals("Good morning", RoutineTimeEngine.getGreetingInfo(t1159).greeting)

        // 12:00 → Good afternoon
        val t1200 = LocalTime.of(12, 0)
        assertEquals("Good afternoon", RoutineTimeEngine.getGreetingInfo(t1200).greeting)
        assertEquals("🌤️", RoutineTimeEngine.getGreetingInfo(t1200).emoji)

        // 16:59 → Good afternoon
        val t1659 = LocalTime.of(16, 59)
        assertEquals("Good afternoon", RoutineTimeEngine.getGreetingInfo(t1659).greeting)

        // 17:00 → Good evening
        val t1700 = LocalTime.of(17, 0)
        assertEquals("Good evening", RoutineTimeEngine.getGreetingInfo(t1700).greeting)
        assertEquals("🌆", RoutineTimeEngine.getGreetingInfo(t1700).emoji)

        // 20:59 → Good evening
        val t2059 = LocalTime.of(20, 59)
        assertEquals("Good evening", RoutineTimeEngine.getGreetingInfo(t2059).greeting)

        // 21:00 → Good night
        val t2100 = LocalTime.of(21, 0)
        assertEquals("Good night", RoutineTimeEngine.getGreetingInfo(t2100).greeting)
        assertEquals("🌙", RoutineTimeEngine.getGreetingInfo(t2100).emoji)

        // 03:59 → Good night
        val t0359 = LocalTime.of(3, 59)
        assertEquals("Good night", RoutineTimeEngine.getGreetingInfo(t0359).greeting)

        // 04:00 → Good morning
        val t0400 = LocalTime.of(4, 0)
        assertEquals("Good morning", RoutineTimeEngine.getGreetingInfo(t0400).greeting)
    }

    @Test
    fun testGreetingWithDynamicUserName() {
        val t1400 = LocalTime.of(14, 0)
        val greetingInfo = RoutineTimeEngine.getGreetingInfo(t1400)
        assertEquals("Good afternoon, Piyush 🌤️", greetingInfo.formatWithUser("Piyush"))
        assertEquals("Good afternoon, Sarah 🌤️", greetingInfo.formatWithUser("Sarah"))
    }

    @Test
    fun testActualDeviceDate() {
        val testDate = LocalDate.of(2026, 10, 2) // Friday, October 2, 2026
        val formatted = RoutineTimeEngine.formatActualDate(testDate)
        assertTrue(formatted.contains("Friday"))
        assertTrue(formatted.contains("October"))
        assertTrue(formatted.contains("2"))
    }

    @Test
    fun testDayOfWeekCalculation() {
        val dateFriday = LocalDate.of(2026, 10, 2)
        assertEquals(DayOfWeek.FRIDAY, RoutineTimeEngine.getDayOfWeek(dateFriday))

        val dateThursday = LocalDate.of(2026, 10, 1)
        assertEquals(DayOfWeek.THURSDAY, RoutineTimeEngine.getDayOfWeek(dateThursday))
    }

    @Test
    fun testLogicalDateMidnightRule() {
        // 01:30 AM Thursday Oct 2 → logical Wednesday Oct 1
        val dt0130 = LocalDateTime.of(2026, 10, 2, 1, 30)
        assertEquals(LocalDate.of(2026, 10, 1), RoutineTimeEngine.getLogicalDate(dt0130))

        // 03:59 AM Thursday Oct 2 → logical Wednesday Oct 1
        val dt0359 = LocalDateTime.of(2026, 10, 2, 3, 59)
        assertEquals(LocalDate.of(2026, 10, 1), RoutineTimeEngine.getLogicalDate(dt0359))

        // 04:00 AM Thursday Oct 2 → logical Thursday Oct 2
        val dt0400 = LocalDateTime.of(2026, 10, 2, 4, 0)
        assertEquals(LocalDate.of(2026, 10, 2), RoutineTimeEngine.getLogicalDate(dt0400))
    }

    @Test
    fun testCurrentTaskAndNextTaskDetection() {
        val sampleTasks = listOf(
            TaskEntity("1", "FRIDAY", "Study", "2:00 PM", "3:00 PM", "Study", 0),
            TaskEntity("2", "FRIDAY", "Project", "3:30 PM", "4:30 PM", "Project / Internship", 1),
            TaskEntity("3", "FRIDAY", "Guitar", "5:00 PM", "5:30 PM", "Guitar", 2)
        )

        // At 3:45 PM: inside "Project"
        val time345pm = LocalDateTime.of(2026, 10, 2, 15, 45)
        val status345 = RoutineTimeEngine.calculateRoutineStatus(sampleTasks, time345pm)
        assertEquals(RoutineTimeEngine.RoutineStatusType.CURRENT_TASK, status345.state)
        assertEquals("Project", status345.currentTask?.title)
        assertEquals("Guitar", status345.nextTask?.title)

        // At 4:45 PM: between "Project" and "Guitar"
        val time445pm = LocalDateTime.of(2026, 10, 2, 16, 45)
        val status445 = RoutineTimeEngine.calculateRoutineStatus(sampleTasks, time445pm)
        assertEquals(RoutineTimeEngine.RoutineStatusType.BETWEEN_TASKS, status445.state)
        assertNull(status445.currentTask)
        assertEquals("Guitar", status445.nextTask?.title)

        // At 1:00 PM: before first task (Study at 2:00 PM)
        val time100pm = LocalDateTime.of(2026, 10, 2, 13, 0)
        val status100 = RoutineTimeEngine.calculateRoutineStatus(sampleTasks, time100pm)
        assertEquals(RoutineTimeEngine.RoutineStatusType.BEFORE_FIRST_TASK, status100.state)
        assertNull(status100.currentTask)
        assertEquals("Study", status100.nextTask?.title)

        // At 6:00 PM: after last task (Guitar ended at 5:30 PM)
        val time600pm = LocalDateTime.of(2026, 10, 2, 18, 0)
        val status600 = RoutineTimeEngine.calculateRoutineStatus(sampleTasks, time600pm)
        assertEquals(RoutineTimeEngine.RoutineStatusType.AFTER_LAST_TASK, status600.state)
        assertNull(status600.currentTask)
        assertNull(status600.nextTask)
    }

    @Test
    fun testEmptyDayStatus() {
        val emptyTasks = emptyList<TaskEntity>()
        val anyTime = LocalDateTime.of(2026, 10, 2, 12, 0)
        val status = RoutineTimeEngine.calculateRoutineStatus(emptyTasks, anyTime)
        assertEquals(RoutineTimeEngine.RoutineStatusType.NO_TASKS_TODAY, status.state)
        assertNull(status.currentTask)
        assertNull(status.nextTask)
        assertTrue(status.headline.contains("Nothing scheduled today"))
    }

    @Test
    fun testLateNightRoutineDisambiguation() {
        // Full evening-to-late-night sequence
        val overnightTasks = listOf(
            TaskEntity("1", "FRIDAY", "Dinner", "8:30 PM", "9:30 PM", "Rest", 0),
            TaskEntity("2", "FRIDAY", "Reading", "11:00", "11:30", "Reading", 1),
            TaskEntity("3", "FRIDAY", "Wind down", "12:30", "1:00", "Rest", 2),
            TaskEntity("4", "FRIDAY", "Sleep", "1:00", "4:00", "Rest", 3)
        )

        // At 12:45 AM (00:45)
        val atMidnight45 = LocalDateTime.of(2026, 10, 3, 0, 45)
        val status = RoutineTimeEngine.calculateRoutineStatus(overnightTasks, atMidnight45)
        assertEquals(RoutineTimeEngine.RoutineStatusType.CURRENT_TASK, status.state)
        assertEquals("Wind down", status.currentTask?.title)
        assertEquals("Sleep", status.nextTask?.title)
    }

    @Test
    fun testAmPmDistinctionAndFormatting() {
        // 9 AM vs 9 PM
        val am12 = RoutineTimeEngine.parseTo12Hour("09:00")
        assertEquals(9, am12.hour12)
        assertTrue(am12.isAm)
        assertEquals("09:00", am12.toCanonical24HourString())
        assertEquals("9:00 AM", RoutineTimeEngine.formatTimeForDisplay("09:00"))

        val pm12 = RoutineTimeEngine.parseTo12Hour("21:00")
        assertEquals(9, pm12.hour12)
        assertFalse(pm12.isAm)
        assertEquals("21:00", pm12.toCanonical24HourString())
        assertEquals("9:00 PM", RoutineTimeEngine.formatTimeForDisplay("21:00"))

        // 12 AM vs 12 PM
        val midnight12 = RoutineTimeEngine.parseTo12Hour("00:00")
        assertEquals(12, midnight12.hour12)
        assertTrue(midnight12.isAm)
        assertEquals("00:00", midnight12.toCanonical24HourString())
        assertEquals("12:00 AM", RoutineTimeEngine.formatTimeForDisplay("00:00"))

        val noon12 = RoutineTimeEngine.parseTo12Hour("12:00")
        assertEquals(12, noon12.hour12)
        assertFalse(noon12.isAm)
        assertEquals("12:00", noon12.toCanonical24HourString())
        assertEquals("12:00 PM", RoutineTimeEngine.formatTimeForDisplay("12:00"))

        // Notification request code uniqueness for 9 AM vs 9 PM tasks
        val codeMorning = com.example.util.RoutineNotificationScheduler.getReminderRequestCode("task_a", "2026-10-02", "09:00")
        val codeEvening = com.example.util.RoutineNotificationScheduler.getReminderRequestCode("task_a", "2026-10-02", "21:00")
        assertNotEquals(codeMorning, codeEvening)
    }

    @Test
    fun testGetEffectiveTasksForLogicalDate() {
        val tasks = listOf(
            TaskEntity("1", "MONDAY", "Monday Task", "09:00", "10:00", "Study", 0, daysOfWeek = "MONDAY", effectiveFromDate = "2026-01-01", effectiveUntilDate = null),
            TaskEntity("2", "MONDAY", "Expired Task", "10:00", "11:00", "Study", 1, daysOfWeek = "MONDAY", effectiveFromDate = "2026-01-01", effectiveUntilDate = "2026-09-30"),
            TaskEntity("3", "MONDAY", "Future Task", "11:00", "12:00", "Study", 2, daysOfWeek = "MONDAY", effectiveFromDate = "2026-10-12", effectiveUntilDate = null),
            TaskEntity("4", "FRIDAY", "Friday Task", "14:00", "15:00", "Work", 3, daysOfWeek = "FRIDAY", effectiveFromDate = "2026-01-01", effectiveUntilDate = null),
            TaskEntity("5", "MONDAY", "Disabled Task", "15:00", "16:00", "Rest", 4, daysOfWeek = "MONDAY", isEnabled = false, effectiveFromDate = "2026-01-01", effectiveUntilDate = null)
        )

        // Monday October 5, 2026 -> should only return "Monday Task"
        val mondayEffective = RoutineTimeEngine.getEffectiveTasksForLogicalDate(tasks, "2026-10-05")
        assertEquals(1, mondayEffective.size)
        assertEquals("Monday Task", mondayEffective[0].title)

        // Friday October 2, 2026 -> should return "Friday Task"
        val fridayEffective = RoutineTimeEngine.getEffectiveTasksForLogicalDate(tasks, "2026-10-02")
        // Wait, Oct 2, 2026 is Friday! Let's check: 2026-10-02 is Friday.
        // If Oct 2 is Friday, "Monday Task" repeats on Monday (not Friday), and "Friday Task" repeats on Friday.
        // Let's test explicitly:
        val oct2FridayTasks = RoutineTimeEngine.getEffectiveTasksForLogicalDate(tasks, "2026-10-02")
        assertEquals(1, oct2FridayTasks.size)
        assertEquals("Friday Task", oct2FridayTasks[0].title)

        // Monday September 28, 2026 -> Monday Task is effective, Expired Task is effective
        val sep28MondayTasks = RoutineTimeEngine.getEffectiveTasksForLogicalDate(tasks, "2026-09-28")
        assertEquals(2, sep28MondayTasks.size)
        assertEquals("Monday Task", sep28MondayTasks[0].title)
        assertEquals("Expired Task", sep28MondayTasks[1].title)
    }
}
