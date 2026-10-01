package com.example.data.repository

import com.example.data.db.CompletionRecordEntity
import com.example.data.db.RoutineDao
import com.example.data.db.TaskEntity
import kotlinx.coroutines.flow.Flow

class RoutineRepository(private val dao: RoutineDao) {

    fun getTasksForDay(day: String): Flow<List<TaskEntity>> = dao.getTasksForDay(day)
    fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()

    fun getCompletionsForDate(date: String): Flow<List<CompletionRecordEntity>> = dao.getCompletionsForDate(date)
    fun getAllCompletions(): Flow<List<CompletionRecordEntity>> = dao.getAllCompletions()

    fun getAllCreditAwards(): Flow<List<com.example.data.db.CreditAwardEntity>> = dao.getAllCreditAwards()
    fun getCreditScoreCount(): Flow<Int> = dao.getCreditScoreCount()
    suspend fun getCreditAwardForDate(date: String) = dao.getCreditAwardForDate(date)
    suspend fun insertCreditAward(award: com.example.data.db.CreditAwardEntity) = dao.insertCreditAward(award)
    suspend fun clearAllCreditAwards() = dao.clearAllCreditAwards()

    suspend fun upsertCompletion(record: CompletionRecordEntity) = dao.upsertCompletion(record)
    suspend fun deleteCompletion(date: String, taskId: String) = dao.deleteCompletion(date, taskId)
    suspend fun deleteCompletionsForDate(date: String) = dao.deleteCompletionsForDate(date)
    suspend fun clearAllCompletions() = dao.clearAllCompletions()

    suspend fun ensureDefaultTasksSeeded() {
        // We can check if any tasks exist by seeding default list
        val defaultTasks = getDefaultTasks()
        dao.insertTasks(defaultTasks)
    }

