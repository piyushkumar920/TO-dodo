package com.example.util

object CategoryHelper {
    /**
     * Universal default categories that are broad and lifestyle-neutral.
     */
    val DEFAULT_CATEGORIES = listOf(
        "Study & Learning",
        "Work",
        "Health & Fitness",
        "Personal",
        "Family",
        "Projects",
        "Hobbies",
        "Self Growth",
        "Errands",
        "Rest",
        "Other"
    )

    const val MAX_CATEGORY_LENGTH = 30

    /**
     * Maps legacy categories to their compatible universal category.
     * Suggested mapping:
     * - Study -> Study & Learning
     * - College -> Study & Learning
     * - Project / Internship -> Projects
     * - Exercise -> Health & Fitness
     * - Guitar -> Hobbies
     * - Flute -> Hobbies
     * - Reading -> Study & Learning
     * - Personal -> Personal
     * - Rest -> Rest
     * - Other -> Other
     */
    fun mapLegacyCategory(rawCategory: String): String {
        val trimmed = rawCategory.trim()
        return when (trimmed) {
            "Study", "College", "Reading" -> "Study & Learning"
            "Project / Internship" -> "Projects"
            "Exercise" -> "Health & Fitness"
            "Guitar", "Flute" -> "Hobbies"
            "Personal" -> "Personal"
            "Rest" -> "Rest"
            "Other" -> "Other"
            else -> when (trimmed.lowercase()) {
                "study", "college", "reading", "study & learning" -> "Study & Learning"
                "project / internship", "projects", "project" -> "Projects"
                "exercise", "health & fitness" -> "Health & Fitness"
                "guitar", "flute", "hobbies", "hobby" -> "Hobbies"
                "personal" -> "Personal"
                "rest" -> "Rest"
                "work" -> "Work"
                "family" -> "Family"
                "self growth" -> "Self Growth"
                "errands" -> "Errands"
                "other" -> "Other"
                else -> trimmed
            }
        }
    }

    /**
     * Validates a candidate custom category name.
     * Returns an error message if invalid, or null if valid.
     */
    fun validateCategoryName(name: String, existingCustomCategories: List<String>): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return "Category name cannot be empty"
        }
        if (trimmed.length > MAX_CATEGORY_LENGTH) {
            return "Category name cannot exceed $MAX_CATEGORY_LENGTH characters"
        }
        val exists = DEFAULT_CATEGORIES.any { it.equals(trimmed, ignoreCase = true) } ||
                existingCustomCategories.any { it.equals(trimmed, ignoreCase = true) }
        if (exists) {
            return "Category already exists"
        }
        return null
    }
}
