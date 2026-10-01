package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM tasks WHERE dayOfWeek = :day ORDER BY sortOrder ASC")
    fun getTasksForDay(day: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("SELECT * FROM completion_records WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<CompletionRecordEntity>>

    @Query("SELECT * FROM completion_records")
    fun getAllCompletions(): Flow<List<CompletionRecordEntity>>

    @Query("SELECT * FROM completion_records WHERE date = :date AND taskId = :taskId LIMIT 1")
    suspend fun getCompletion(date: String, taskId: String): CompletionRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(record: CompletionRecordEntity)

    @Query("DELETE FROM completion_records WHERE date = :date AND taskId = :taskId")
    suspend fun deleteCompletion(date: String, taskId: String)

    @Query("DELETE FROM completion_records WHERE date = :date")
    suspend fun deleteCompletionsForDate(date: String)

    @Query("DELETE FROM completion_records")
    suspend fun clearAllCompletions()

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    // Credit Awards queries
    @Query("SELECT * FROM credit_awards")
    fun getAllCreditAwards(): Flow<List<CreditAwardEntity>>

    @Query("SELECT COUNT(*) FROM credit_awards WHERE creditAwarded = 1")
    fun getCreditScoreCount(): Flow<Int>

    @Query("SELECT * FROM credit_awards WHERE date = :date LIMIT 1")
    suspend fun getCreditAwardForDate(date: String): CreditAwardEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCreditAward(award: CreditAwardEntity): Long

    @Query("DELETE FROM credit_awards")
    suspend fun clearAllCreditAwards()
}
