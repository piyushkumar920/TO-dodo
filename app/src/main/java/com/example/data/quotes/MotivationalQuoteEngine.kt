package com.example.data.quotes

import kotlin.math.abs

data class QuoteContext(
    val is100PercentComplete: Boolean = false,
    val currentTaskCategory: String? = null,
    val isFreeDay: Boolean = false,
    val timeOfDay: TimeContext = TimeContext.ANY
)

object MotivationalQuoteEngine {

    fun getTimeContext(hour: Int): TimeContext {
        return when (hour) {
            in 4..11 -> TimeContext.MORNING
            in 12..16 -> TimeContext.AFTERNOON
            in 17..20 -> TimeContext.EVENING
            else -> TimeContext.NIGHT
        }
    }

    fun getCandidateCategories(context: QuoteContext): List<QuoteCategory> {
        if (context.is100PercentComplete) {
            return listOf(QuoteCategory.CELEBRATION)
        }
        val taskCat = context.currentTaskCategory
        if (!taskCat.isNullOrBlank()) {
            val normalized = taskCat.trim()
            return when (normalized.lowercase()) {
                // Universal default categories
                "study & learning" -> listOf(QuoteCategory.STUDY, QuoteCategory.CONSISTENCY, QuoteCategory.DISCIPLINE)
                "work" -> listOf(QuoteCategory.GENERAL, QuoteCategory.DISCIPLINE, QuoteCategory.CONSISTENCY)
                "health & fitness" -> listOf(QuoteCategory.EXERCISE, QuoteCategory.DISCIPLINE)
                "projects" -> listOf(QuoteCategory.PERSONAL_GROWTH, QuoteCategory.CONSISTENCY)
                "hobbies" -> listOf(QuoteCategory.PERSONAL_GROWTH, QuoteCategory.CONSISTENCY)
                "self growth" -> listOf(QuoteCategory.PERSONAL_GROWTH, QuoteCategory.CONSISTENCY)
                "family" -> listOf(QuoteCategory.GENERAL, QuoteCategory.PERSONAL_GROWTH)
                "errands" -> listOf(QuoteCategory.GENERAL, QuoteCategory.CONSISTENCY)
                "other" -> listOf(QuoteCategory.GENERAL, QuoteCategory.CONSISTENCY)
                // Legacy categories (explicit backward compatibility)
                "study", "college" -> listOf(QuoteCategory.STUDY, QuoteCategory.DISCIPLINE, QuoteCategory.CONSISTENCY)
                "project / internship" -> listOf(QuoteCategory.CODING, QuoteCategory.PERSONAL_GROWTH, QuoteCategory.CONSISTENCY)
                "exercise" -> listOf(QuoteCategory.EXERCISE, QuoteCategory.DISCIPLINE)
                "reading" -> listOf(QuoteCategory.READING, QuoteCategory.PERSONAL_GROWTH)
                "guitar", "flute" -> listOf(QuoteCategory.PERSONAL_GROWTH, QuoteCategory.CONSISTENCY)
                "rest" -> listOf(QuoteCategory.REST, QuoteCategory.PERSONAL_GROWTH)
                "personal" -> listOf(QuoteCategory.PERSONAL_GROWTH, QuoteCategory.GENERAL)
                // Arbitrary custom categories: Photography, Freelancing, Cooking, etc.
                else -> listOf(QuoteCategory.GENERAL, QuoteCategory.CONSISTENCY, QuoteCategory.PERSONAL_GROWTH)
            }
        }
        if (context.isFreeDay) {
            return listOf(QuoteCategory.REST, QuoteCategory.GENERAL, QuoteCategory.PERSONAL_GROWTH)
        }
        return listOf(QuoteCategory.GENERAL, QuoteCategory.CONSISTENCY, QuoteCategory.DISCIPLINE, QuoteCategory.PERSONAL_GROWTH)
    }

    fun selectQuote(
        context: QuoteContext,
        recentQuoteIds: List<String> = emptyList(),
        seed: Long = System.currentTimeMillis()
    ): MotivationalQuote {
        val targetCategories = getCandidateCategories(context)
        val categoryPool = MotivationalQuoteLibrary.quotes.filter { it.category in targetCategories }

        // Secondary filter: time suitability if specified
        val timeFilteredPool = if (context.timeOfDay != TimeContext.ANY) {
            val matching = categoryPool.filter { it.suitableTime == context.timeOfDay || it.suitableTime == TimeContext.ANY }
            if (matching.isNotEmpty()) matching else categoryPool
        } else {
            categoryPool
        }

        // Repetition prevention: exclude recent IDs
        val unshownPool = timeFilteredPool.filter { it.id !in recentQuoteIds }

        val poolToChooseFrom = when {
            unshownPool.isNotEmpty() -> unshownPool
            // If all matched have been shown, at least avoid the immediate last shown quote
            recentQuoteIds.isNotEmpty() -> {
                val nonImmediate = timeFilteredPool.filter { it.id != recentQuoteIds.first() }
                if (nonImmediate.isNotEmpty()) nonImmediate else timeFilteredPool
            }
            timeFilteredPool.isNotEmpty() -> timeFilteredPool
            else -> MotivationalQuoteLibrary.quotes
        }

        val positiveIndex = abs(seed % poolToChooseFrom.size).toInt()
        return poolToChooseFrom[positiveIndex]
    }
}
