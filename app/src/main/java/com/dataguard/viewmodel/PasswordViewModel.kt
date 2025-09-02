package com.dataguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dataguard.data.Settings
import com.dataguard.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest

/**
 * ViewModel for handling password logic (setup and verification).
 *
 * Technical Decision:
 * - Using a ViewModel is crucial for separating UI logic from the UI controllers (Compose screens).
 *   It holds the UI state and survives configuration changes (like screen rotation), preventing
 *   data loss.
 * - State is exposed via `StateFlow`, a modern, coroutine-based state holder that allows the UI
 *   to reactively observe and update based on state changes.
 * - Passwords are not stored in plain text. A SHA-256 hash is computed and stored. This is a
 *   fundamental security practice. While more advanced key-stretching algorithms exist, SHA-256
 *   provides a solid baseline of security for this application's threat model.
 */
class PasswordViewModel(private val repository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PasswordUiState>(PasswordUiState.Loading)
    val uiState: StateFlow<PasswordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = repository.settings.first()
            if (settings == null || settings.appPasswordHash == null) {
                _uiState.value = PasswordUiState.NeedsSetup
            } else {
                _uiState.value = PasswordUiState.NeedsUnlock
            }
        }
    }

    fun verifyAppPassword(password: String) {
        viewModelScope.launch {
            val settings = repository.settings.first()
            if (settings?.appPasswordHash == hashPassword(password)) {
                _uiState.value = PasswordUiState.Unlocked
            } else {
                // You might want to add a more specific error state here
                _uiState.value = PasswordUiState.Error("Invalid password")
            }
        }
    }

    fun saveInitialPasswords(appPass: String, settingsPass: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val initialSettings = Settings(
                id = 1,
                appPasswordHash = hashPassword(appPass),
                settingsPasswordHash = hashPassword(settingsPass),
                alertEmailAddress = null,
                senderEmailAddress = null,
                senderEmailPassword = null,
                dataLimitGB = null
            )
            repository.saveSettings(initialSettings)
            onComplete()
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}

sealed class PasswordUiState {
    object Loading : PasswordUiState()
    object NeedsSetup : PasswordUiState()
    object NeedsUnlock : PasswordUiState()
    object Unlocked : PasswordUiState()
    data class Error(val message: String) : PasswordUiState()
}

// ViewModel Factory to provide the repository dependency
class PasswordViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