    private fun getDefaultTasks(): List<TaskEntity> {
        val list = mutableListOf<TaskEntity>()
        var sortOrder = 0

        // --- MONDAY ---
        val mondayTasks = listOf(
            Triple("8:00–8:15", "Room cleaning + make bed", "Personal"),
            Triple("8:15–9:00", "Shower, brush, breakfast, get ready", "Personal"),
            Triple("9:00–9:30", "Study", "Study"),
            Triple("9:30–10:00", "Travel / prepare", "Other"),
            Triple("10:00–12:00", "College", "College"),
            Triple("12:00–1:00", "Lunch", "Rest"),
            Triple("1:00–1:30", "Rest", "Rest"),
            Triple("1:30–3:00", "Study", "Study"),
            Triple("3:00–3:30", "Guitar", "Guitar"),
            Triple("3:30–4:00", "Travel / prepare", "Other"),
            Triple("4:00–6:00", "College", "College"),
            Triple("6:00–6:30", "Return / snack", "Rest"),
            Triple("6:30–7:00", "Exercise", "Exercise"),
            Triple("7:00–8:30", "Project / internship", "Project / Internship"),
            Triple("8:30–9:30", "Dinner", "Rest"),
            Triple("9:30–10:30", "Study", "Study"),
            Triple("10:30–11:00", "Flute", "Flute"),
            Triple("11:00–11:30", "Reading", "Reading"),
            Triple("11:30–12:30", "Free time", "Rest"),
            Triple("12:30–1:00", "Wind down", "Rest"),
            Triple("1:00", "Sleep", "Rest")
        )
        mondayTasks.forEachIndexed { index, (time, title, cat) ->
            list.add(TaskEntity(id = "MONDAY_$index", dayOfWeek = "MONDAY", title = title, startTime = time.split("–")[0].trim(), endTime = if(time.contains("–")) time.split("–")[1].trim() else "", category = cat, sortOrder = sortOrder++))
        }

        // --- TUESDAY ---
        sortOrder = 0
        val tuesdayTasks = listOf(
            Triple("8:00–8:15", "Room cleaning", "Personal"),
            Triple("8:15–9:00", "Shower + breakfast", "Personal"),
            Triple("9:00–9:45", "Study", "Study"),
            Triple("10:00–12:00", "College", "College"),
            Triple("12:00–1:00", "Lunch", "Rest"),
            Triple("1:00–1:30", "Rest", "Rest"),
            Triple("1:30–3:30", "Study", "Study"),
            Triple("3:30–4:00", "Guitar", "Guitar"),
            Triple("4:00–6:00", "Project / internship", "Project / Internship"),
            Triple("6:30–7:00", "Exercise", "Exercise"),
            Triple("7:00–7:30", "Break", "Rest"),
            Triple("7:30–8:15", "Study", "Study"),
            Triple("8:15–9:15", "Dinner", "Rest"),
            Triple("9:15–9:45", "Flute", "Flute"),
            Triple("9:45–10:15", "Reading", "Reading"),
            Triple("10:15–12:30", "Free time / college work", "Rest"),
            Triple("12:30–1:00", "Wind down", "Rest"),
            Triple("1:00", "Sleep", "Rest")
        )
        tuesdayTasks.forEachIndexed { index, (time, title, cat) ->
            list.add(TaskEntity(id = "TUESDAY_$index", dayOfWeek = "TUESDAY", title = title, startTime = time.split("–")[0].trim(), endTime = if(time.contains("–")) time.split("–")[1].trim() else "", category = cat, sortOrder = sortOrder++))
        }

        // --- WEDNESDAY ---
        sortOrder = 0
        val wednesdayTasks = listOf(
            Triple("8:00–8:15", "Room cleaning", "Personal"),
            Triple("8:15–9:00", "Shower + breakfast", "Personal"),
            Triple("9:00–11:00", "Deep study", "Study"),
            Triple("11:00–11:30", "Guitar", "Guitar"),
            Triple("11:30–12:00", "Travel / prepare", "Other"),
            Triple("12:00–1:30", "College", "College"),
            Triple("1:30–2:30", "Lunch", "Rest"),
            Triple("2:30–4:30", "Project / internship", "Project / Internship"),
            Triple("4:30–5:00", "Break", "Rest"),
            Triple("5:00–6:15", "Study", "Study"),
            Triple("6:30–7:00", "Exercise", "Exercise"),
            Triple("7:00–7:30", "Flute", "Flute"),
            Triple("7:30–8:30", "Study", "Study"),
            Triple("8:30–9:30", "Dinner", "Rest"),
            Triple("9:30–10:00", "Reading", "Reading"),
            Triple("10:00–12:30", "Free time / revision", "Rest"),
            Triple("12:30–1:00", "Wind down", "Rest"),
            Triple("1:00", "Sleep", "Rest")
        )
        wednesdayTasks.forEachIndexed { index, (time, title, cat) ->
            list.add(TaskEntity(id = "WEDNESDAY_$index", dayOfWeek = "WEDNESDAY", title = title, startTime = time.split("–")[0].trim(), endTime = if(time.contains("–")) time.split("–")[1].trim() else "", category = cat, sortOrder = sortOrder++))
        }

        // --- THURSDAY ---
        sortOrder = 0
        val thursdayTasks = listOf(
            Triple("8:00–8:15", "Room cleaning", "Personal"),
            Triple("8:15–9:00", "Shower + breakfast", "Personal"),
            Triple("9:00–11:00", "Deep study", "Study"),
            Triple("11:00–11:30", "Guitar", "Guitar"),
            Triple("11:30–12:30", "Study", "Study"),
            Triple("12:30–1:30", "Lunch", "Rest"),
            Triple("1:30–3:30", "Project / internship", "Project / Internship"),
            Triple("3:30–4:00", "Travel / prepare", "Other"),
            Triple("4:00–6:00", "College", "College"),
            Triple("6:00–6:30", "Return / snack", "Rest"),
            Triple("6:30–7:00", "Exercise", "Exercise"),
            Triple("7:00–8:00", "Study", "Study"),
            Triple("8:00–9:00", "Dinner", "Rest"),
            Triple("9:00–9:30", "Flute", "Flute"),
            Triple("9:30–10:00", "Reading", "Reading"),
            Triple("10:00–12:30", "Free time / college work", "Rest"),
            Triple("12:30–1:00", "Wind down", "Rest"),
            Triple("1:00", "Sleep", "Rest")
        )
        thursdayTasks.forEachIndexed { index, (time, title, cat) ->
            list.add(TaskEntity(id = "THURSDAY_$index", dayOfWeek = "THURSDAY", title = title, startTime = time.split("–")[0].trim(), endTime = if(time.contains("–")) time.split("–")[1].trim() else "", category = cat, sortOrder = sortOrder++))
        }

        // --- FRIDAY ---
        sortOrder = 0
        val fridayTasks = listOf(
            Triple("8:00–8:15", "Room cleaning", "Personal"),
            Triple("8:15–9:00", "Shower + breakfast", "Personal"),
            Triple("9:00–11:00", "Study", "Study"),
            Triple("11:00–11:30", "Guitar", "Guitar"),
            Triple("11:30–12:00", "Break", "Rest"),
            Triple("12:00–1:00", "Lunch", "Rest"),
            Triple("1:00–3:00", "Study", "Study"),
            Triple("3:00–3:30", "Flute", "Flute"),
            Triple("3:30–6:00", "Project / internship", "Project / Internship"),
            Triple("6:30–7:00", "Exercise", "Exercise"),
            Triple("7:00–8:00", "Free time", "Rest"),
            Triple("8:00–9:00", "Dinner", "Rest"),
            Triple("9:00–9:30", "Reading", "Reading"),
            Triple("9:30–10:00", "Revision", "Study"),
            Triple("10:00–12:30", "Free time", "Rest"),
            Triple("12:30–1:00", "Wind down", "Rest"),
            Triple("1:00", "Sleep", "Rest")
        )
        fridayTasks.forEachIndexed { index, (time, title, cat) ->
            list.add(TaskEntity(id = "FRIDAY_$index", dayOfWeek = "FRIDAY", title = title, startTime = time.split("–")[0].trim(), endTime = if(time.contains("–")) time.split("–")[1].trim() else "", category = cat, sortOrder = sortOrder++))
        }

        // --- SATURDAY ---
        sortOrder = 0
        val saturdayTasks = listOf(
            Triple("8:00–8:15", "Room cleaning", "Personal"),
            Triple("8:15–9:00", "Shower + breakfast", "Personal"),
            Triple("9:00–11:00", "Study", "Study"),
            Triple("11:00–11:30", "Guitar", "Guitar"),
            Triple("11:30–12:00", "Break", "Rest"),
            Triple("12:00–1:00", "Lunch", "Rest"),
            Triple("1:00–3:00", "Study", "Study"),
            Triple("3:00–3:30", "Flute", "Flute"),
            Triple("3:30–6:30", "Project / internship", "Project / Internship"),
            Triple("6:30–7:00", "Exercise", "Exercise"),
            Triple("7:00–8:00", "Free time", "Rest"),
            Triple("8:00–9:00", "Dinner", "Rest"),
            Triple("9:00–9:30", "Reading", "Reading"),
            Triple("9:30–10:00", "Revision", "Study"),
            Triple("10:00–12:30", "Free time", "Rest"),
            Triple("12:30–1:00", "Wind down", "Rest"),
            Triple("1:00", "Sleep", "Rest")
        )
        saturdayTasks.forEachIndexed { index, (time, title, cat) ->
            list.add(TaskEntity(id = "SATURDAY_$index", dayOfWeek = "SATURDAY", title = title, startTime = time.split("–")[0].trim(), endTime = if(time.contains("–")) time.split("–")[1].trim() else "", category = cat, sortOrder = sortOrder++))
        }

        // --- SUNDAY (No scheduled exercise) ---
        sortOrder = 0
        val sundayTasks = listOf(
            Triple("8:00–8:15", "Room cleaning", "Personal"),
            Triple("8:15–9:00", "Shower + breakfast", "Personal"),
            Triple("9:00–11:00", "Study", "Study"),
            Triple("11:00–11:30", "Guitar", "Guitar"),
            Triple("11:30–12:00", "Break", "Rest"),
            Triple("12:00–1:00", "Lunch", "Rest"),
            Triple("1:00–3:00", "Study", "Study"),
            Triple("3:00–5:00", "Project / internship", "Project / Internship"),
            Triple("5:00–5:30", "Flute", "Flute"),
            Triple("5:30–6:00", "Weekly review", "Study"),
            Triple("6:00–7:00", "Deeper room cleaning + organization", "Personal"),
            Triple("7:00–8:00", "Free time", "Rest"),
            Triple("8:00–9:00", "Dinner", "Rest"),
            Triple("9:00–9:30", "Reading", "Reading"),
            Triple("9:30–10:00", "Plan next week", "Study"),
            Triple("10:00–12:30", "Relax", "Rest"),
            Triple("12:30–1:00", "Wind down", "Rest"),
            Triple("1:00", "Sleep", "Rest")
        )
        sundayTasks.forEachIndexed { index, (time, title, cat) ->
            list.add(TaskEntity(id = "SUNDAY_$index", dayOfWeek = "SUNDAY", title = title, startTime = time.split("–")[0].trim(), endTime = if(time.contains("–")) time.split("–")[1].trim() else "", category = cat, sortOrder = sortOrder++))
        }

        return list
    }
}
