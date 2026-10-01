package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.SettingsManager
import com.example.data.quotes.MotivationalQuoteEngine
import com.example.data.quotes.MotivationalQuoteLibrary
import com.example.data.quotes.QuoteCategory
import com.example.data.quotes.QuoteContext
import com.example.data.quotes.TimeContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MotivationalQuoteSystemTest {

    @Test
    fun testQuoteLibraryContainsAtLeast50Quotes() {
        val totalQuotes = MotivationalQuoteLibrary.quotes.size
        assertTrue("Quote library must contain at least 50 quotes, found $totalQuotes", totalQuotes >= 50)
    }

    @Test
    fun testQuoteIdsAreUnique() {
        val allIds = MotivationalQuoteLibrary.quotes.map { it.id }
        val uniqueIds = allIds.toSet()
        assertEquals("All quote IDs must be strictly unique", allIds.size, uniqueIds.size)
    }

    @Test
    fun testQuoteFieldsAreValid() {
        for (q in MotivationalQuoteLibrary.quotes) {
            assertTrue("Quote ID cannot be blank", q.id.isNotBlank())
            assertTrue("Quote text cannot be blank", q.text.isNotBlank())
            assertTrue("Quote author cannot be blank", q.author.isNotBlank())
            assertNotNull("Category must be present", q.category)
            assertNotNull("Suitable time must be present", q.suitableTime)
        }
    }

    @Test
    fun testQuoteSelectionReturnsValidQuote() {
        val context = QuoteContext(
            is100PercentComplete = false,
            currentTaskCategory = null,
            isFreeDay = false,
            timeOfDay = TimeContext.MORNING
        )
        val selected = MotivationalQuoteEngine.selectQuote(context)
        assertNotNull(selected)
        assertTrue(selected.text.isNotBlank())
        assertNotNull(MotivationalQuoteLibrary.getById(selected.id))
    }

    @Test
    fun testRepetitionPreventionDoesNotImmediatelyRepeat() {
        val context = QuoteContext(
            is100PercentComplete = false,
            currentTaskCategory = "Study",
            isFreeDay = false,
            timeOfDay = TimeContext.ANY
        )
        val firstQuote = MotivationalQuoteEngine.selectQuote(context, recentQuoteIds = emptyList(), seed = 42L)

        // Select next quote with firstQuote in recent history
        val secondQuote = MotivationalQuoteEngine.selectQuote(context, recentQuoteIds = listOf(firstQuote.id), seed = 42L)

        assertNotEquals("The quote must not immediately repeat when present in recent history", firstQuote.id, secondQuote.id)
    }

    @Test
    fun testStudyTaskPrefersStudyDisciplineConsistency() {
        val context = QuoteContext(currentTaskCategory = "Study")
        val candidateCategories = MotivationalQuoteEngine.getCandidateCategories(context)
        assertTrue(candidateCategories.contains(QuoteCategory.STUDY))
        assertTrue(candidateCategories.contains(QuoteCategory.DISCIPLINE))
        assertTrue(candidateCategories.contains(QuoteCategory.CONSISTENCY))

        val quote = MotivationalQuoteEngine.selectQuote(context)
        assertTrue(quote.category in candidateCategories)
    }

    @Test
    fun testCodingTaskPrefersCodingConsistency() {
        val context = QuoteContext(currentTaskCategory = "Project / Internship")
        val candidateCategories = MotivationalQuoteEngine.getCandidateCategories(context)
        assertTrue(candidateCategories.contains(QuoteCategory.CODING))
        assertTrue(candidateCategories.contains(QuoteCategory.CONSISTENCY))

        val quote = MotivationalQuoteEngine.selectQuote(context)
        assertTrue(quote.category in candidateCategories)
    }

    @Test
    fun testExerciseTaskPrefersExerciseDiscipline() {
        val context = QuoteContext(currentTaskCategory = "Exercise")
        val candidateCategories = MotivationalQuoteEngine.getCandidateCategories(context)
        assertTrue(candidateCategories.contains(QuoteCategory.EXERCISE))
        assertTrue(candidateCategories.contains(QuoteCategory.DISCIPLINE))

        val quote = MotivationalQuoteEngine.selectQuote(context)
        assertTrue(quote.category in candidateCategories)
    }

    @Test
    fun testReadingTaskPrefersReadingPersonalGrowth() {
        val context = QuoteContext(currentTaskCategory = "Reading")
        val candidateCategories = MotivationalQuoteEngine.getCandidateCategories(context)
        assertTrue(candidateCategories.contains(QuoteCategory.READING))
        assertTrue(candidateCategories.contains(QuoteCategory.PERSONAL_GROWTH))

        val quote = MotivationalQuoteEngine.selectQuote(context)
        assertTrue(quote.category in candidateCategories)
    }

    @Test
    fun testFreeDayPrefersRestGeneralPersonalGrowth() {
        val context = QuoteContext(isFreeDay = true)
        val candidateCategories = MotivationalQuoteEngine.getCandidateCategories(context)
        assertTrue(candidateCategories.contains(QuoteCategory.REST))
        assertTrue(candidateCategories.contains(QuoteCategory.GENERAL))
        assertTrue(candidateCategories.contains(QuoteCategory.PERSONAL_GROWTH))

        val quote = MotivationalQuoteEngine.selectQuote(context)
        assertTrue(quote.category in candidateCategories)
    }

    @Test
    fun test100PercentDayPrefersCelebration() {
        val context = QuoteContext(is100PercentComplete = true)
        val candidateCategories = MotivationalQuoteEngine.getCandidateCategories(context)
        assertEquals(listOf(QuoteCategory.CELEBRATION), candidateCategories)

        val quote = MotivationalQuoteEngine.selectQuote(context)
        assertEquals(QuoteCategory.CELEBRATION, quote.category)
    }

    @Test
    fun testQuoteSelectionWithNoCurrentTaskAndEmptyRoutine() {
        // No current task
        val contextNoTask = QuoteContext(currentTaskCategory = null, isFreeDay = false)
        val quote1 = MotivationalQuoteEngine.selectQuote(contextNoTask)
        assertNotNull(quote1)

        // Empty routine overall
        val contextEmpty = QuoteContext(currentTaskCategory = null, isFreeDay = true)
        val quote2 = MotivationalQuoteEngine.selectQuote(contextEmpty)
        assertNotNull(quote2)
    }

    @Test
    fun testTimeContextDerivation() {
        assertEquals(TimeContext.MORNING, MotivationalQuoteEngine.getTimeContext(8))
        assertEquals(TimeContext.AFTERNOON, MotivationalQuoteEngine.getTimeContext(14))
        assertEquals(TimeContext.EVENING, MotivationalQuoteEngine.getTimeContext(19))
        assertEquals(TimeContext.NIGHT, MotivationalQuoteEngine.getTimeContext(23))
        assertEquals(TimeContext.NIGHT, MotivationalQuoteEngine.getTimeContext(2))
    }

    @Test
    fun testDataStoreQuotePersistenceAndStability() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val settingsManager = SettingsManager(context)

            val quoteId = "cons_01"
            val logicalDate = "2026-10-02"

            settingsManager.saveSelectedQuote(quoteId, logicalDate)

            assertEquals(quoteId, settingsManager.selectedQuoteIdFlow.first())
            assertEquals(logicalDate, settingsManager.selectedQuoteDateFlow.first())

            val recents = settingsManager.recentQuoteIdsFlow.first()
            assertTrue(recents.contains(quoteId))
            assertEquals(quoteId, recents.first())
        }
    }
}
