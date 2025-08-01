package com.kcode.gankotlin.data.sync

import com.kcode.gankotlin.data.remote.NetworkConnectivityManager
import com.kcode.gankotlin.data.scheduler.DataUpdateScheduler
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for coordinating data synchronization across the app
 */
@Singleton
class DataSyncManager @Inject constructor(
    private val dataUpdateScheduler: DataUpdateScheduler,
    private val networkConnectivityManager: NetworkConnectivityManager,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()
    
    private var syncJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Initialize the sync manager
     */
    fun initialize() {
        // Monitor network connectivity and adjust sync behavior
        coroutineScope.launch {
            networkConnectivityManager.networkConnectivityFlow()
                .collect { isConnected ->
                    if (isConnected) {
                        onNetworkAvailable()
                    } else {
                        onNetworkUnavailable()
                    }
                }
        }
        
        // Monitor user preferences for refresh interval changes
        coroutineScope.launch {
            userPreferencesRepository.getRefreshInterval()
                .distinctUntilChanged()
                .collect { intervalMinutes ->
                    updateSyncInterval(intervalMinutes)
                }
        }
    }
    
    /**
     * Start automatic data synchronization
     */
    suspend fun startAutoSync() {
        _syncStatus.value = SyncStatus.STARTING
        
        try {
            // Schedule periodic updates
            dataUpdateScheduler.scheduleMarketDataUpdates()
            dataUpdateScheduler.scheduleWatchlistUpdates()
            
            _syncStatus.value = SyncStatus.ACTIVE
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
        }
    }
    
    /**
     * Stop automatic data synchronization
     */
    fun stopAutoSync() {
        _syncStatus.value = SyncStatus.STOPPING
        
        dataUpdateScheduler.cancelAllUpdates()
        syncJob?.cancel()
        
        _syncStatus.value = SyncStatus.IDLE
    }
    
    /**
     * Trigger immediate sync
     */
    fun triggerImmediateSync(watchlistOnly: Boolean = false) {
        if (_syncStatus.value == SyncStatus.SYNCING) {
            return // Already syncing
        }
        
        _syncStatus.value = SyncStatus.SYNCING
        
        dataUpdateScheduler.scheduleImmediateUpdate(watchlistOnly)
        _lastSyncTime.value = System.currentTimeMillis()
        
        // Reset status after a delay (since we can't directly monitor WorkManager completion here)
        coroutineScope.launch {
            delay(5000) // 5 seconds
            if (_syncStatus.value == SyncStatus.SYNCING) {
                _syncStatus.value = SyncStatus.ACTIVE
            }
        }
    }
    
    /**
     * Handle network becoming available
     */
    private suspend fun onNetworkAvailable() {
        if (_syncStatus.value == SyncStatus.IDLE || _syncStatus.value == SyncStatus.ERROR) {
            startAutoSync()
        }
        
        // Trigger immediate sync when network becomes available
        triggerImmediateSync()
    }
    
    /**
     * Handle network becoming unavailable
     */
    private fun onNetworkUnavailable() {
        if (_syncStatus.value == SyncStatus.ACTIVE) {
            _syncStatus.value = SyncStatus.PAUSED
        }
    }
    
    /**
     * Update sync interval
     */
    private suspend fun updateSyncInterval(intervalMinutes: Long) {
        if (_syncStatus.value == SyncStatus.ACTIVE) {
            // Reschedule with new interval
            dataUpdateScheduler.updateRefreshInterval(intervalMinutes)
        }
    }
    
    /**
     * Get sync statistics
     */
    fun getSyncStats(): SyncStats {
        return SyncStats(
            status = _syncStatus.value,
            lastSyncTime = _lastSyncTime.value,
            isNetworkAvailable = networkConnectivityManager.isConnected(),
            networkType = networkConnectivityManager.getNetworkType()
        )
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        stopAutoSync()
        coroutineScope.cancel()
    }
}

/**
 * Enum representing sync status
 */
enum class SyncStatus {
    IDLE,
    STARTING,
    ACTIVE,
    SYNCING,
    PAUSED,
    STOPPING,
    ERROR
}

/**
 * Data class for sync statistics
 */
data class SyncStats(
    val status: SyncStatus,
    val lastSyncTime: Long,
    val isNetworkAvailable: Boolean,
    val networkType: com.kcode.gankotlin.data.remote.NetworkType
)