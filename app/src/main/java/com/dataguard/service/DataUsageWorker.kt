package com.dataguard.service

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dataguard.DataGuardApp
import com.dataguard.data.Settings
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

/**
 * A background worker that periodically checks data usage and sends an alert if the limit is exceeded.
 *
 * Technical Decision:
 * - WorkManager is the recommended solution for persistent background tasks. It's battery-efficient
 *   and guarantees execution, even if the app is closed or the device restarts. This makes it
 *   perfect for a periodic monitoring task like this.
 * - Using a CoroutineWorker allows us to use suspend functions for database and network operations,
 *   making the background code clean and non-blocking.
 * - The logic to check if an alert has already been sent for the current month prevents spamming
 *   the user with multiple emails.
 */
class DataUsageWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as DataGuardApp).repository
        val settings = repository.settings.firstOrNull()

        if (settings == null || !areSettingsValid(settings)) {
            // If settings are not configured, we can't proceed.
            return Result.failure()
        }

        // Check if an alert has already been sent this month
        val prefs = applicationContext.getSharedPreferences("DataGuardPrefs", Context.MODE_PRIVATE)
        val lastAlertMonth = prefs.getInt("lastAlertMonth", -1)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        if (lastAlertMonth == currentMonth) {
            return Result.success() // Alert already sent for this month
        }

        val dataUsageMonitor = DataUsageMonitor(applicationContext)
        val (startTime, endTime) = getMonthTimeRange()
        val totalUsageBytes = dataUsageMonitor.getTotalDataUsage(startTime, endTime)
        val limitBytes = (settings.dataLimitGB ?: 0f) * 1024 * 1024 * 1024

        if (totalUsageBytes > limitBytes) {
            val emailSender = EmailSender()
            emailSender.sendEmail(
                senderEmail = settings.senderEmailAddress!!,
                senderPassword = settings.senderEmailPassword!!,
                recipientEmail = settings.alertEmailAddress!!,
                deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"
            )
            // Save the current month to prevent re-sending alerts
            prefs.edit().putInt("lastAlertMonth", currentMonth).apply()
        }

        return Result.success()
    }

    private fun areSettingsValid(settings: Settings): Boolean {
        return !settings.alertEmailAddress.isNullOrBlank() &&
                !settings.senderEmailAddress.isNullOrBlank() &&
                !settings.senderEmailPassword.isNullOrBlank() &&
                settings.dataLimitGB != null && settings.dataLimitGB > 0
    }

    private fun getMonthTimeRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()
        return Pair(startTime, endTime)
    }
}
