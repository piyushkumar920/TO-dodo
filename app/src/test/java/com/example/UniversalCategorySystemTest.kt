package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.SettingsManager
import com.example.data.quotes.MotivationalQuoteEngine
import com.example.data.quotes.QuoteContext
import com.example.util.CategoryHelper
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UniversalCategorySystemTest {

    @Test
    fun testDefaultCategoriesExistAndCount() {
        assertEquals(11, CategoryHelper.DEFAULT_CATEGORIES.size)
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Study & Learning"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Work"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Health & Fitness"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Personal"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Family"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Projects"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Hobbies"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Self Growth"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Errands"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Rest"))
        assertTrue(CategoryHelper.DEFAULT_CATEGORIES.contains("Other"))
    }

    @Test
    fun testCategoryValidationEmptyAndWhitespace() {
        val existing = listOf("Photography")
        assertNotNull(CategoryHelper.validateCategoryName("", existing))
        assertNotNull(CategoryHelper.validateCategoryName("   ", existing))
    }

    @Test
    fun testCategoryValidationMaxLength() {
        val longName = "A".repeat(31)
        val existing = emptyList<String>()
        val error = CategoryHelper.validateCategoryName(longName, existing)
        assertNotNull("Should reject names longer than 30 chars", error)

        val validName = "A".repeat(30)
        assertNull(CategoryHelper.validateCategoryName(validName, existing))
    }

    @Test
    fun testCategoryDuplicatePrevention() {
        val existing = listOf("Photography", "Cooking")
        // Exact duplicate
        assertNotNull(CategoryHelper.validateCategoryName("Photography", existing))
        // Case-insensitive duplicate
        assertNotNull(CategoryHelper.validateCategoryName("PHOTOGRAPHY", existing))
        assertNotNull(CategoryHelper.validateCategoryName("  cooking  ", existing))
        // Duplicate against default categories
        assertNotNull(CategoryHelper.validateCategoryName("Work", existing))
        assertNotNull(CategoryHelper.validateCategoryName("study & learning", existing))
    }

    @Test
    fun testLegacyCategoryMapping() {
        assertEquals("Study & Learning", CategoryHelper.mapLegacyCategory("Study"))
        assertEquals("Study & Learning", CategoryHelper.mapLegacyCategory("College"))
        assertEquals("Study & Learning", CategoryHelper.mapLegacyCategory("Reading"))
        assertEquals("Projects", CategoryHelper.mapLegacyCategory("Project / Internship"))
        assertEquals("Health & Fitness", CategoryHelper.mapLegacyCategory("Exercise"))
        assertEquals("Hobbies", CategoryHelper.mapLegacyCategory("Guitar"))
        assertEquals("Hobbies", CategoryHelper.mapLegacyCategory("Flute"))
        assertEquals("Personal", CategoryHelper.mapLegacyCategory("Personal"))
        assertEquals("Rest", CategoryHelper.mapLegacyCategory("Rest"))
        assertEquals("Other", CategoryHelper.mapLegacyCategory("Other"))
        assertEquals("CustomCat", CategoryHelper.mapLegacyCategory("CustomCat"))
    }

    @Test
    fun testNotificationEmojiMapping() {
        assertEquals("📚", NotificationHelper.getCategoryEmoji("Study & Learning"))
        assertEquals("💼", NotificationHelper.getCategoryEmoji("Work"))
        assertEquals("🏃", NotificationHelper.getCategoryEmoji("Health & Fitness"))
        assertEquals("🎸", NotificationHelper.getCategoryEmoji("Guitar"))
        assertEquals("🎨", NotificationHelper.getCategoryEmoji("Hobbies"))
        assertEquals("⭐", NotificationHelper.getCategoryEmoji("Photography"))
    }

    @Test
    fun testQuoteEngineWithUniversalAndCustomCategories() {
        val contextUniversal = QuoteContext(currentTaskCategory = "Study & Learning")
        val categoriesUniversal = MotivationalQuoteEngine.getCandidateCategories(contextUniversal)
        assertFalse(categoriesUniversal.isEmpty())

        val contextCustom = QuoteContext(currentTaskCategory = "Photography")
        val categoriesCustom = MotivationalQuoteEngine.getCandidateCategories(contextCustom)
        assertFalse("Custom categories should receive safe fallback quotes", categoriesCustom.isEmpty())
    }

    @Test
    fun testSettingsManagerCustomCategoryPersistence() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val settings = SettingsManager(context)

        // Clear
        settings.setCustomCategories(emptyList())
        assertTrue(settings.getCustomCategories().isEmpty())

        // Add custom category
        val added = settings.addCustomCategory("Gardening")
        assertTrue(added)

        val list = settings.getCustomCategories()
        assertTrue(list.contains("Gardening"))

        // Add duplicate should return false
        val addedDuplicate = settings.addCustomCategory("gardening")
        assertFalse(addedDuplicate)
    }
}
