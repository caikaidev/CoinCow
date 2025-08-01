package com.kcode.gankotlin.data.recovery

import android.content.Context
import android.content.SharedPreferences
import com.kcode.gankotlin.data.local.CryptoDatabase
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for application crash recovery and state restoration
 */
@Singleton
class CrashRecoveryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CryptoDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "crash_recovery", Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_LAST_SUCCESSFUL_LAUNCH = "last_successful_launch"
        private const val KEY_CRASH_COUNT = "crash_count"
        private const val KEY_LAST_CRASH_TIME = "last_crash_time"
        private const val KEY_RECOVERY_MODE = "recovery_mode"
        private const val KEY_LAST_SCREEN = "last_screen"
        private const val KEY_WATCHLIST_BACKUP = "watchlist_backup"
        
        private const val MAX_CRASH_COUNT = 3
        private const val CRASH_TIMEOUT_MS = 30_000L // 30 seconds
    }
    
    /**
     * Record successful app launch
     */
    fun recordSuccessfulLaunch() {
        prefs.edit()
            .putLong(KEY_LAST_SUCCESSFUL_LAUNCH, System.currentTimeMillis())
            .putInt(KEY_CRASH_COUNT, 0)
            .putBoolean(KEY_RECOVERY_MODE, false)
            .apply()
    }
    
    /**
     * Record app crash
     */
    fun recordCrash(exception: Throwable) {
        val crashCount = prefs.getInt(KEY_CRASH_COUNT, 0) + 1
        
        prefs.edit()
            .putLong(KEY_LAST_CRASH_TIME, System.currentTimeMillis())
            .putInt(KEY_CRASH_COUNT, crashCount)
            .putString("last_crash_message", exception.message ?: "Unknown error")
            .putString("last_crash_type", exception.javaClass.simpleName)
            .apply()
        
        // Enable recovery mode if too many crashes
        if (crashCount >= MAX_CRASH_COUNT) {
            enableRecoveryMode()
        }
    }
    
    /**
     * Check if app should start in recovery mode
     */
    fun shouldStartInRecoveryMode(): Boolean {
        val lastLaunch = prefs.getLong(KEY_LAST_SUCCESSFUL_LAUNCH, 0)
        val lastCrash = prefs.getLong(KEY_LAST_CRASH_TIME, 0)
        val crashCount = prefs.getInt(KEY_CRASH_COUNT, 0)
        
        // If last crash was recent and we haven't had a successful launch since
        val recentCrash = lastCrash > lastLaunch && 
                (System.currentTimeMillis() - lastCrash) < CRASH_TIMEOUT_MS
        
        return recentCrash || crashCount >= MAX_CRASH_COUNT || 
                prefs.getBoolean(KEY_RECOVERY_MODE, false)
    }
    
    /**
     * Enable recovery mode
     */
    private fun enableRecoveryMode() {
        prefs.edit()
            .putBoolean(KEY_RECOVERY_MODE, true)
            .apply()
    }
    
    /**
     * Disable recovery mode
     */
    fun disableRecoveryMode() {
        prefs.edit()
            .putBoolean(KEY_RECOVERY_MODE, false)
            .putInt(KEY_CRASH_COUNT, 0)
            .apply()
    }
    
    /**
     * Save current app state for recovery
     */
    suspend fun saveAppState(currentScreen: String) {
        try {
            // Save current screen
            prefs.edit()
                .putString(KEY_LAST_SCREEN, currentScreen)
                .apply()
            
            // Backup watchlist
            val watchlist = userPreferencesRepository.getWatchlist().first()
            val watchlistJson = watchlist.joinToString(",")
            prefs.edit()
                .putString(KEY_WATCHLIST_BACKUP, watchlistJson)
                .apply()
                
        } catch (e: Exception) {
            // Don't crash while saving state
            e.printStackTrace()
        }
    }
    
    /**
     * Restore app state after crash
     */
    suspend fun restoreAppState(): RecoveryState {
        return try {
            val lastScreen = prefs.getString(KEY_LAST_SCREEN, "watchlist") ?: "watchlist"
            val watchlistBackup = prefs.getString(KEY_WATCHLIST_BACKUP, "") ?: ""
            val crashCount = prefs.getInt(KEY_CRASH_COUNT, 0)
            val lastCrashMessage = prefs.getString("last_crash_message", "Unknown error")
            
            RecoveryState(
                lastScreen = lastScreen,
                watchlistBackup = if (watchlistBackup.isNotEmpty()) {
                    watchlistBackup.split(",").filter { it.isNotBlank() }
                } else {
                    emptyList()
                },
                crashCount = crashCount,
                lastCrashMessage = lastCrashMessage ?: "Unknown error"
            )
        } catch (e: Exception) {
            RecoveryState(
                lastScreen = "watchlist",
                watchlistBackup = emptyList(),
                crashCount = 0,
                lastCrashMessage = "Recovery failed: ${e.message}"
            )
        }
    }
    
    /**
     * Clear all cached data (nuclear option for recovery)
     */
    suspend fun clearAllData() {
        try {
            // Clear database
            database.clearAllTables()
            
            // Clear preferences (except recovery data)
            val recoveryData = mapOf(
                KEY_CRASH_COUNT to prefs.getInt(KEY_CRASH_COUNT, 0),
                KEY_LAST_CRASH_TIME to prefs.getLong(KEY_LAST_CRASH_TIME, 0),
                KEY_RECOVERY_MODE to prefs.getBoolean(KEY_RECOVERY_MODE, false)
            )
            
            // Clear user preferences
            userPreferencesRepository.clearWatchlist()
            
            // Restore only recovery-related data
            prefs.edit().clear().apply()
            prefs.edit()
                .putInt(KEY_CRASH_COUNT, recoveryData[KEY_CRASH_COUNT] as Int)
                .putLong(KEY_LAST_CRASH_TIME, recoveryData[KEY_LAST_CRASH_TIME] as Long)
                .putBoolean(KEY_RECOVERY_MODE, recoveryData[KEY_RECOVERY_MODE] as Boolean)
                .apply()
                
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get recovery statistics
     */
    fun getRecoveryStats(): RecoveryStats {
        return RecoveryStats(
            crashCount = prefs.getInt(KEY_CRASH_COUNT, 0),
            lastCrashTime = prefs.getLong(KEY_LAST_CRASH_TIME, 0),
            lastSuccessfulLaunch = prefs.getLong(KEY_LAST_SUCCESSFUL_LAUNCH, 0),
            isInRecoveryMode = prefs.getBoolean(KEY_RECOVERY_MODE, false),
            lastCrashMessage = prefs.getString("last_crash_message", null),
            lastCrashType = prefs.getString("last_crash_type", null)
        )
    }
}

/**
 * State information for crash recovery
 */
data class RecoveryState(
    val lastScreen: String,
    val watchlistBackup: List<String>,
    val crashCount: Int,
    val lastCrashMessage: String
)

/**
 * Statistics about app crashes and recovery
 */
data class RecoveryStats(
    val crashCount: Int,
    val lastCrashTime: Long,
    val lastSuccessfulLaunch: Long,
    val isInRecoveryMode: Boolean,
    val lastCrashMessage: String?,
    val lastCrashType: String?
)