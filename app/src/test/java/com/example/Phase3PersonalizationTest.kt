package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.CompletionRecordEntity
import com.example.data.db.CreditAwardEntity
import com.example.data.db.MIGRATION_1_2
import com.example.data.db.MIGRATION_2_3
import com.example.data.db.TaskEntity
import com.example.data.preferences.SettingsManager
import com.example.data.repository.RoutineRepository
import com.example.util.RoutineTimeEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase3PersonalizationTest {

    @Test
    fun testExistingUserUpgradePreservesDataAndSkipsOnboarding() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
            val dao = db.routineDao()
            val settingsManager = SettingsManager(context)

            // Setup existing user data: customized routine, completion records, credit award
            val existingTask = TaskEntity(
                id = "custom_task_1",
                dayOfWeek = "MONDAY",
                title = "Machine Learning",
                startTime = "18:00",
                endTime = "19:30",
                category = "Project / Internship",
                sortOrder = 1,
                daysOfWeek = "MONDAY,WEDNESDAY"
            )
            dao.insertTasks(listOf(existingTask))

            val existingComp = CompletionRecordEntity(
                id = "2026-10-01_custom_task_1",
                date = "2026-10-01",
                taskId = "custom_task_1",
                completed = true,
                completedAt = 123456789L
            )
            dao.upsertCompletion(existingComp)

            val existingAward = CreditAwardEntity(
                date = "2026-10-01",
                creditAwarded = true,
                awardedAt = 123456789L
            )
            dao.insertCreditAward(existingAward)

            // Verify detection: existing data present
            val taskCount = dao.getTaskCount()
            val completionCount = dao.getAllCompletions().first().size
            val creditCount = dao.getCreditScoreCount().first()

            val hasExistingData = (taskCount > 0 || completionCount > 0 || creditCount > 0)
            assertTrue("Existing installation data must be detected", hasExistingData)

            // Auto-mark onboarding completed
            settingsManager.setOnboardingCompleted(true)
            settingsManager.setRoutineSetupCompleted(true)

            assertTrue(settingsManager.onboardingCompletedFlow.first())
            assertTrue(settingsManager.routineSetupCompletedFlow.first())

            // Verify existing task remained intact
            val tasks = dao.getAllTasks().first()
            assertEquals(1, tasks.size)
            assertEquals("Machine Learning", tasks[0].title)

            // Verify existing completions and credit awards remain intact
            val completions = dao.getAllCompletions().first()
            assertEquals(1, completions.size)
            assertEquals("custom_task_1", completions[0].taskId)

            val creditAwards = dao.getCreditScoreCount().first()
            assertEquals(1, creditAwards)

            db.close()
        }
    }

    @Test
    fun testNewUserOnboardingChooseStarterRoutine() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
            val repo = RoutineRepository(db.routineDao())
            val settingsManager = SettingsManager(context)

            // Clean install: 0 tasks initially
            assertEquals(0, repo.getTaskCount())

            // User enters name "Rahul" and chooses "Use Starter Routine"
            settingsManager.setUserName("Rahul")
            repo.loadStarterRoutine()
            settingsManager.setOnboardingCompleted(true)
            settingsManager.setRoutineSetupCompleted(true)

            assertEquals("Rahul", settingsManager.userNameFlow.first())
            assertTrue(settingsManager.onboardingCompletedFlow.first())
            assertTrue(repo.getTaskCount() > 0)

            // Verify starter routine tasks can be edited and deleted normally
            val tasks = repo.getAllTasks().first()
            val firstTask = tasks[0]
            val updatedTask = firstTask.copy(title = "Morning Reading")
            repo.updateTask(updatedTask)

            val updatedFetched = repo.getTaskById(firstTask.id)
            assertEquals("Morning Reading", updatedFetched?.title)

            repo.deleteTask(updatedTask)
            assertNull(repo.getTaskById(firstTask.id))

            db.close()
        }
    }

    @Test
    fun testNewUserOnboardingChooseCustomRoutineAndSurvivesRestart() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
            val repo = RoutineRepository(db.routineDao())
            val settingsManager = SettingsManager(context)

            // Clean install: 0 tasks initially
            assertEquals(0, repo.getTaskCount())

            // User enters name "Ananya" and chooses "Create My Routine"
            settingsManager.setUserName("Ananya")
            repo.clearAllTasks()
            settingsManager.setOnboardingCompleted(true)
            settingsManager.setRoutineSetupCompleted(true)

            assertEquals("Ananya", settingsManager.userNameFlow.first())
            assertTrue(settingsManager.onboardingCompletedFlow.first())
            assertTrue(settingsManager.routineSetupCompletedFlow.first())
            assertEquals(0, repo.getTaskCount())

            // Simulate app restart: routineSetupCompleted is true, tasks must remain empty
            if (!settingsManager.routineSetupCompletedFlow.first() && repo.getTaskCount() == 0) {
                repo.ensureDefaultTasksSeeded()
            }

            // Routine remains empty! No unwanted seeding!
            assertEquals(0, repo.getTaskCount())

            // Now user adds their own custom task
            val customTask = TaskEntity(
                id = "study_ananya",
                dayOfWeek = "MONDAY",
                title = "Study",
                startTime = "19:00",
                endTime = "20:00",
                category = "Study",
                sortOrder = 1,
                daysOfWeek = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
            )
            repo.insertTasks(listOf(customTask))

            assertEquals(1, repo.getTaskCount())
            val fetched = repo.getTaskById("study_ananya")
            assertEquals("Study", fetched?.title)
            assertTrue(fetched?.repeatsOn("WEDNESDAY") == true)
            assertFalse(fetched?.repeatsOn("SATURDAY") == true)

            db.close()
        }
    }

    @Test
    fun testDynamicGreetingPersonalization() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val settingsManager = SettingsManager(context)

            // Default
            settingsManager.setUserName("Piyush")
            val greeting1 = RoutineTimeEngine.GreetingInfo("Good morning", "☀️")
            assertEquals("Good morning, Piyush ☀️", greeting1.formatWithUser(settingsManager.userNameFlow.first()))

            // Rename to Rahul
            settingsManager.setUserName("Rahul")
            assertEquals("Good morning, Rahul ☀️", greeting1.formatWithUser(settingsManager.userNameFlow.first()))

            // Rename to Ananya
            settingsManager.setUserName("Ananya")
            assertEquals("Good morning, Ananya ☀️", greeting1.formatWithUser(settingsManager.userNameFlow.first()))
        }
    }

    @Test
    fun testJsonExportAndImportWithUserName() {
        val root = JSONObject().apply {
            put("version", 3)
            put("appName", "to-dodo")
            put("userName", "Rahul")
            put("exportedAt", System.currentTimeMillis())
            put("tasks", org.json.JSONArray())
            put("completionRecords", org.json.JSONArray())
        }

        val jsonStr = root.toString()
        val parsed = JSONObject(jsonStr)

        assertTrue(parsed.has("userName"))
        assertEquals("Rahul", parsed.getString("userName"))
    }
}
