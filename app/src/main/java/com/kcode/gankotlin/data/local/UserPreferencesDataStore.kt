package com.kcode.gankotlin.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore implementation for user preferences
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        
        private val WATCHLIST_KEY = stringSetPreferencesKey("watchlist")
        private val PREFERRED_CURRENCY_KEY = stringPreferencesKey("preferred_currency")
        private val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")
        private val REFRESH_INTERVAL_KEY = longPreferencesKey("refresh_interval")
        
        private const val DEFAULT_CURRENCY = "usd"
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_REFRESH_INTERVAL = 1L // 1 minute
    }
    
    /**
     * Get user's watchlist
     */
    fun getWatchlist(): Flow<Set<String>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[WATCHLIST_KEY] ?: emptySet()
            }
    }
    
    /**
     * Add coin to watchlist
     */
    suspend fun addToWatchlist(coinId: String) {
        context.dataStore.edit { preferences ->
            val currentWatchlist = preferences[WATCHLIST_KEY] ?: emptySet()
            preferences[WATCHLIST_KEY] = currentWatchlist + coinId
        }
    }
    
    /**
     * Remove coin from watchlist
     */
    suspend fun removeFromWatchlist(coinId: String) {
        context.dataStore.edit { preferences ->
            val currentWatchlist = preferences[WATCHLIST_KEY] ?: emptySet()
            preferences[WATCHLIST_KEY] = currentWatchlist - coinId
        }
    }
    
    /**
     * Check if coin is in watchlist
     */
    suspend fun isInWatchlist(coinId: String): Boolean {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val watchlist = preferences[WATCHLIST_KEY] ?: emptySet()
                watchlist.contains(coinId)
            }
            .first()
    }
    
    /**
     * Clear entire watchlist
     */
    suspend fun clearWatchlist() {
        context.dataStore.edit { preferences ->
            preferences[WATCHLIST_KEY] = emptySet()
        }
    }
    
    /**
     * Get preferred currency
     */
    fun getPreferredCurrency(): Flow<String> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[PREFERRED_CURRENCY_KEY] ?: DEFAULT_CURRENCY
            }
    }
    
    /**
     * Set preferred currency
     */
    suspend fun setPreferredCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_CURRENCY_KEY] = currency
        }
    }
    
    /**
     * Get theme preference
     */
    fun getThemePreference(): Flow<String> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[THEME_PREFERENCE_KEY] ?: DEFAULT_THEME
            }
    }
    
    /**
     * Set theme preference
     */
    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_PREFERENCE_KEY] = theme
        }
    }
    
    /**
     * Get refresh interval preference
     */
    fun getRefreshInterval(): Flow<Long> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[REFRESH_INTERVAL_KEY] ?: DEFAULT_REFRESH_INTERVAL
            }
    }
    
    /**
     * Set refresh interval preference
     */
    suspend fun setRefreshInterval(intervalMinutes: Long) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_INTERVAL_KEY] = intervalMinutes
        }
    }
}