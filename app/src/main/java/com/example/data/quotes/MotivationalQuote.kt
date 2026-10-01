package com.example.data.quotes

enum class QuoteCategory {
    STUDY,
    CODING,
    CONSISTENCY,
    DISCIPLINE,
    PERSONAL_GROWTH,
    EXERCISE,
    READING,
    CELEBRATION,
    REST,
    GENERAL
}

enum class TimeContext {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    ANY
}

data class MotivationalQuote(
    val id: String,
    val text: String,
    val author: String = "to-dodo",
    val category: QuoteCategory,
    val suitableTime: TimeContext = TimeContext.ANY
)
