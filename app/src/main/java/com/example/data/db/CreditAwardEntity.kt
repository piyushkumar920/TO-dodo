package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_awards")
data class CreditAwardEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD" logical date
    val creditAwarded: Boolean = true,
    val awardedAt: Long = System.currentTimeMillis()
)
