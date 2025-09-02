package com.dataguard

import android.app.Application
import com.dataguard.data.AppDatabase
import com.dataguard.data.SettingsRepository

/**
 * Technical Decision:
 * An Application class is the best place to initialize singletons that need a context
 * and should persist throughout the app's lifecycle. Here, we create a single instance
 * of the AppDatabase and the SettingsRepository. This is a simple form of dependency
 * injection, ensuring that all parts of the app (ViewModels, services) use the same
 * database and repository instances, preventing data inconsistencies and memory leaks.
 */
class DataGuardApp : Application() {

    // Using 'lazy' ensures the database and repository are only created when first needed.
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: SettingsRepository by lazy { SettingsRepository(database.settingsDao()) }
}
