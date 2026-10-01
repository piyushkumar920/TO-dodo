package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CompletionRecordEntity
import com.example.data.db.CreditAwardEntity
import com.example.data.db.TaskEntity
import com.example.data.preferences.SettingsManager
import com.example.data.repository.RoutineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

data class FullDayCelebrationData(
    val date: String,
    val newCreditScore: Int,
    val milestoneMessage: String
)

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = RoutineRepository(database.routineDao())
    private val settingsManager = SettingsManager(application)

    // Helper: Compute logical date for routine tracking (times 00:00-03:59 belong to previous night's routine)
    companion object {
        fun getLogicalDate(now: LocalDateTime = LocalDateTime.now()): LocalDate {
            return if (now.hour < 4) {
                now.toLocalDate().minusDays(1)
            } else {
                now.toLocalDate()
            }
        }
    }

    // Settings flows
    val userName: StateFlow<String> = settingsManager.userNameFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Piyush"
    )
    val theme: StateFlow<String> = settingsManager.themeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "system"
    )
    val notificationsEnabled: StateFlow<Boolean> = settingsManager.notificationsEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val weekStartsMonday: StateFlow<Boolean> = settingsManager.weekStartsMondayFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val onboardingCompleted: StateFlow<Boolean> = settingsManager.onboardingCompletedFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    // Selected Date for viewing/tracking (defaults to logical date)
    private val _selectedDate = MutableStateFlow(getLogicalDate().toString())
    val selectedDate: StateFlow<String> = _selectedDate

    // Week offset for historical week browsing (0 = current week, 1 = previous week, 2 = 2 weeks ago, etc.)
    private val _selectedWeekOffset = MutableStateFlow(0)
    val selectedWeekOffset: StateFlow<Int> = _selectedWeekOffset

    // All tasks from repository
    private val _allTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val allTasks: StateFlow<List<TaskEntity>> = _allTasks

    // All completion records from repository
    private val _allCompletions = MutableStateFlow<List<CompletionRecordEntity>>(emptyList())
    val allCompletions: StateFlow<List<CompletionRecordEntity>> = _allCompletions

    // Credit Awards & Credit Score
    val creditScore: StateFlow<Int> = repository.getCreditScoreCount().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    // Celebration events
    private val _partyPopperTaskId = MutableStateFlow<String?>(null)
    val partyPopperTaskId: StateFlow<String?> = _partyPopperTaskId

    private val _fullDayCelebrationEvent = MutableStateFlow<FullDayCelebrationData?>(null)
    val fullDayCelebrationEvent: StateFlow<FullDayCelebrationData?> = _fullDayCelebrationEvent

    init {
        viewModelScope.launch {
            repository.ensureDefaultTasksSeeded()
        }
        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                _allTasks.value = tasks
            }
        }
        viewModelScope.launch {
            repository.getAllCompletions().collect { comps ->
                _allCompletions.value = comps
            }
        }
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setWeekOffset(offset: Int) {
        _selectedWeekOffset.value = offset.coerceAtLeast(0)
    }

    fun dismissFullDayCelebration() {
        _fullDayCelebrationEvent.value = null
    }

    fun clearPartyPopper() {
        _partyPopperTaskId.value = null
    }

    // Tasks for the currently selected date's day of week
    val tasksForSelectedDate: StateFlow<List<TaskEntity>> = combine(_allTasks, _selectedDate) { tasks, dateStr ->
        try {
            val date = LocalDate.parse(dateStr)
            val dayName = date.dayOfWeek.name
            tasks.filter { it.dayOfWeek.uppercase() == dayName }.sortedBy { it.sortOrder }
        } catch (e: Exception) {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Completions for the currently selected date
    val completionsForSelectedDate: StateFlow<Map<String, Boolean>> = combine(_allCompletions, _selectedDate) { comps, dateStr ->
        comps.filter { it.date == dateStr }.associate { it.taskId to it.completed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Today's progress statistics
    data class DailyProgress(
        val total: Int,
        val completed: Int,
        val percentage: Int
    )

    fun getProgressForDate(dateStr: String): DailyProgress {
        val tasks = _allTasks.value.filter { task ->
            try {
                val d = LocalDate.parse(dateStr)
                task.dayOfWeek.uppercase() == d.dayOfWeek.name
            } catch (e: Exception) {
                false
            }
        }
        val total = tasks.size
        if (total == 0) return DailyProgress(0, 0, 0)
        val comps = _allCompletions.value.filter { it.date == dateStr && it.completed }
        val completedCount = tasks.count { task -> comps.any { it.taskId == task.id } }
        val percentage = ((completedCount.toFloat() / total) * 100).toInt()
        return DailyProgress(total, completedCount, percentage)
    }

    val todayProgress: StateFlow<DailyProgress> = combine(_allTasks, _allCompletions, _selectedDate) { _, _, dateStr ->
        getProgressForDate(dateStr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyProgress(0, 0, 0))

    fun toggleTaskCompletion(taskId: String, completed: Boolean, targetDate: String = _selectedDate.value) {
        viewModelScope.launch {
            if (completed) {
                repository.upsertCompletion(
                    CompletionRecordEntity(
                        id = "${targetDate}_${taskId}",
                        date = targetDate,
                        taskId = taskId,
                        completed = true,
                        completedAt = System.currentTimeMillis()
                    )
                )

                // Trigger individual party popper animation
                _partyPopperTaskId.value = taskId

                // Check for 100% full-day completion
                val tasksForDay = _allTasks.value.filter { task ->
                    try {
                        val d = LocalDate.parse(targetDate)
                        task.dayOfWeek.uppercase() == d.dayOfWeek.name
                    } catch (e: Exception) {
                        false
                    }
                }
                val totalTasks = tasksForDay.size
                if (totalTasks > 0) {
                    val currentCompletions = _allCompletions.value.filter { it.date == targetDate && it.completed }.map { it.taskId }.toMutableSet()
                    currentCompletions.add(taskId)

                    if (currentCompletions.size >= totalTasks) {
                        // All tasks completed for this logical day! Check if already awarded (idempotent)
                        val existingAward = repository.getCreditAwardForDate(targetDate)
                        if (existingAward == null) {
                            repository.insertCreditAward(
                                CreditAwardEntity(
                                    date = targetDate,
                                    creditAwarded = true,
                                    awardedAt = System.currentTimeMillis()
                                )
                            )
                            val newScore = (creditScore.value + 1)
                            val milestoneMsg = getMilestoneMessage(newScore)
                            _fullDayCelebrationEvent.value = FullDayCelebrationData(
                                date = targetDate,
                                newCreditScore = newScore,
                                milestoneMessage = milestoneMsg
                            )
                        }
                    }
                }
            } else {
                repository.deleteCompletion(targetDate, taskId)
            }
        }
    }

    private fun getMilestoneMessage(score: Int): String {
        return when (score) {
            1 -> "Your first full day! ♡"
            3 -> "3 full days in a row! ✨"
            7 -> "7 full days! You're unstoppable! 🐥"
            14 -> "2 whole weeks of 100% completion! 🌟"
            30 -> "30 days of showing up! Master of routine! 🔥"
            50 -> "50 full days completed! True dedication! 🏆"
            100 -> "100 FULL DAYS! Legendary habit builder! 👑"
            else -> "Day complete! You did the whole thing! ♡"
        }
    }

    // Streak calculation: A day counts toward streak if completed tasks >= 70% of scheduled tasks
    val currentStreak: StateFlow<Int> = combine(_allTasks, _allCompletions) { _, comps ->
        calculateStreak()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun calculateStreak(): Int {
        var streak = 0
        var currentDate = getLogicalDate()

        val todayProg = getProgressForDate(currentDate.toString())
        if (todayProg.total > 0 && todayProg.percentage >= 70) {
            streak++
            currentDate = currentDate.minusDays(1)
        } else {
            currentDate = currentDate.minusDays(1)
        }

        while (true) {
            val dateStr = currentDate.toString()
            val prog = getProgressForDate(dateStr)
            if (prog.total > 0 && prog.percentage >= 70) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    // Session statistics counters
    data class SessionStats(
        val totalCompletedTasks: Int,
        val studySessions: Int,
        val projectSessions: Int,
        val guitarSessions: Int,
        val fluteSessions: Int,
        val exerciseSessions: Int,
        val readingSessions: Int
    )

    val sessionStats: StateFlow<SessionStats> = combine(_allTasks, _allCompletions) { tasks, comps ->
        val completedComps = comps.filter { it.completed }
        val taskMap = tasks.associateBy { it.id }

        var study = 0
        var project = 0
        var guitar = 0
        var flute = 0
        var exercise = 0
        var reading = 0

        for (comp in completedComps) {
            val task = taskMap[comp.taskId] ?: continue
            when (task.category.lowercase().trim()) {
                "study" -> study++
                "project / internship" -> project++
                "guitar" -> guitar++
                "flute" -> flute++
                "exercise" -> exercise++
                "reading" -> reading++
            }
        }
        SessionStats(
            totalCompletedTasks = completedComps.size,
            studySessions = study,
            projectSessions = project,
            guitarSessions = guitar,
            fluteSessions = flute,
            exerciseSessions = exercise,
            readingSessions = reading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionStats(0,0,0,0,0,0,0))

    // Category progress across all 10 categories
    data class CategoryProgress(
        val category: String,
        val completedCount: Int,
        val totalScheduledWeekly: Int,
        val percentage: Int
    )

    val categoryProgressList: StateFlow<List<CategoryProgress>> = combine(_allTasks, _allCompletions) { tasks, comps ->
        val allCategories = listOf(
            "Study", "College", "Project / Internship", "Exercise",
            "Guitar", "Flute", "Reading", "Personal", "Rest", "Other"
        )
        val taskMap = tasks.associateBy { it.id }

        allCategories.map { cat ->
            val weeklyTasksForCat = tasks.filter { it.category.equals(cat, ignoreCase = true) }
            val completedCount = comps.count { comp ->
                val task = taskMap[comp.taskId]
                task != null && task.category.equals(cat, ignoreCase = true) && comp.completed
            }
            val weeklyTotal = weeklyTasksForCat.size
            val percentage = if (weeklyTotal > 0) {
                ((completedCount.toFloat() / weeklyTotal) * 100).toInt().coerceAtMost(100)
            } else {
                if (completedCount > 0) 100 else 0
            }
            CategoryProgress(
                category = cat,
                completedCount = completedCount,
                totalScheduledWeekly = weeklyTotal,
                percentage = percentage
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Week days breakdown for selected week offset
    data class DayProgressSummary(
        val dayName: String,
        val dateStr: String,
        val percentage: Int,
        val completed: Int,
        val total: Int,
        val isToday: Boolean
    )

    val currentWeekDays: StateFlow<List<DayProgressSummary>> = combine(_allTasks, _allCompletions, _selectedWeekOffset) { _, _, offset ->
        val baseDate = getLogicalDate().minusWeeks(offset.toLong())
        var monday = baseDate
        while (monday.dayOfWeek != java.time.DayOfWeek.MONDAY) {
            monday = monday.minusDays(1)
        }

        val days = mutableListOf<DayProgressSummary>()
        var current = monday
        for (i in 0..6) {
            val dStr = current.toString()
            val prog = getProgressForDate(dStr)
            val dayName = current.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            days.add(
                DayProgressSummary(
                    dayName = dayName,
                    dateStr = dStr,
                    percentage = prog.percentage,
                    completed = prog.completed,
                    total = prog.total,
                    isToday = dStr == getLogicalDate().toString()
                )
            )
            current = current.plusDays(1)
        }
        days
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings actions
    fun setUserName(name: String) {
        viewModelScope.launch { settingsManager.setUserName(name) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { settingsManager.setTheme(theme) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setNotificationsEnabled(enabled) }
    }

    fun setWeekStartsMonday(startsMonday: Boolean) {
        viewModelScope.launch { settingsManager.setWeekStartsMonday(startsMonday) }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { settingsManager.setOnboardingCompleted(completed) }
    }

    fun resetTodayTasks() {
        viewModelScope.launch {
            val todayStr = getLogicalDate().toString()
            repository.deleteCompletionsForDate(todayStr)
        }
    }

    fun clearAllProgress() {
        viewModelScope.launch {
            repository.clearAllCompletions()
            repository.clearAllCreditAwards()
        }
    }

    // Export Data to JSON string
    fun exportDataJson(): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("appName", "to-dodo")
        root.put("exportedAt", System.currentTimeMillis())

        val compsArray = JSONArray()
        for (comp in _allCompletions.value) {
            val obj = JSONObject().apply {
                put("id", comp.id)
                put("date", comp.date)
                put("taskId", comp.taskId)
                put("completed", comp.completed)
                put("completedAt", comp.completedAt)
            }
            compsArray.put(obj)
        }
        root.put("completionRecords", compsArray)
        return root.toString(2)
    }

    // Import Data from JSON string with strict validation
    fun importDataJson(jsonStr: String): Boolean {
        try {
            val root = JSONObject(jsonStr)
            if (!root.has("completionRecords")) {
                return false
            }
            val compsArray = root.getJSONArray("completionRecords")
            val newRecords = mutableListOf<CompletionRecordEntity>()

            for (i in 0 until compsArray.length()) {
                val obj = compsArray.getJSONObject(i)
                val date = obj.getString("date")
                val taskId = obj.getString("taskId")
                val completed = obj.optBoolean("completed", true)
                val completedAt = obj.optLong("completedAt", System.currentTimeMillis())
                val id = obj.optString("id", "${date}_${taskId}")

                LocalDate.parse(date)

                newRecords.add(
                    CompletionRecordEntity(
                        id = id,
                        date = date,
                        taskId = taskId,
                        completed = completed,
                        completedAt = completedAt
                    )
                )
            }

            viewModelScope.launch {
                repository.clearAllCompletions()
                for (rec in newRecords) {
                    repository.upsertCompletion(rec)
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
