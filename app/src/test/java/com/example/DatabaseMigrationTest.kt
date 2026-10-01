package com.example

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.CreditAwardEntity
import com.example.data.db.MIGRATION_1_2
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    @Test
    fun testDatabaseCreationAndCreditAwardDao() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .addMigrations(MIGRATION_1_2)
                .build()

            val dao = db.routineDao()
            assertNotNull(dao)

            val award = CreditAwardEntity(date = "2026-10-01", creditAwarded = true, awardedAt = 123456L)
            val rowId = dao.insertCreditAward(award)
            assertEquals(1L, rowId)

            val fetched = dao.getCreditAwardForDate("2026-10-01")
            assertNotNull(fetched)
            assertEquals("2026-10-01", fetched?.date)
            assertEquals(true, fetched?.creditAwarded)

            // Test idempotence: duplicate insert on same date is ignored (rowId -1)
            val duplicateAward = CreditAwardEntity(date = "2026-10-01", creditAwarded = true, awardedAt = 999999L)
            val dupRowId = dao.insertCreditAward(duplicateAward)
            assertEquals(-1L, dupRowId)

            val count = dao.getCreditScoreCount().first()
            assertEquals(1, count)

            db.close()
        }
    }

    @Test
    fun testActualV1ToV2MigrationWithPreservedData() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val dbFile = context.getDatabasePath("test_migration.db")
            dbFile.delete()

            // 1. Create a pure v1 database manually via FrameworkSQLiteOpenHelper
            val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.name)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` TEXT NOT NULL, `dayOfWeek` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                        db.execSQL("CREATE TABLE IF NOT EXISTS `completion_records` (`id` TEXT NOT NULL, `date` TEXT NOT NULL, `taskId` TEXT NOT NULL, `completed` INTEGER NOT NULL, `completedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    }
                    override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()

            val helper = FrameworkSQLiteOpenHelperFactory().create(config)
            val v1Db = helper.writableDatabase

            // Insert sample v1 tasks and completion records
            v1Db.execSQL("INSERT INTO tasks VALUES ('task_1', 'MONDAY', '09:00', '10:00', 'Deep Study', 'Study', 0)")
            v1Db.execSQL("INSERT INTO completion_records VALUES ('2026-10-01_task_1', '2026-10-01', 'task_1', 1, 123456789)")
            v1Db.close()

            // 2. Open with Room v2 using MIGRATION_1_2
            val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.name)
                .addMigrations(MIGRATION_1_2)
                .build()

            val dao = roomDb.routineDao()

            // Verify existing v1 tasks are preserved
            val tasks = dao.getAllTasks().first()
            assertEquals(1, tasks.size)
            assertEquals("task_1", tasks[0].id)
            assertEquals("Deep Study", tasks[0].title)

            // Verify existing v1 completions are preserved
            val completions = dao.getAllCompletions().first()
            assertEquals(1, completions.size)
            assertEquals("2026-10-01", completions[0].date)
            assertEquals("task_1", completions[0].taskId)
            assertTrue(completions[0].completed)

            // Verify credit_awards table is created and operational
            val award = CreditAwardEntity(date = "2026-10-01", creditAwarded = true, awardedAt = 987654321L)
            val rowId = dao.insertCreditAward(award)
            assertEquals(1L, rowId)

            val fetchedAward = dao.getCreditAwardForDate("2026-10-01")
            assertNotNull(fetchedAward)
            assertEquals("2026-10-01", fetchedAward?.date)

            roomDb.close()
            dbFile.delete()
        }
    }
}
