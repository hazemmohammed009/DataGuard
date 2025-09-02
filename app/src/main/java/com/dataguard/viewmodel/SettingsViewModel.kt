package com.dataguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dataguard.data.Settings
import com.dataguard.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest

/**
 * ViewModel for the Settings screen.
 *
 * Technical Decision:
 * - This ViewModel exposes the `Settings` object from the repository as a `StateFlow`. This
 *   allows the SettingsScreen UI to be completely stateless and reactive. It simply observes
 *   this flow and re-renders whenever the settings data changes.
 * - The `stateIn` operator is used to convert the cold `Flow` from the repository into a hot
 *   `StateFlow`, which shares the latest value with all collectors and keeps the data alive
 *   as long as the UI is visible (`SharingStarted.WhileSubscribed(5000)`).
 * - The password verification and saving logic are encapsulated here, keeping the UI clean
 *   and focused only on presenting the data and capturing user input.
 */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<Settings?> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun verifySettingsPassword(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val result = currentSettings?.settingsPasswordHash == hashPassword(password)
            onResult(result)
        }
    }

    fun saveSettings(
        alertEmail: String,
        senderEmail: String,
        senderPassword: String,
        dataLimit: Float
    ) {
        viewModelScope.launch {
            val currentSettings = settings.value
            if (currentSettings != null) {
                val updatedSettings = currentSettings.copy(
                    alertEmailAddress = alertEmail,
                    senderEmailAddress = senderEmail,
                    senderEmailPassword = senderPassword,
                    dataLimitGB = dataLimit
                )
                repository.saveSettings(updatedSettings)
            }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

// ViewModel Factory
class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
