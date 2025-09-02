package com.dataguard.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository module for handling data operations on Settings.
 *
 * Technical Decision:
 * The Repository Pattern is a key architectural choice. It abstracts the data source (in this
 * case, the Room database) from the rest of the application, specifically the ViewModels.
 * This separation of concerns makes the code cleaner, easier to test, and more maintainable.
 * If we were to add a remote data source in the future (e.g., fetching settings from a server),
 * we would only need to modify the repository, and the ViewModels would remain unchanged.
 * It provides a clean API for data access to the rest of the app.
 */
class SettingsRepository(private val settingsDao: SettingsDao) {

    val settings: Flow<Settings?> = settingsDao.getSettings()

    suspend fun saveSettings(settings: Settings) {
        settingsDao.insertOrUpdate(settings)
    }
}
