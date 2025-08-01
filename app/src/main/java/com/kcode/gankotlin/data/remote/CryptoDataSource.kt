package com.kcode.gankotlin.data.remote

import com.kcode.gankotlin.domain.model.*

/**
 * Interface for cryptocurrency data source
 */
interface CryptoDataSource {
    
    /**
     * Get market data for cryptocurrencies
     */
    suspend fun getMarketData(
        currency: String = "usd",
        ids: List<String>? = null,
        page: Int = 1,
        perPage: Int = 100
    ): NetworkResult<List<CoinMarketData>>
    
    /**
     * Get detailed information about a specific coin
     */
    suspend fun getCoinDetails(coinId: String): NetworkResult<CoinDetails>
    
    /**
     * Get historical price data for a coin
     */
    suspend fun getCoinPriceHistory(
        coinId: String,
        currency: String = "usd",
        days: String
    ): NetworkResult<CoinPriceHistory>
    
    /**
     * Search for coins by name or symbol
     */
    suspend fun searchCoins(query: String): NetworkResult<List<SearchCoin>>
    
    /**
     * Check API connectivity
     */
    suspend fun ping(): NetworkResult<Boolean>
}