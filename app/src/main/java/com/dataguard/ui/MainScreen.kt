package com.dataguard.ui

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dataguard.service.DataUsageMonitor
import com.dataguard.viewmodel.MainUiState
import com.dataguard.viewmodel.MainViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var permissionChecked by remember { mutableStateOf(DataUsageMonitor.hasUsageStatsPermission(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Guard Dashboard") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
            if (!permissionChecked) {
                PermissionRequestCard {
                    // Intent to open the usage access settings screen
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
            } else {
                when (val state = uiState) {
                    is MainUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is MainUiState.Success -> {
                        DashboardContent(state)
                    }
                    is MainUiState.Error -> {
                        Text("Error: ${state.message}")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(state: MainUiState.Success) {
    val monthlyPercentage = if (state.dataLimitBytes > 0) {
        (state.monthlyUsageBytes.toFloat() / state.dataLimitBytes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Chart setup
    val chartModelProducer = remember { ChartEntryModelProducer() }
    LaunchedEffect(state) {
        chartModelProducer.setEntries(
            listOf(
                entryOf(1f, state.dailyUsageBytes / (1024f * 1024f)), // in MB
                entryOf(2f, state.weeklyUsageBytes / (1024f * 1024f)),
                entryOf(3f, state.monthlyUsageBytes / (1024f * 1024f))
            )
        )
    }

    Column {
        Text("Monthly Usage", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { monthlyPercentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatBytes(state.monthlyUsageBytes), fontSize = 14.sp)
            Text(formatBytes(state.dataLimitBytes), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UsageCard(title = "Today", usage = formatBytes(state.dailyUsageBytes), modifier = Modifier.weight(1f))
            UsageCard(title = "This Week", usage = formatBytes(state.weeklyUsageBytes), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Usage Trend (MB)", style = MaterialTheme.typography.titleMedium)
                Chart(
                    chart = lineChart(),
                    chartModelProducer = chartModelProducer,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            when (value) {
                                1f -> "Day"
                                2f -> "Week"
                                3f -> "Month"
                                else -> ""
                            }
                        }
                    ),
                )
            }
        }
    }
}

@Composable
fun UsageCard(title: String, usage: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(usage, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun PermissionRequestCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Permission Required", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Data Guard needs 'Usage Access' permission to monitor your data usage. Please grant this permission in the system settings.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClick, modifier = Modifier.align(Alignment.End)) {
                Text("Open Settings")
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes > 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        bytes > 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
        bytes > 1024 -> "%.2f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
