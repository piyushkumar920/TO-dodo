package com.example.util

import com.example.data.db.TaskEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Central Time & Date Engine for to-dodo.
 *
 * Provides a single source of truth for:
 * - Device actual date & time
 * - Logical routine date (00:00-03:59 belongs to previous day's routine)
 * - Dynamic time-of-day greetings
 * - Day-of-week derivation
 * - Current task, next task, and routine state detection
 *
 * 100% offline, local-only, and future-routine-customization compatible.
 */
object RoutineTimeEngine {

    // Logical routine day boundary: 04:00 AM
    const val LOGICAL_DAY_CUTOFF_HOUR = 4

    /**
     * Determines the logical date for routine tracking.
     * Routine hours between 00:00 and 03:59 belong to the previous night's routine day.
     * At 04:00 AM onward, the logical date is the current calendar day.
     */
    fun getLogicalDate(dateTime: LocalDateTime = LocalDateTime.now()): LocalDate {
        return if (dateTime.hour < LOGICAL_DAY_CUTOFF_HOUR) {
            dateTime.toLocalDate().minusDays(1)
        } else {
            dateTime.toLocalDate()
        }
    }

    /**
     * Day of week derived strictly from the given date.
     */
    fun getDayOfWeek(date: LocalDate): DayOfWeek = date.dayOfWeek

    data class GreetingInfo(
        val greeting: String,
        val emoji: String
    ) {
        fun formatWithUser(userName: String): String {
            return "$greeting, $userName $emoji"
        }
    }

    /**
     * Dynamic greeting based on current local time:
     * 04:00–11:59 -> "Good morning" ☀️
     * 12:00–16:59 -> "Good afternoon" 🌤️
     * 17:00–20:59 -> "Good evening" 🌆
     * 21:00–03:59 -> "Good night" 🌙
     */
    fun getGreetingInfo(time: LocalTime = LocalTime.now()): GreetingInfo {
        val hour = time.hour
        return when {
            hour in 4..11 -> GreetingInfo("Good morning", "☀️")
            hour in 12..16 -> GreetingInfo("Good afternoon", "🌤️")
            hour in 17..20 -> GreetingInfo("Good evening", "🌆")
            else -> GreetingInfo("Good night", "🌙") // 21:00 to 03:59
        }
    }

    /**
     * Friendly date format: e.g. "Thursday, October 2"
     */
    fun formatActualDate(date: LocalDate = LocalDate.now()): String {
        return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    }

    enum class RoutineStatusType {
        BEFORE_FIRST_TASK,
        CURRENT_TASK,
        BETWEEN_TASKS,
        AFTER_LAST_TASK,
        NO_TASKS_TODAY
    }

    data class RoutineStatusInfo(
        val state: RoutineStatusType,
        val currentTask: TaskEntity? = null,
        val nextTask: TaskEntity? = null,
        val headline: String,
        val subtitle: String
    )

    /**
     * Helper representation of a parsed task time window inside the logical day.
     * Minutes run from 0 (04:00 AM) to 1439 (03:59 AM next morning).
     */
    data class ParsedTaskInterval(
        val task: TaskEntity,
        val startLogicalMinutes: Int,
        val endLogicalMinutes: Int
    )

    /**
     * Converts a local hour & minute to logical day minutes where 04:00 AM = 0.
     */
    fun toLogicalMinutes(hour: Int, minute: Int): Int {
        return if (hour >= LOGICAL_DAY_CUTOFF_HOUR) {
            (hour - LOGICAL_DAY_CUTOFF_HOUR) * 60 + minute
        } else {
            (hour + (24 - LOGICAL_DAY_CUTOFF_HOUR)) * 60 + minute
        }
    }

    fun toLogicalMinutes(timeStr: String): Int {
        return parseTimeToLogicalMinutes(timeStr, 0) ?: 0
    }

    /**
     * Converts logical day minutes (0 = 04:00 AM) back to standard 24-hour clock (hour, minute).
     */
    fun logicalMinutesToWallClock(logicalMinutes: Int): Pair<Int, Int> {
        val totalMins = Math.floorMod(logicalMinutes, 1440)
        val hour = if (totalMins < 1200) {
            LOGICAL_DAY_CUTOFF_HOUR + (totalMins / 60)
        } else {
            (totalMins - 1200) / 60
        }
        val min = totalMins % 60
        return Pair(hour, min)
    }

    /**
     * Converts a task's logical minutes on a logical routine date to the exact wall-clock LocalDateTime.
     */
    fun getTaskStartLocalDateTime(logicalDate: LocalDate, logicalMinutes: Int): LocalDateTime {
        val (hour, min) = logicalMinutesToWallClock(logicalMinutes)
        val actualDate = if (hour < LOGICAL_DAY_CUTOFF_HOUR) logicalDate.plusDays(1) else logicalDate
        return actualDate.atTime(hour, min)
    }

    /**
     * Parses time strings into logical day minutes.
     * Supports:
     * - 24-hour: "14:00", "09:30"
     * - AM/PM: "2:00 PM", "9:00 AM", "11:30 PM"
     * - Sequential 12-hour: automatically disambiguates morning vs afternoon vs midnight
     *   based on preceding task times.
     */
    fun parseTimeToLogicalMinutes(
        timeStr: String,
        previousLogicalMinutes: Int = 0
    ): Int? {
        val trimmed = timeStr.trim()
        if (trimmed.isEmpty()) return null

        val upper = trimmed.uppercase()
        val isExplicitPm = upper.contains("PM")
        val isExplicitAm = upper.contains("AM")
        val cleaned = upper.replace("AM", "").replace("PM", "").trim()

        val parts = cleaned.split(":")
        if (parts.isEmpty()) return null

        val parsedHour = parts[0].trim().toIntOrNull() ?: return null
        val parsedMin = if (parts.size > 1) parts[1].trim().toIntOrNull() ?: 0 else 0

        val resolvedHour24 = when {
            isExplicitPm -> if (parsedHour == 12) 12 else (parsedHour % 12) + 12
            isExplicitAm -> if (parsedHour == 12) 0 else parsedHour % 12
            parsedHour >= 13 -> parsedHour // already 24h
            previousLogicalMinutes in 480 until 1200 -> {
                // In afternoon/evening sequence (12:00 PM to 23:59 PM)
                when {
                    parsedHour in 1..11 -> parsedHour + 12
                    parsedHour == 12 -> if (previousLogicalMinutes >= 1100) 0 else 12
                    else -> parsedHour
                }
            }
            previousLogicalMinutes >= 1200 -> {
                // In post-midnight sequence (00:00 AM to 03:59 AM)
                when {
                    parsedHour == 12 -> 0
                    parsedHour in 1..6 -> parsedHour
                    else -> parsedHour
                }
            }
            else -> {
                // Morning sequence (before 12:00 PM)
                when {
                    parsedHour in 1..6 -> parsedHour + 12
                    else -> parsedHour
                }
            }
        }

        return toLogicalMinutes(resolvedHour24, parsedMin)
    }

    /**
     * Resolves all tasks into ordered intervals within the logical routine day.
     */
    fun resolveTaskIntervals(tasks: List<TaskEntity>): List<ParsedTaskInterval> {
        val sortedTasks = tasks.sortedBy { it.sortOrder }
        val intervals = mutableListOf<ParsedTaskInterval>()
        var lastEndMinutes = 0

        for (task in sortedTasks) {
            val startMin = parseTimeToLogicalMinutes(task.startTime, lastEndMinutes) ?: continue
            val endMin = if (task.endTime.isNotEmpty()) {
                parseTimeToLogicalMinutes(task.endTime, startMin) ?: (startMin + 60)
            } else {
                startMin + 60 // default 1 hour if end time omitted
            }

            val adjustedEnd = if (endMin <= startMin) startMin + 30 else endMin
            intervals.add(ParsedTaskInterval(task, startMin, adjustedEnd))
            lastEndMinutes = adjustedEnd
        }

        return intervals.sortedBy { it.startLogicalMinutes }
    }

    /**
     * Computes the current RoutineStatus given scheduled tasks and device local time.
     */
    fun calculateRoutineStatus(
        tasks: List<TaskEntity>,
        now: LocalDateTime = LocalDateTime.now()
    ): RoutineStatusInfo {
        if (tasks.isEmpty()) {
            return RoutineStatusInfo(
                state = RoutineStatusType.NO_TASKS_TODAY,
                currentTask = null,
                nextTask = null,
                headline = "Nothing scheduled today 🌿",
                subtitle = "Enjoy your free day ♡"
            )
        }

        val intervals = resolveTaskIntervals(tasks)
        if (intervals.isEmpty()) {
            return RoutineStatusInfo(
                state = RoutineStatusType.NO_TASKS_TODAY,
                currentTask = null,
                nextTask = null,
                headline = "Nothing scheduled today 🌿",
                subtitle = "Enjoy your free day ♡"
            )
        }

        val currentLogicalMin = toLogicalMinutes(now.hour, now.minute)
        val firstInterval = intervals.first()

        // 1. Before first task of the day
        if (currentLogicalMin < firstInterval.startLogicalMinutes) {
            return RoutineStatusInfo(
                state = RoutineStatusType.BEFORE_FIRST_TASK,
                currentTask = null,
                nextTask = firstInterval.task,
                headline = "First task starts at ${firstInterval.task.startTime}",
                subtitle = "Up next: ${firstInterval.task.title}"
            )
        }

        // 2. Check if currently inside any task
        val current = intervals.firstOrNull { interval ->
            currentLogicalMin >= interval.startLogicalMinutes && currentLogicalMin < interval.endLogicalMinutes
        }

        if (current != null) {
            val next = intervals.firstOrNull { it.startLogicalMinutes >= current.endLogicalMinutes }?.task
            return RoutineStatusInfo(
                state = RoutineStatusType.CURRENT_TASK,
                currentTask = current.task,
                nextTask = next,
                headline = "Currently: ${current.task.title}",
                subtitle = if (next != null) "Up next: ${next.title} at ${next.startTime}" else "Final task of the day ♡"
            )
        }

        // 3. Between tasks
        val next = intervals.firstOrNull { it.startLogicalMinutes > currentLogicalMin }
        if (next != null) {
            return RoutineStatusInfo(
                state = RoutineStatusType.BETWEEN_TASKS,
                currentTask = null,
                nextTask = next.task,
                headline = "You're between tasks ♡",
                subtitle = "Up next: ${next.task.title} at ${next.task.startTime}"
            )
        }

        // 4. After the final task of the day
        return RoutineStatusInfo(
            state = RoutineStatusType.AFTER_LAST_TASK,
            currentTask = null,
            nextTask = null,
            headline = "You're done for today! 🎉",
            subtitle = "Great work showing up today ♡"
        )
    }

    /**
     * Check if a specific task is currently active at the given time.
     */
    fun isTaskActive(
        task: TaskEntity,
        now: LocalDateTime = LocalDateTime.now(),
        allTasks: List<TaskEntity> = emptyList()
    ): Boolean {
        val intervals = if (allTasks.isNotEmpty()) resolveTaskIntervals(allTasks) else resolveTaskIntervals(listOf(task))
        val currentLogicalMin = toLogicalMinutes(now.hour, now.minute)
        val match = intervals.firstOrNull { it.task.id == task.id } ?: return false
        return currentLogicalMin >= match.startLogicalMinutes && currentLogicalMin < match.endLogicalMinutes
    }
}
