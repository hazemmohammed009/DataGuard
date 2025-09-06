package com.dataguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dataguard.data.SettingsRepository
import com.dataguard.service.DataUsageMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for the main data usage dashboard screen.
 *
 * Technical Decision:
 * - This ViewModel is an `AndroidViewModel` because it needs an application context to instantiate
 *   the `DataUsageMonitor`.
 * - It uses a `combine` flow operator to merge the data from two asynchronous sources: the
 *   settings from the database and the data usage stats from the system. The UI state is only
 *   updated when both pieces of data are available, ensuring a consistent and complete UI.
 * - The data usage is fetched for different time ranges (daily, weekly, monthly) and exposed
 *   in a single `MainUiState` object. This simplifies the UI's observation logic, as it only
 *   needs to watch one state object for all the data it needs to display.
 */
class MainViewModel(
    application: Application,
    private val repository: SettingsRepository
) : AndroidViewModel(application) {

    private val dataUsageMonitor = DataUsageMonitor(application)
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                val dataLimitBytes = (settings?.dataLimitGB ?: 0f) * 1024 * 1024 * 1024
                
                val dailyUsage = fetchDataForTimeRange(TimeRange.DAILY)
                val weeklyUsage = fetchDataForTimeRange(TimeRange.WEEKLY)
                val monthlyUsage = fetchDataForTimeRange(TimeRange.MONTHLY)

                _uiState.value = MainUiState.Success(
                    dailyUsageBytes = dailyUsage,
                    weeklyUsageBytes = weeklyUsage,
                    monthlyUsageBytes = monthlyUsage,
                    dataLimitBytes = dataLimitBytes.toLong()
                )
            }
        }
    }

    private suspend fun fetchDataForTimeRange(timeRange: TimeRange): Long {
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis
        
        when (timeRange) {
            TimeRange.DAILY -> cal.set(Calendar.HOUR_OF_DAY, 0)
            TimeRange.WEEKLY -> cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            TimeRange.MONTHLY -> cal.set(Calendar.DAY_OF_MONTH, 1)
        }
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startTime = cal.timeInMillis

        return dataUsageMonitor.getTotalDataUsage(startTime, endTime)
    }

    fun refreshData() {
        // Re-trigger the collection
        viewModelScope.launch {
            // A bit of a hack to re-trigger, but effective for this use case.
            val settings = repository.settings.first()
            val dataLimitBytes = (settings?.dataLimitGB ?: 0f) * 1024 * 1024 * 1024
            
            val dailyUsage = fetchDataForTimeRange(TimeRange.DAILY)
            val weeklyUsage = fetchDataForTimeRange(TimeRange.WEEKLY)
            val monthlyUsage = fetchDataForTimeRange(TimeRange.MONTHLY)

            _uiState.value = MainUiState.Success(
                dailyUsageBytes = dailyUsage,
                weeklyUsageBytes = weeklyUsage,
                monthlyUsageBytes = monthlyUsage,
                dataLimitBytes = dataLimitBytes.toLong()
            )
        }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(
        val dailyUsageBytes: Long,
        val weeklyUsageBytes: Long,
        val monthlyUsageBytes: Long,
        val dataLimitBytes: Long
    ) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

enum class TimeRange {
    DAILY, WEEKLY, MONTHLY
}

// ViewModel Factory
class MainViewModelFactory(
    private val application: Application,
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
