package com.kcode.gankotlin.domain.repository

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for cryptocurrency data
 */
interface CryptoRepository {
    
    /**
     * Get market data for cryptocurrencies with caching
     */
    suspend fun getMarketData(
        currency: String = "usd",
        forceRefresh: Boolean = false
    ): NetworkResult<List<CoinMarketData>>
    
    /**
     * Get market data for specific coins
     */
    suspend fun getWatchlistMarketData(
        coinIds: List<String>,
        currency: String = "usd",
        forceRefresh: Boolean = false
    ): NetworkResult<List<CoinMarketData>>
    
    /**
     * Get detailed information about a specific coin
     */
    suspend fun getCoinDetails(
        coinId: String,
        forceRefresh: Boolean = false
    ): NetworkResult<CoinDetails>
    
    /**
     * Get historical price data for a coin
     */
    suspend fun getCoinPriceHistory(
        coinId: String,
        currency: String = "usd",
        days: String,
        forceRefresh: Boolean = false
    ): NetworkResult<CoinPriceHistory>
    
    /**
     * Search for coins by name or symbol
     */
    suspend fun searchCoins(query: String): NetworkResult<List<SearchCoin>>
    
    /**
     * Get cached market data as Flow for reactive updates
     */
    fun getMarketDataFlow(): Flow<List<CoinMarketData>>
    
    /**
     * Clear all cached data
     */
    suspend fun clearCache()
    
    /**
     * Check if cached data is still valid
     */
    suspend fun isCacheValid(cacheKey: String): Boolean
}