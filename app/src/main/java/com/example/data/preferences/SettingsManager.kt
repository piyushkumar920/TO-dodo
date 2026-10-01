package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "routine_settings")

class SettingsManager(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme_mode") // "system", "light", "dark"
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        val WEEK_STARTS_MONDAY = booleanPreferencesKey("week_starts_monday")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ROUTINE_SETUP_COMPLETED = booleanPreferencesKey("routine_setup_completed")
        val SELECTED_QUOTE_ID = stringPreferencesKey("selected_quote_id")
        val SELECTED_QUOTE_DATE = stringPreferencesKey("selected_quote_date")
        val RECENT_QUOTE_IDS = stringPreferencesKey("recent_quote_ids")
        val TASK_REMINDERS_KEY = booleanPreferencesKey("task_reminders_enabled")
        val UPCOMING_TASK_KEY = booleanPreferencesKey("upcoming_task_enabled")
        val MORNING_SUMMARY_KEY = booleanPreferencesKey("morning_summary_enabled")
        val REMINDER_TIMING_MINUTES = androidx.datastore.preferences.core.intPreferencesKey("reminder_timing_minutes")
        val MORNING_SUMMARY_TIME = stringPreferencesKey("morning_summary_time")
        val CUSTOM_CATEGORIES_KEY = stringPreferencesKey("custom_categories_json")
    }

    val customCategoriesFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val jsonStr = preferences[CUSTOM_CATEGORIES_KEY] ?: return@map emptyList<String>()
        try {
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val item = array.optString(i)?.trim() ?: ""
                if (item.isNotEmpty() && !list.contains(item)) {
                    list.add(item)
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "Piyush"
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "system"
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_KEY] ?: true
    }

    val taskRemindersEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TASK_REMINDERS_KEY] ?: true
    }

    val upcomingTaskEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[UPCOMING_TASK_KEY] ?: false
    }

    val morningSummaryEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MORNING_SUMMARY_KEY] ?: false
    }

    val reminderTimingMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_TIMING_MINUTES] ?: 10
    }

    val morningSummaryTimeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MORNING_SUMMARY_TIME] ?: "08:00"
    }

    val weekStartsMondayFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WEEK_STARTS_MONDAY] ?: true
    }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    val routineSetupCompletedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ROUTINE_SETUP_COMPLETED] ?: false
    }

    val selectedQuoteIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_QUOTE_ID]
    }

    val selectedQuoteDateFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_QUOTE_DATE]
    }

    val recentQuoteIdsFlow: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[RECENT_QUOTE_IDS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TASK_REMINDERS_KEY] = enabled
        }
    }

    suspend fun setUpcomingTaskEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[UPCOMING_TASK_KEY] = enabled
        }
    }

    suspend fun setMorningSummaryEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MORNING_SUMMARY_KEY] = enabled
        }
    }

    suspend fun setReminderTimingMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_TIMING_MINUTES] = minutes
        }
    }

    suspend fun setMorningSummaryTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[MORNING_SUMMARY_TIME] = time
        }
    }

    suspend fun setWeekStartsMonday(startsMonday: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WEEK_STARTS_MONDAY] = startsMonday
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setRoutineSetupCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ROUTINE_SETUP_COMPLETED] = completed
        }
    }

    suspend fun saveSelectedQuote(quoteId: String, logicalDate: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_QUOTE_ID] = quoteId
            preferences[SELECTED_QUOTE_DATE] = logicalDate
            val currentRecent = preferences[RECENT_QUOTE_IDS]?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
            currentRecent.remove(quoteId)
            currentRecent.add(0, quoteId)
            val trimmedRecent = currentRecent.take(15).joinToString(",")
            preferences[RECENT_QUOTE_IDS] = trimmedRecent
        }
    }

    suspend fun getCustomCategories(): List<String> {
        val jsonStr = context.dataStore.data.map { it[CUSTOM_CATEGORIES_KEY] }.first() ?: return emptyList()
        return try {
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val item = array.optString(i)?.trim() ?: ""
                if (item.isNotEmpty() && !list.contains(item)) {
                    list.add(item)
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addCustomCategory(category: String): Boolean {
        val trimmed = category.trim()
        if (trimmed.isEmpty() || trimmed.length > com.example.util.CategoryHelper.MAX_CATEGORY_LENGTH) return false
        var added = false
        context.dataStore.edit { preferences ->
            val jsonStr = preferences[CUSTOM_CATEGORIES_KEY] ?: "[]"
            val list = mutableListOf<String>()
            try {
                val array = org.json.JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val item = array.optString(i)?.trim() ?: ""
                    if (item.isNotEmpty()) list.add(item)
                }
            } catch (_: Exception) {}

            val isDuplicate = list.any { it.equals(trimmed, ignoreCase = true) } ||
                    com.example.util.CategoryHelper.DEFAULT_CATEGORIES.any { it.equals(trimmed, ignoreCase = true) }
            if (!isDuplicate) {
                list.add(trimmed)
                preferences[CUSTOM_CATEGORIES_KEY] = org.json.JSONArray(list).toString()
                added = true
            }
        }
        return added
    }

    suspend fun setCustomCategories(categories: List<String>) {
        val list = mutableListOf<String>()
        for (cat in categories) {
            val trimmed = cat.trim()
            if (trimmed.isNotEmpty() && trimmed.length <= com.example.util.CategoryHelper.MAX_CATEGORY_LENGTH) {
                val isDuplicate = list.any { it.equals(trimmed, ignoreCase = true) } ||
                        com.example.util.CategoryHelper.DEFAULT_CATEGORIES.any { it.equals(trimmed, ignoreCase = true) }
                if (!isDuplicate) {
                    list.add(trimmed)
                }
            }
        }
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_CATEGORIES_KEY] = org.json.JSONArray(list).toString()
        }
    }
}
