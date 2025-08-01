package com.kcode.gankotlin.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences and settings
 */
interface UserPreferencesRepository {
    
    /**
     * Get user's watchlist (favorite coins)
     */
    fun getWatchlist(): Flow<List<String>>
    
    /**
     * Add coin to watchlist
     */
    suspend fun addToWatchlist(coinId: String)
    
    /**
     * Remove coin from watchlist
     */
    suspend fun removeFromWatchlist(coinId: String)
    
    /**
     * Check if coin is in watchlist
     */
    suspend fun isInWatchlist(coinId: String): Boolean
    
    /**
     * Clear entire watchlist
     */
    suspend fun clearWatchlist()
    
    /**
     * Get preferred currency
     */
    fun getPreferredCurrency(): Flow<String>
    
    /**
     * Set preferred currency
     */
    suspend fun setPreferredCurrency(currency: String)
    
    /**
     * Get theme preference
     */
    fun getThemePreference(): Flow<String>
    
    /**
     * Set theme preference
     */
    suspend fun setThemePreference(theme: String)
    
    /**
     * Get refresh interval preference
     */
    fun getRefreshInterval(): Flow<Long>
    
    /**
     * Set refresh interval preference
     */
    suspend fun setRefreshInterval(intervalMinutes: Long)
}