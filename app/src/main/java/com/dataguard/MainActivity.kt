package com.dataguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.*
import com.dataguard.service.DataUsageWorker
import com.dataguard.ui.AppNavigation
import com.dataguard.ui.theme.DataGuardTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Schedule the background worker
        scheduleDataUsageWorker()

        setContent {
            DataGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(application = application)
                }
            }
        }
    }

    /**
     * Schedules the periodic background worker for monitoring data usage.
     *
     * Technical Decision:
     * - A PeriodicWorkRequest is used to schedule the DataUsageWorker to run repeatedly.
     *   The interval is set to 6 hours, which is a reasonable balance between timely alerts
     *   and battery efficiency.
     * - `ExistingPeriodicWorkPolicy.KEEP` ensures that if a worker is already scheduled, a new
     *   one will not be created. This prevents duplicate workers from running.
     * - Network constraints (`Constraints.Builder().setRequiredNetworkType`) ensure the worker
     *   only runs when the device is connected to the internet, preventing unnecessary work
     *   and potential errors when trying to send an email.
     */
    private fun scheduleDataUsageWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DataUsageWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DataUsageMonitorWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}
