package com.dataguard.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dataguard.DataGuardApp
import com.dataguard.viewmodel.*

/**
 * Defines the navigation graph for the application.
 *
 * Technical Decision:
 * - Jetpack Navigation Compose is the standard and recommended library for handling navigation
 *   in a Compose-based application. It provides a declarative way to define navigation paths
 *   and is tightly integrated with the Compose lifecycle.
 * - A central `AppNavigation` composable encapsulates all navigation logic, making the app's
 *   flow easy to understand and manage.
 * - ViewModels are scoped to the navigation graph. The `PasswordViewModel` determines the
 *   initial route (`startDestination`). If the app is unlocked, the user is taken directly
 *   to the main screen; otherwise, they are shown the password screen. This logic is robust
 *   and handles the app's security model correctly.
 */
@Composable
fun AppNavigation(application: Application) {
    val navController = rememberNavController()
    val app = application as DataGuardApp

    // Create ViewModel instances using their factories
    val passwordViewModel: PasswordViewModel = viewModel(
        factory = PasswordViewModelFactory(app.repository)
    )
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(application, app.repository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(app.repository)
    )

    val passwordUiState by passwordViewModel.uiState.collectAsState()
    val startDestination = when (passwordUiState) {
        is PasswordUiState.Unlocked -> "main"
        else -> "password"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("password") {
            PasswordScreen(
                viewModel = passwordViewModel,
                onUnlock = {
                    navController.navigate("main") {
                        // Clear the back stack so the user can't go back to the password screen
                        popUpTo("password") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen(navController = navController, viewModel = mainViewModel)
        }
        composable("settings") {
            SettingsScreen(navController = navController, viewModel = settingsViewModel)
        }
    }
}
