package com.kcode.gankotlin.data.repository

import com.kcode.gankotlin.data.local.UserPreferencesDataStore
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserPreferencesRepository using DataStore
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {
    
    override fun getWatchlist(): Flow<List<String>> {
        return dataStore.getWatchlist().map { it.toList() }
    }
    
    override suspend fun addToWatchlist(coinId: String) {
        dataStore.addToWatchlist(coinId)
    }
    
    override suspend fun removeFromWatchlist(coinId: String) {
        dataStore.removeFromWatchlist(coinId)
    }
    
    override suspend fun isInWatchlist(coinId: String): Boolean {
        return dataStore.isInWatchlist(coinId)
    }
    
    override suspend fun clearWatchlist() {
        dataStore.clearWatchlist()
    }
    
    override fun getPreferredCurrency(): Flow<String> {
        return dataStore.getPreferredCurrency()
    }
    
    override suspend fun setPreferredCurrency(currency: String) {
        dataStore.setPreferredCurrency(currency)
    }
    
    override fun getThemePreference(): Flow<String> {
        return dataStore.getThemePreference()
    }
    
    override suspend fun setThemePreference(theme: String) {
        dataStore.setThemePreference(theme)
    }
    
    override fun getRefreshInterval(): Flow<Long> {
        return dataStore.getRefreshInterval()
    }
    
    override suspend fun setRefreshInterval(intervalMinutes: Long) {
        dataStore.setRefreshInterval(intervalMinutes)
    }
}