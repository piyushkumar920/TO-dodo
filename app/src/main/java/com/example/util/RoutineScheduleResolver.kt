package com.example.util

import com.example.data.db.TaskEntity
import java.time.LocalDate

/**
 * Single Authoritative Schedule Resolver for to-dodo.
 *
 * Guarantees that My Routine, Today's Tasks, Calendar, Widgets, and Notifications
 * resolve active routine tasks using the exact same filtering, normalization, and sorting logic.
 */
object RoutineScheduleResolver {

    /**
     * Resolves the effective active tasks for a given [LocalDate].
     *
     * @param tasks Complete list of [TaskEntity] definitions from Room
     * @param date Target date to resolve schedule for
     * @return Ordered list of active [TaskEntity] instances valid for [date]
     */
    fun getEffectiveTasksForDate(tasks: List<TaskEntity>, date: LocalDate): List<TaskEntity> {
        val dateString = date.toString()
        val dayName = date.dayOfWeek.name // e.g. "FRIDAY"

        return tasks.filter { task ->
            task.isEnabled &&
            task.isEffectiveOn(dateString) &&
            task.repeatsOn(dayName)
        }.sortedWith(
            compareBy(
                { RoutineTimeEngine.toLogicalMinutes(it.startTime) },
                { it.sortOrder }
            )
        )
    }

    /**
     * Resolves tasks for a specific day of week name (e.g. "FRIDAY") using a reference date.
     */
    fun getEffectiveTasksForDayName(tasks: List<TaskEntity>, dayName: String, referenceDate: LocalDate = LocalDate.now()): List<TaskEntity> {
        val dateString = referenceDate.toString()
        val uppercaseDay = dayName.trim().uppercase()

        return tasks.filter { task ->
            task.isEnabled &&
            task.isEffectiveOn(dateString) &&
            task.repeatsOn(uppercaseDay)
        }.sortedWith(
            compareBy(
                { RoutineTimeEngine.toLogicalMinutes(it.startTime) },
                { it.sortOrder }
            )
        )
    }

    /**
     * Resolves all tasks (both enabled and disabled) for editing in My Routine for a specific day of week name.
     */
    fun getEditorTasksForDayName(tasks: List<TaskEntity>, dayName: String, referenceDate: LocalDate = LocalDate.now()): List<TaskEntity> {
        val dateString = referenceDate.toString()
        val uppercaseDay = dayName.trim().uppercase()

        return tasks.filter { task ->
            task.isEffectiveOn(dateString) &&
            task.repeatsOn(uppercaseDay)
        }.sortedWith(
            compareBy(
                { RoutineTimeEngine.toLogicalMinutes(it.startTime) },
                { it.sortOrder }
            )
        )
    }
}
