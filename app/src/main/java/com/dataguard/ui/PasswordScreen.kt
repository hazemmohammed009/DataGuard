package com.dataguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.dataguard.viewmodel.PasswordUiState
import com.dataguard.viewmodel.PasswordViewModel

@Composable
fun PasswordScreen(
    viewModel: PasswordViewModel,
    onUnlock: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is PasswordUiState.Loading -> {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PasswordUiState.NeedsSetup -> {
                SetupPasswordContent(viewModel = viewModel, onSetupComplete = onUnlock)
            }
            is PasswordUiState.NeedsUnlock -> {
                UnlockContent(viewModel = viewModel)
            }
            is PasswordUiState.Unlocked -> {
                // This state change will be caught by the navigation graph,
                // which will then navigate to the main screen.
                // We can also call onUnlock directly if needed.
                LaunchedEffect(Unit) {
                    onUnlock()
                }
            }
            is PasswordUiState.Error -> {
                // You can add more sophisticated error handling here
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun SetupPasswordContent(viewModel: PasswordViewModel, onSetupComplete: () -> Unit) {
    var appPassword by remember { mutableStateOf("") }
    var settingsPassword by remember { mutableStateOf("") }
    var confirmAppPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Data Guard", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please set up your passwords to secure the app.")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = appPassword,
            onValueChange = { appPassword = it },
            label = { Text("Enter App Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmAppPassword,
            onValueChange = { confirmAppPassword = it },
            label = { Text("Confirm App Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = settingsPassword,
            onValueChange = { settingsPassword = it },
            label = { Text("Enter Settings Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (appPassword.isNotBlank() && appPassword == confirmAppPassword && settingsPassword.isNotBlank()) {
                    viewModel.saveInitialPasswords(appPassword, settingsPassword, onSetupComplete)
                }
            },
            enabled = appPassword.isNotBlank() && appPassword == confirmAppPassword && settingsPassword.isNotBlank()
        ) {
            Text("Save and Continue")
        }
    }
}

@Composable
fun UnlockContent(viewModel: PasswordViewModel) {
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Password", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.verifyAppPassword(password) }) {
            Text("Unlock")
        }
    }
}
