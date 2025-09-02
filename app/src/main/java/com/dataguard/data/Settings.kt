package com.dataguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the settings table in the Room database.
 * This data class holds all the user-configurable settings.
 *
 * Technical Decision:
 * Using a single table with a fixed PrimaryKey (id = 1) is an efficient way to store a single
 * row of global application settings. This simplifies data retrieval, as we always know the
 * exact ID of the row we need to query or update. It's a clean and simple approach for
 * managing a singleton-like configuration entity.
 */
@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val appPasswordHash: String?,
    val settingsPasswordHash: String?,
    val alertEmailAddress: String?,
    val senderEmailAddress: String?,
    val senderEmailPassword: String?, // This should be a Google App Password
    val dataLimitGB: Float?
)
