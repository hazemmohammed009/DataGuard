package com.dataguard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the Settings entity.
 * This interface defines the database operations for Room.
 *
 * Technical Decision:
 * - Using a Flow<Settings?> allows the UI to observe database changes reactively. When the
 *   settings are updated, any collector of this flow will automatically receive the new data,
 *   ensuring the UI is always in sync with the database.
 * - The `onConflict = OnConflictStrategy.REPLACE` strategy in the `insertOrUpdate` function
 *   simplifies the logic for saving settings. If the settings row already exists, it gets
- *   updated; otherwise, it gets created. This is perfect for our single-row settings table.
 * - `suspend` functions are used for database operations to ensure they are run off the main
 *   thread, preventing UI freezes and maintaining a responsive application.
 */
@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: Settings)
}
