package com.dataguard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main Room database class for the application.
 *
 * Technical Decision:
 * - The singleton pattern (`INSTANCE` and `getDatabase`) is crucial for database management.
 *   It ensures that only one instance of the database is ever created per app process. This
 *   prevents race conditions, memory leaks, and performance issues associated with having
 *   multiple database connections open.
 * - `volatile = true` ensures that the `INSTANCE` variable is always up-to-date across all
 *   execution threads, which is important for thread safety in a multi-threaded environment.
 * - `fallbackToDestructiveMigration()` is used here for simplicity. In a real-world production
 *   app, you would implement a proper migration strategy to preserve user data between app
 *   updates. For this project, if the schema changes, the database will be cleared.
 */
@Database(entities = [Settings::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "data_guard_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
