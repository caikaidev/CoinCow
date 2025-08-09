package com.kcode.gankotlin.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val WATCHLIST = stringSetPreferencesKey("watchlist")
        val PREFERRED_CURRENCY = stringPreferencesKey("preferred_currency")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val REFRESH_INTERVAL = longPreferencesKey("refresh_interval")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }
    
    // Watchlist methods
    fun getWatchlist(): Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WATCHLIST] ?: emptySet()
    }
    
    suspend fun addToWatchlist(coinId: String) {
        context.dataStore.edit { preferences ->
            val currentWatchlist = preferences[PreferencesKeys.WATCHLIST] ?: emptySet()
            preferences[PreferencesKeys.WATCHLIST] = currentWatchlist + coinId
        }
    }
    
    suspend fun removeFromWatchlist(coinId: String) {
        context.dataStore.edit { preferences ->
            val currentWatchlist = preferences[PreferencesKeys.WATCHLIST] ?: emptySet()
            preferences[PreferencesKeys.WATCHLIST] = currentWatchlist - coinId
        }
    }
    
    suspend fun isInWatchlist(coinId: String): Boolean {
        return context.dataStore.data.map { preferences ->
            val watchlist = preferences[PreferencesKeys.WATCHLIST] ?: emptySet()
            watchlist.contains(coinId)
        }.first()
    }
    
    suspend fun clearWatchlist() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATCHLIST] = emptySet()
        }
    }
    
    // Currency preference
    fun getPreferredCurrency(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PREFERRED_CURRENCY] ?: "usd"
    }
    
    suspend fun setPreferredCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_CURRENCY] = currency
        }
    }
    
    // Theme preference
    fun getThemePreference(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_PREFERENCE] ?: "system"
    }
    
    suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE] = theme
        }
    }
    
    // Refresh interval
    fun getRefreshInterval(): Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REFRESH_INTERVAL] ?: 60L // Default 60 seconds
    }
    
    suspend fun setRefreshInterval(intervalMinutes: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFRESH_INTERVAL] = intervalMinutes
        }
    }
    
    // First launch methods
    fun isFirstLaunch(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
    }
    
    suspend fun markFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }
    
    suspend fun resetFirstLaunchStatus() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = true
        }
    }
}