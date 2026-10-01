package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `credit_awards` (`date` TEXT NOT NULL, `creditAwarded` INTEGER NOT NULL, `awardedAt` INTEGER NOT NULL, PRIMARY KEY(`date`))"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `daysOfWeek` TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE `tasks` SET `daysOfWeek` = `dayOfWeek` WHERE `daysOfWeek` = ''")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `isEnabled` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `effectiveFromDate` TEXT NOT NULL DEFAULT '2000-01-01'")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `effectiveUntilDate` TEXT DEFAULT NULL")
    }
}

@Database(entities = [TaskEntity::class, CompletionRecordEntity::class, CreditAwardEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routine_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
