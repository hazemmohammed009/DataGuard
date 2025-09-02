package com.dataguard.service

import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A class to monitor data usage using Android's NetworkStatsManager.
 *
 * Technical Decision:
 * - `NetworkStatsManager` is the modern and accurate API for querying network usage data. It
 *   provides detailed statistics for different network types (mobile, Wi-Fi).
 * - All data fetching is done within a `withContext(Dispatchers.IO)` block. This is essential
 *   because querying system services can be slow, and performing this work on a background
 *   thread is necessary to keep the UI responsive.
 * - The `hasUsageStatsPermission` check is crucial for guiding the user. Since this permission
 *   cannot be requested at runtime via a simple dialog, the app must detect if it's granted
 *   and provide instructions if it's not.
 */
class DataUsageMonitor(private val context: Context) {

    private val networkStatsManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    suspend fun getTotalDataUsage(startTime: Long, endTime: Long): Long {
        return withContext(Dispatchers.IO) {
            var totalBytes = 0L
            try {
                // Mobile Data
                val mobileBucket = networkStatsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context),
                    startTime,
                    endTime
                )
                totalBytes += (mobileBucket?.rxBytes ?: 0L) + (mobileBucket?.txBytes ?: 0L)

                // Wi-Fi Data
                val wifiBucket = networkStatsManager.querySummaryForDevice(
                    ConnectivityManager.TYPE_WIFI,
                    "",
                    startTime,
                    endTime
                )
                totalBytes += (wifiBucket?.rxBytes ?: 0L) + (wifiBucket?.txBytes ?: 0L)

            } catch (e: SecurityException) {
                // This will be thrown if the permission is not granted.
                e.printStackTrace()
            }
            totalBytes
        }
    }

    private fun getSubscriberId(context: Context): String? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            tm.subscriberId
        } else {
            // On Android Q and above, subscriber ID is not available for privacy reasons.
            // The system can still identify the mobile network without it.
            null
        }
    }

    companion object {
        fun hasUsageStatsPermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }
    }
}
