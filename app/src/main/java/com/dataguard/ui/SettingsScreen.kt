package com.dataguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dataguard.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val settings by viewModel.settings.collectAsState()
    var isUnlocked by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!isUnlocked) {
                SettingsPasswordPrompt(
                    password = password,
                    onPasswordChange = { password = it },
                    onUnlock = {
                        viewModel.verifySettingsPassword(password) { success ->
                            if (success) {
                                isUnlocked = true
                            }
                        }
                    }
                )
            } else {
                SettingsContent(
                    settings = settings,
                    onSave = { alertEmail, senderEmail, senderPass, dataLimit ->
                        viewModel.saveSettings(alertEmail, senderEmail, senderPass, dataLimit)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsPasswordPrompt(
    password: String,
    onPasswordChange: (String) -> Unit,
    onUnlock: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Enter Settings Password to make changes.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Settings Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onUnlock) {
            Text("Unlock Settings")
        }
    }
}

@Composable
fun SettingsContent(
    settings: com.dataguard.data.Settings?,
    onSave: (String, String, String, Float) -> Unit
) {
    var alertEmail by remember { mutableStateOf(settings?.alertEmailAddress ?: "") }
    var senderEmail by remember { mutableStateOf(settings?.senderEmailAddress ?: "") }
    var senderPassword by remember { mutableStateOf(settings?.senderEmailPassword ?: "") }
    var dataLimit by remember { mutableStateOf(settings?.dataLimitGB?.toString() ?: "") }

    Column {
        Text("Alert Configuration", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = alertEmail,
            onValueChange = { alertEmail = it },
            label = { Text("Recipient Email Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = senderEmail,
            onValueChange = { senderEmail = it },
            label = { Text("Sender Gmail Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = senderPassword,
            onValueChange = { senderPassword = it },
            label = { Text("Sender Google App Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dataLimit,
            onValueChange = { dataLimit = it },
            label = { Text("Data Limit (GB)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSave(alertEmail, senderEmail, senderPassword, dataLimit.toFloatOrNull() ?: 0f) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }
    }
}
