package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completion_records")
data class CompletionRecordEntity(
    @PrimaryKey val id: String, // "${date}_${taskId}"
    val date: String, // "YYYY-MM-DD"
    val taskId: String,
    val completed: Boolean,
    val completedAt: Long
)
