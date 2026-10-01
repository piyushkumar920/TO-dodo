package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val dayOfWeek: String, // "MONDAY", "TUESDAY", etc.
    val title: String,
    val startTime: String,
    val endTime: String,
    val category: String, // "Study", "College", "Project / Internship", "Exercise", "Guitar", "Flute", "Reading", "Personal", "Rest", "Other"
    val sortOrder: Int
)
