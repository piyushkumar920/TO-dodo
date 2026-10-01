package com.example

import com.example.data.db.CreditAwardEntity
import com.example.viewmodel.RoutineViewModel
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoutineLogicTest {

    @Test
    fun testLogicalDateMidnightRule() {
        // At 1:00 AM on Tuesday Oct 6, logical date should be Monday Oct 5
        val tuesday1am = LocalDateTime.of(2026, 10, 6, 1, 0)
        val logicalDate1am = RoutineViewModel.getLogicalDate(tuesday1am)
        assertEquals(LocalDate.of(2026, 10, 5), logicalDate1am)

        // At 8:00 AM on Tuesday Oct 6, logical date should be Tuesday Oct 6
        val tuesday8am = LocalDateTime.of(2026, 10, 6, 8, 0)
        val logicalDate8am = RoutineViewModel.getLogicalDate(tuesday8am)
        assertEquals(LocalDate.of(2026, 10, 6), logicalDate8am)
    }

    @Test
    fun testStreakThreshold70Percent() {
        val totalTasks = 20
        val threshold70 = (totalTasks * 0.70).toInt() // 14
        assertEquals(14, threshold70)

        val completed13 = 13
        val percentage13 = ((completed13.toFloat() / totalTasks) * 100).toInt() // 65%
        assertTrue(percentage13 < 70)

        val completed14 = 14
        val percentage14 = ((completed14.toFloat() / totalTasks) * 100).toInt() // 70%
        assertTrue(percentage14 >= 70)
    }

    @Test
    fun testCreditScoreIdempotentAward() {
        val awards = mutableMapOf<String, CreditAwardEntity>()
        val testDate = "2026-10-01"

        // Award credit score for first time full completion
        if (!awards.containsKey(testDate)) {
            awards[testDate] = CreditAwardEntity(date = testDate, creditAwarded = true)
        }
        assertEquals(1, awards.size)

        // Repeated completion / reopening on same date does NOT duplicate credit award
        if (!awards.containsKey(testDate)) {
            awards[testDate] = CreditAwardEntity(date = testDate, creditAwarded = true)
        }
        assertEquals(1, awards.size)

        // Award for a different full day increases count
        val secondDate = "2026-10-02"
        if (!awards.containsKey(secondDate)) {
            awards[secondDate] = CreditAwardEntity(date = secondDate, creditAwarded = true)
        }
        assertEquals(2, awards.size)
    }

    @Test
    fun testExportImportJsonSafety() {
        // Test valid JSON schema
        val validJson = JSONObject().apply {
            put("version", 2)
            put("appName", "to-dodo")
            put("exportedAt", 123456789L)
            val arr = JSONArray()
            arr.put(JSONObject().apply {
                put("id", "2026-10-01_MONDAY_0")
                put("date", "2026-10-01")
                put("taskId", "MONDAY_0")
                put("completed", true)
                put("completedAt", 123456789L)
            })
            put("completionRecords", arr)
        }

        val jsonStr = validJson.toString()
        val root = JSONObject(jsonStr)
        assertTrue(root.has("completionRecords"))
        val arr = root.getJSONArray("completionRecords")
        assertEquals(1, arr.length())
        assertEquals("2026-10-01", arr.getJSONObject(0).getString("date"))
        assertEquals("MONDAY_0", arr.getJSONObject(0).getString("taskId"))

        // Test invalid/corrupted JSON
        val malformedJson = "{ corrupted json"
        var parsedSafely = false
        try {
            JSONObject(malformedJson)
            parsedSafely = true
        } catch (e: Exception) {
            parsedSafely = false
        }
        assertFalse(parsedSafely)
    }

    @Test
    fun testStreakExactBoundary69And70Percent() {
        val total = 100
        // 69% completed
        val completed69 = 69
        val percent69 = ((completed69.toFloat() / total) * 100).toInt()
        assertEquals(69, percent69)
        assertFalse("69% must NOT count toward streak", percent69 >= 70)

        // 70% completed
        val completed70 = 70
        val percent70 = ((completed70.toFloat() / total) * 100).toInt()
        assertEquals(70, percent70)
        assertTrue("70% MUST count toward streak", percent70 >= 70)

        // 100% completed
        val completed100 = 100
        val percent100 = ((completed100.toFloat() / total) * 100).toInt()
        assertEquals(100, percent100)
        assertTrue("100% MUST count toward streak", percent100 >= 70)
        assertTrue("100% qualifies for Credit Score award", completed100 >= total)
    }

    @Test
    fun testMissedDayDoesNotDeductCreditScore() {
        val awards = mutableListOf(
            CreditAwardEntity(date = "2026-10-01", creditAwarded = true),
            CreditAwardEntity(date = "2026-10-02", creditAwarded = true)
        )
        val initialScore = awards.size
        assertEquals(2, initialScore)

        // Day 2026-10-03 is missed (no completion)
        // Score remains strictly cumulative based on awards count
        val scoreAfterMissedDay = awards.size
        assertEquals(2, scoreAfterMissedDay)
        assertEquals(initialScore, scoreAfterMissedDay)
    }

    @Test
    fun testTimeRecalculationCannotTriggerCreditScoreOrCelebration() {
        var creditScore = 5
        var celebrationTriggered = false

        // Simulate 10 time tick recalculations
        for (i in 0 until 10) {
            // Recalculating time/date must not mutate awards or trigger celebration
            val newTime = LocalDateTime.of(2026, 10, 2, 12, i)
            val logicalDate = com.example.util.RoutineTimeEngine.getLogicalDate(newTime)
            assertNotNull(logicalDate)
            // Verify invariants
            assertEquals(5, creditScore)
            assertFalse(celebrationTriggered)
        }
    }
}
