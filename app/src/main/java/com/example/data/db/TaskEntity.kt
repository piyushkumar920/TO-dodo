package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: String, // Legacy primary day name (e.g. "MONDAY", preserved for backward compatibility)
    val title: String,
    val startTime: String,
    val endTime: String,
    val category: String, // "Study", "College", "Project / Internship", "Exercise", "Guitar", "Flute", "Reading", "Personal", "Rest", "Other"
    val sortOrder: Int,
    val daysOfWeek: String = dayOfWeek, // Comma-separated days: "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
    val isEnabled: Boolean = true,
    val reminderEnabled: Boolean = true,
    val effectiveFromDate: String = "2000-01-01", // Takes effect from this logical date (YYYY-MM-DD)
    val effectiveUntilDate: String? = null // Ceases to take effect after this logical date (null = indefinite)
) {
    /**
     * Checks if this recurring task repeats on the given day of week.
     */
    fun repeatsOn(dayName: String): Boolean {
        if (!isEnabled) return false
        val uppercaseDay = dayName.trim().uppercase()
        val daysList = daysOfWeek.split(",").map { it.trim().uppercase() }
        return daysList.contains(uppercaseDay) || dayOfWeek.trim().uppercase() == uppercaseDay
    }

    /**
     * Checks if this task definition was effective on a specific logical date string (YYYY-MM-DD).
     */
    fun isEffectiveOn(dateStr: String): Boolean {
        if (dateStr < effectiveFromDate) return false
        if (effectiveUntilDate != null && dateStr > effectiveUntilDate) return false
        return true
    }
}
