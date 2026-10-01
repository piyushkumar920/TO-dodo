package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CompletionRecordEntity
import com.example.data.db.CreditAwardEntity
import com.example.data.db.TaskEntity
import com.example.data.preferences.SettingsManager
import com.example.data.quotes.MotivationalQuote
import com.example.data.quotes.MotivationalQuoteEngine
import com.example.data.quotes.MotivationalQuoteLibrary
import com.example.data.quotes.QuoteContext
import com.example.data.repository.RoutineRepository
import com.example.util.CategoryHelper
import com.example.util.NotificationHelper
import com.example.util.RoutineNotificationScheduler
import com.example.util.RoutineTimeEngine
import com.example.widget.ToDodoWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    // Companion delegation to central RoutineTimeEngine
    companion object {
        fun getLogicalDate(now: LocalDateTime = LocalDateTime.now()): LocalDate {
            return RoutineTimeEngine.getLogicalDate(now)
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
    val taskRemindersEnabled: StateFlow<Boolean> = settingsManager.taskRemindersEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val upcomingTaskEnabled: StateFlow<Boolean> = settingsManager.upcomingTaskEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val morningSummaryEnabled: StateFlow<Boolean> = settingsManager.morningSummaryEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val reminderTimingMinutes: StateFlow<Int> = settingsManager.reminderTimingMinutesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 10
    )
    val morningSummaryTime: StateFlow<String> = settingsManager.morningSummaryTimeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "08:00"
    )
    val weekStartsMonday: StateFlow<Boolean> = settingsManager.weekStartsMondayFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val onboardingCompleted: StateFlow<Boolean> = settingsManager.onboardingCompletedFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val customCategories: StateFlow<List<String>> = settingsManager.customCategoriesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allAvailableCategories: StateFlow<List<String>> = customCategories.map { customs ->
        CategoryHelper.DEFAULT_CATEGORIES + customs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryHelper.DEFAULT_CATEGORIES)

    // Time & Date Engine reactive flows
    private val _currentTimeTick = MutableStateFlow(LocalDateTime.now())
    val currentTimeTick: StateFlow<LocalDateTime> = _currentTimeTick.asStateFlow()

    // Flag tracking if the user is viewing today's live routine
    private val _isViewingCurrentLogicalDate = MutableStateFlow(true)

    val actualDate: StateFlow<LocalDate> = _currentTimeTick.map { it.toLocalDate() }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDate.now()
    )

    val currentGreeting: StateFlow<RoutineTimeEngine.GreetingInfo> = _currentTimeTick.map {
        RoutineTimeEngine.getGreetingInfo(it.toLocalTime())
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), RoutineTimeEngine.getGreetingInfo()
    )

    // Selected Date for viewing/tracking (defaults to current logical date)
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

    // Local Motivational Quote (Phase 4)
    private val _currentMotivationalQuote = MutableStateFlow<MotivationalQuote>(
        MotivationalQuoteLibrary.quotes.first()
    )
    val currentMotivationalQuote: StateFlow<MotivationalQuote> = _currentMotivationalQuote.asStateFlow()

    init {
        viewModelScope.launch {
            val isOnboardingDone = settingsManager.onboardingCompletedFlow.first()
            val taskCount = repository.getTaskCount()
            val completionCount = repository.getAllCompletions().first().size
            val creditCount = repository.getCreditScoreCount().first()

            val hasExistingData = (taskCount > 0 || completionCount > 0 || creditCount > 0)
            if (hasExistingData && !isOnboardingDone) {
                // Existing user from Phase 1 or 2 upgrading to Phase 3:
                // Auto-mark onboarding as completed so they are never interrupted!
                settingsManager.setOnboardingCompleted(true)
                settingsManager.setRoutineSetupCompleted(true)
            }
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
        viewModelScope.launch {
            refreshMotivationalQuote(forceNew = false)
        }
        viewModelScope.launch {
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
        // Battery-efficient minute ticker aligned to the top of each minute
        viewModelScope.launch {
            while (isActive) {
                val nowTime = LocalTime.now()
                val millisUntilNextMinute = (60 - nowTime.second) * 1000L - (nowTime.nano / 1_000_000L) + 50L
                delay(millisUntilNextMinute.coerceAtLeast(500L))
                refreshTime()
            }
        }
    }

    fun refreshTime() {
        val now = LocalDateTime.now()
        _currentTimeTick.value = now
        val logicalDateStr = RoutineTimeEngine.getLogicalDate(now).toString()
        // If user is tracking current logical day, auto-update on date / 4am boundaries
        if (_isViewingCurrentLogicalDate.value && _selectedDate.value != logicalDateStr) {
            _selectedDate.value = logicalDateStr
            refreshMotivationalQuote(forceNew = false)
        }
        try {
            ToDodoWidgetProvider.updateWidgets(getApplication())
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun refreshMotivationalQuote(forceNew: Boolean = true) {
        viewModelScope.launch {
            val currentLogicalDateStr = RoutineTimeEngine.getLogicalDate().toString()
            val recentIds = settingsManager.recentQuoteIdsFlow.first()
            val totalTasks = todayProgress.value.total
            val completedTasks = todayProgress.value.completed
            val is100Percent = totalTasks > 0 && completedTasks >= totalTasks
            val curCategory = routineStatus.value.currentTask?.category
            val isFree = tasksForSelectedDate.value.isEmpty()
            val timeContext = MotivationalQuoteEngine.getTimeContext(LocalTime.now().hour)

            val context = QuoteContext(
                is100PercentComplete = is100Percent,
                currentTaskCategory = curCategory,
                isFreeDay = isFree,
                timeOfDay = timeContext
            )

            if (!forceNew) {
                val savedId = settingsManager.selectedQuoteIdFlow.first()
                val savedDate = settingsManager.selectedQuoteDateFlow.first()
                if (savedDate == currentLogicalDateStr && savedId != null) {
                    val existing = MotivationalQuoteLibrary.getById(savedId)
                    if (existing != null) {
                        _currentMotivationalQuote.value = existing
                        return@launch
                    }
                }
            }

            val seed = System.currentTimeMillis()
            val newQuote = MotivationalQuoteEngine.selectQuote(
                context = context,
                recentQuoteIds = recentIds,
                seed = seed
            )
            _currentMotivationalQuote.value = newQuote
            settingsManager.saveSelectedQuote(newQuote.id, currentLogicalDateStr)
        }
    }

    fun setSelectedDate(dateStr: String) {
        val currentLogicalStr = RoutineTimeEngine.getLogicalDate().toString()
        _isViewingCurrentLogicalDate.value = (dateStr == currentLogicalStr)
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
            tasks.filter { task ->
                task.isEffectiveOn(dateStr) && task.repeatsOn(dayName)
            }.sortedWith(compareBy({ RoutineTimeEngine.toLogicalMinutes(it.startTime) }, { it.sortOrder }))
        } catch (e: Exception) {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time Routine Status (CURRENT_TASK, BETWEEN_TASKS, BEFORE_FIRST_TASK, AFTER_LAST_TASK, NO_TASKS_TODAY)
    val routineStatus: StateFlow<RoutineTimeEngine.RoutineStatusInfo> = combine(
        tasksForSelectedDate,
        _currentTimeTick
    ) { tasks, now ->
        RoutineTimeEngine.calculateRoutineStatus(tasks, now)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RoutineTimeEngine.calculateRoutineStatus(emptyList())
    )

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
                task.isEffectiveOn(dateStr) && task.repeatsOn(d.dayOfWeek.name)
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

                // Cancel pending reminder for this completed task
                RoutineNotificationScheduler.cancelTaskReminder(getApplication(), taskId, targetDate)
                NotificationHelper.cancelTaskNotification(getApplication(), taskId)

                // Trigger individual party popper animation
                _partyPopperTaskId.value = taskId

                // Check for 100% full-day completion
                val tasksForDay = _allTasks.value.filter { task ->
                    try {
                        val d = LocalDate.parse(targetDate)
                        task.isEffectiveOn(targetDate) && task.repeatsOn(d.dayOfWeek.name)
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
            // Update widget asynchronously
            ToDodoWidgetProvider.updateWidgets(getApplication())
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
        }
        currentDate = currentDate.minusDays(1)

        val oldestLimit = getLogicalDate().minusYears(1)
        while (currentDate.isAfter(oldestLimit)) {
            val dateStr = currentDate.toString()
            val prog = getProgressForDate(dateStr)
            if (prog.total == 0) {
                // Free day (0 tasks): skip backward without breaking streak and without incrementing
                currentDate = currentDate.minusDays(1)
                continue
            }
            if (prog.percentage >= 70) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                // Non-empty day with < 70% completed: streak breaks
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

    // Category progress across universal defaults and custom categories
    data class CategoryProgress(
        val category: String,
        val completedCount: Int,
        val totalScheduledWeekly: Int,
        val percentage: Int
    )

    val categoryProgressList: StateFlow<List<CategoryProgress>> = combine(
        _allTasks,
        _allCompletions,
        settingsManager.customCategoriesFlow
    ) { tasks, comps, customCats ->
        val allCategories = CategoryHelper.DEFAULT_CATEGORIES + customCats
        val taskMap = tasks.associateBy { it.id }

        allCategories.map { cat ->
            val weeklyTasksForCat = tasks.filter {
                CategoryHelper.mapLegacyCategory(it.category).equals(cat, ignoreCase = true)
            }
            val completedCount = comps.count { comp ->
                val task = taskMap[comp.taskId]
                task != null && CategoryHelper.mapLegacyCategory(task.category).equals(cat, ignoreCase = true) && comp.completed
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

    fun addCustomCategory(name: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        val validationError = CategoryHelper.validateCategoryName(name, customCategories.value)
        if (validationError != null) {
            onResult(false, validationError)
            return
        }
        viewModelScope.launch {
            val success = settingsManager.addCustomCategory(name.trim())
            if (success) {
                onResult(true, null)
            } else {
                onResult(false, "Category already exists")
            }
        }
    }

    fun deleteCustomCategory(categoryName: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            // Reassign affected tasks to "Other" safely
            val tasksWithCat = _allTasks.value.filter { it.category.equals(categoryName, ignoreCase = true) }
            for (task in tasksWithCat) {
                repository.updateTask(task.copy(category = "Other"))
            }
            val currentCustoms = settingsManager.getCustomCategories()
            val updated = currentCustoms.filterNot { it.equals(categoryName, ignoreCase = true) }
            settingsManager.setCustomCategories(updated)
            onResult(true)
        }
    }

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
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(enabled)
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun setTaskRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setTaskRemindersEnabled(enabled)
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun setUpcomingTaskEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setUpcomingTaskEnabled(enabled)
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun setMorningSummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setMorningSummaryEnabled(enabled)
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun setReminderTimingMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsManager.setReminderTimingMinutes(minutes)
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun setMorningSummaryTime(time: String) {
        viewModelScope.launch {
            settingsManager.setMorningSummaryTime(time)
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
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

    // --- Task Customization Methods (Phase 2) ---

    fun addCustomTask(
        title: String,
        category: String,
        startTime: String,
        endTime: String,
        daysOfWeek: Set<String>,
        reminderEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            val nextLogicalDate = RoutineTimeEngine.getLogicalDate().plusDays(1).toString()
            val taskId = "TASK_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val primaryDay = daysOfWeek.firstOrNull() ?: "MONDAY"
            val daysStr = daysOfWeek.joinToString(",")
            val sortOrder = RoutineTimeEngine.toLogicalMinutes(startTime)

            val newTask = TaskEntity(
                id = taskId,
                dayOfWeek = primaryDay,
                title = title.trim(),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                category = category.trim(),
                sortOrder = sortOrder,
                daysOfWeek = daysStr,
                isEnabled = true,
                reminderEnabled = reminderEnabled,
                effectiveFromDate = nextLogicalDate,
                effectiveUntilDate = null
            )
            repository.insertTask(newTask)
            ToDodoWidgetProvider.updateWidgets(getApplication())
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun editCustomTask(
        existingTask: TaskEntity,
        newTitle: String,
        newCategory: String,
        newStartTime: String,
        newEndTime: String,
        newDaysOfWeek: Set<String>,
        newReminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val currentLogicalDate = RoutineTimeEngine.getLogicalDate().toString()
            val nextLogicalDate = RoutineTimeEngine.getLogicalDate().plusDays(1).toString()
            val primaryDay = newDaysOfWeek.firstOrNull() ?: existingTask.dayOfWeek
            val newDaysStr = newDaysOfWeek.joinToString(",")
            val newSortOrder = RoutineTimeEngine.toLogicalMinutes(newStartTime)

            // Archive the existing schedule version up to today (preserving historical schedule)
            repository.updateTask(existingTask.copy(effectiveUntilDate = currentLogicalDate))

            // Create updated version starting next logical day
            val updatedTask = TaskEntity(
                id = "${existingTask.id}_v${System.currentTimeMillis().toString().takeLast(5)}",
                dayOfWeek = primaryDay,
                title = newTitle.trim(),
                startTime = newStartTime.trim(),
                endTime = newEndTime.trim(),
                category = newCategory.trim(),
                sortOrder = newSortOrder,
                daysOfWeek = newDaysStr,
                isEnabled = existingTask.isEnabled,
                reminderEnabled = newReminderEnabled,
                effectiveFromDate = nextLogicalDate,
                effectiveUntilDate = null
            )
            repository.insertTask(updatedTask)
            ToDodoWidgetProvider.updateWidgets(getApplication())
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun deleteCustomTask(task: TaskEntity) {
        viewModelScope.launch {
            val currentLogicalDate = RoutineTimeEngine.getLogicalDate().toString()
            // Setting effectiveUntilDate = currentLogicalDate preserves past completions while removing from future schedule
            repository.updateTask(task.copy(effectiveUntilDate = currentLogicalDate))
            NotificationHelper.cancelTaskNotification(getApplication(), task.id)
            RoutineNotificationScheduler.cancelTaskReminder(getApplication(), task.id, currentLogicalDate)
            ToDodoWidgetProvider.updateWidgets(getApplication())
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun toggleTaskEnabled(taskId: String, enabled: Boolean) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId) ?: return@launch
            repository.updateTask(task.copy(isEnabled = enabled))
            if (!enabled) {
                NotificationHelper.cancelTaskNotification(getApplication(), taskId)
            }
            ToDodoWidgetProvider.updateWidgets(getApplication())
            RoutineNotificationScheduler.scheduleNotifications(getApplication())
        }
    }

    fun loadStarterRoutine() {
        viewModelScope.launch {
            repository.loadStarterRoutine()
            settingsManager.setRoutineSetupCompleted(true)
            ToDodoWidgetProvider.updateWidgets(getApplication())
        }
    }

    fun clearRoutineForCustom() {
        viewModelScope.launch {
            repository.clearAllTasks()
            settingsManager.setRoutineSetupCompleted(true)
            ToDodoWidgetProvider.updateWidgets(getApplication())
        }
    }

    fun clearAllProgress() {
        viewModelScope.launch {
            repository.clearAllCompletions()
            repository.clearAllCreditAwards()
            ToDodoWidgetProvider.updateWidgets(getApplication())
        }
    }

    // Export Data to JSON string (supporting both completion history and custom routine tasks)
    fun exportDataJson(): String {
        val root = JSONObject()
        root.put("version", 3)
        root.put("appName", "to-dodo")
        root.put("userName", userName.value)
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

        val tasksArray = JSONArray()
        for (task in _allTasks.value) {
            val obj = JSONObject().apply {
                put("id", task.id)
                put("dayOfWeek", task.dayOfWeek)
                put("title", task.title)
                put("startTime", task.startTime)
                put("endTime", task.endTime)
                put("category", task.category)
                put("sortOrder", task.sortOrder)
                put("daysOfWeek", task.daysOfWeek)
                put("isEnabled", task.isEnabled)
                put("reminderEnabled", task.reminderEnabled)
                put("effectiveFromDate", task.effectiveFromDate)
                put("effectiveUntilDate", task.effectiveUntilDate ?: JSONObject.NULL)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        val customCatsArray = JSONArray()
        for (cat in customCategories.value) {
            customCatsArray.put(cat)
        }
        root.put("customCategories", customCatsArray)

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

            val newTasks = mutableListOf<TaskEntity>()
            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val id = obj.getString("id")
                    val title = obj.getString("title")
                    val dayOfWeek = obj.optString("dayOfWeek", "MONDAY")
                    val startTime = obj.optString("startTime", "09:00")
                    val endTime = obj.optString("endTime", "10:00")
                    val category = obj.optString("category", "Personal")
                    val sortOrder = obj.optInt("sortOrder", 0)
                    val daysOfWeek = obj.optString("daysOfWeek", dayOfWeek)
                    val isEnabled = obj.optBoolean("isEnabled", true)
                    val reminderEnabled = obj.optBoolean("reminderEnabled", true)
                    val effectiveFromDate = obj.optString("effectiveFromDate", "2000-01-01")
                    val effectiveUntilDate = if (obj.isNull("effectiveUntilDate")) null else obj.optString("effectiveUntilDate")

                    newTasks.add(
                        TaskEntity(
                            id = id,
                            dayOfWeek = dayOfWeek,
                            title = title,
                            startTime = startTime,
                            endTime = endTime,
                            category = category,
                            sortOrder = sortOrder,
                            daysOfWeek = daysOfWeek,
                            isEnabled = isEnabled,
                            reminderEnabled = reminderEnabled,
                            effectiveFromDate = effectiveFromDate,
                            effectiveUntilDate = effectiveUntilDate
                        )
                    )
                }
            }

            viewModelScope.launch {
                if (root.has("userName")) {
                    val importedName = root.getString("userName")
                    if (importedName.isNotBlank()) {
                        settingsManager.setUserName(importedName.trim())
                    }
                }
                if (root.has("customCategories")) {
                    val catArray = root.getJSONArray("customCategories")
                    val importedCats = mutableListOf<String>()
                    for (i in 0 until catArray.length()) {
                        val cat = catArray.getString(i)
                        if (cat.isNotBlank()) importedCats.add(cat.trim())
                    }
                    settingsManager.setCustomCategories(importedCats)
                }
                repository.clearAllCompletions()
                for (rec in newRecords) {
                    repository.upsertCompletion(rec)
                }
                if (newTasks.isNotEmpty()) {
                    repository.clearAllTasks()
                    repository.insertTasks(newTasks)
                }
                ToDodoWidgetProvider.updateWidgets(getApplication())
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
