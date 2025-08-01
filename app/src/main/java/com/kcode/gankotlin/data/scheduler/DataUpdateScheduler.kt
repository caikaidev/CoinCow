package com.kcode.gankotlin.data.scheduler

import android.content.Context
import androidx.work.*
import com.kcode.gankotlin.data.worker.MarketDataUpdateWorker
import com.kcode.gankotlin.data.worker.WatchlistUpdateWorker
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for automatic data updates using WorkManager
 */
@Singleton
class DataUpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    companion object {
        private const val MARKET_DATA_WORK_NAME = "market_data_update_work"
        private const val WATCHLIST_DATA_WORK_NAME = "watchlist_data_update_work"
        private const val DEFAULT_REFRESH_INTERVAL_MINUTES = 1L
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule periodic market data updates
     */
    suspend fun scheduleMarketDataUpdates() {
        val refreshInterval = userPreferencesRepository.getRefreshInterval().first()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<MarketDataUpdateWorker>(
            refreshInterval.coerceAtLeast(15), // Minimum 15 minutes for periodic work
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            MARKET_DATA_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Schedule frequent watchlist updates (every 1 minute when app is active)
     */
    suspend fun scheduleWatchlistUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<WatchlistUpdateWorker>(
            15, // Minimum 15 minutes for periodic work
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WATCHLIST_DATA_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Schedule immediate one-time update
     */
    fun scheduleImmediateUpdate(watchlistOnly: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val inputData = Data.Builder()
            .putBoolean("watchlist_only", watchlistOnly)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<MarketDataUpdateWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        
        workManager.enqueue(workRequest)
    }
    
    /**
     * Cancel all scheduled updates
     */
    fun cancelAllUpdates() {
        workManager.cancelUniqueWork(MARKET_DATA_WORK_NAME)
        workManager.cancelUniqueWork(WATCHLIST_DATA_WORK_NAME)
    }
    
    /**
     * Cancel market data updates only
     */
    fun cancelMarketDataUpdates() {
        workManager.cancelUniqueWork(MARKET_DATA_WORK_NAME)
    }
    
    /**
     * Cancel watchlist updates only
     */
    fun cancelWatchlistUpdates() {
        workManager.cancelUniqueWork(WATCHLIST_DATA_WORK_NAME)
    }
    
    /**
     * Update refresh interval and reschedule work
     */
    suspend fun updateRefreshInterval(intervalMinutes: Long) {
        userPreferencesRepository.setRefreshInterval(intervalMinutes)
        scheduleMarketDataUpdates() // Reschedule with new interval
    }
    
    /**
     * Get current work status
     */
    fun getWorkStatus(workName: String) = workManager.getWorkInfosForUniqueWorkLiveData(workName)
}