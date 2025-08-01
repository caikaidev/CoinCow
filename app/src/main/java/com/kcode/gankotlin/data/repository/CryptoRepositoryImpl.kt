package com.kcode.gankotlin.data.repository

import com.kcode.gankotlin.data.local.CryptoDatabase
import com.kcode.gankotlin.data.mapper.*
import com.kcode.gankotlin.data.remote.CryptoDataSource
import com.kcode.gankotlin.data.remote.NetworkConnectivityManager
import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.*
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CryptoRepository with caching strategy
 */
@Singleton
class CryptoRepositoryImpl @Inject constructor(
    private val remoteDataSource: CryptoDataSource,
    private val database: CryptoDatabase,
    private val networkConnectivityManager: NetworkConnectivityManager,
    private val moshi: Moshi
) : CryptoRepository {
    
    companion object {
        private const val CACHE_EXPIRY_TIME_MS = 60_000L // 1 minute
        private const val DETAILS_CACHE_EXPIRY_TIME_MS = 300_000L // 5 minutes
        private const val PRICE_HISTORY_CACHE_EXPIRY_TIME_MS = 300_000L // 5 minutes
    }
    
    override suspend fun getMarketData(
        currency: String,
        forceRefresh: Boolean
    ): NetworkResult<List<CoinMarketData>> {
        println("CryptoRepository: getMarketData called with currency=$currency, forceRefresh=$forceRefresh")
        
        // Check cache first if not forcing refresh
        if (!forceRefresh && isCacheValid("market_data")) {
            println("CryptoRepository: Cache is valid, trying to return cached data")
            val cachedData = database.coinMarketDataDao().getAllMarketData()
            // Convert Flow to list for immediate return
            try {
                val cachedList = cachedData.first().map { it.toDomainModel() }
                if (cachedList.isNotEmpty()) {
                    println("CryptoRepository: Returning ${cachedList.size} cached coins")
                    return NetworkResult.Success(cachedList)
                }
            } catch (e: Exception) {
                println("CryptoRepository: Cache read failed: ${e.message}")
                // Continue to network fetch if cache fails
            }
        }
        
        // Check network connectivity
        val isConnected = networkConnectivityManager.isConnected()
        println("CryptoRepository: Network connected: $isConnected")
        
        // Fetch from network
        if (!isConnected) {
            println("CryptoRepository: No network, trying cached data")
            // Return cached data if available, even if expired
            val cachedData = database.coinMarketDataDao().getAllMarketData()
            try {
                val cachedList = cachedData.first().map { it.toDomainModel() }
                if (cachedList.isNotEmpty()) {
                    println("CryptoRepository: Returning ${cachedList.size} expired cached coins")
                    return NetworkResult.Success(cachedList)
                } else {
                    return NetworkResult.Error(Exception("No internet connection and no cached data available"))
                }
            } catch (e: Exception) {
                return NetworkResult.Error(Exception("No internet connection and no cached data available"))
            }
        }
        
        println("CryptoRepository: Fetching from network...")
        return when (val result = remoteDataSource.getMarketData(currency = currency)) {
            is NetworkResult.Success -> {
                println("CryptoRepository: Network fetch successful, caching ${result.data.size} coins")
                // Cache the data
                val entities = result.data.map { it.toEntity() }
                database.coinMarketDataDao().insertMarketData(entities)
                result
            }
            is NetworkResult.Error -> {
                println("CryptoRepository: Network fetch failed: ${result.message}")
                // Try to return cached data on error
                val cachedData = database.coinMarketDataDao().getAllMarketData()
                try {
                    val cachedList = cachedData.first().map { it.toDomainModel() }
                    if (cachedList.isNotEmpty()) {
                        println("CryptoRepository: Returning ${cachedList.size} cached coins as fallback")
                        return NetworkResult.Success(cachedList)
                    }
                } catch (e: Exception) {
                    println("CryptoRepository: Cache fallback failed: ${e.message}")
                    // Return original error if no cache available
                }
                result
            }
            is NetworkResult.Loading -> {
                println("CryptoRepository: Network returned loading state")
                result
            }
        }
    }
    
    override suspend fun getWatchlistMarketData(
        coinIds: List<String>,
        currency: String,
        forceRefresh: Boolean
    ): NetworkResult<List<CoinMarketData>> {
        if (coinIds.isEmpty()) {
            return NetworkResult.Success(emptyList())
        }
        
        // Check cache first
        if (!forceRefresh) {
            val cachedData = database.coinMarketDataDao().getMarketDataByIds(coinIds)
            val validCachedData = cachedData.filter { 
                System.currentTimeMillis() - it.cachedAt < CACHE_EXPIRY_TIME_MS 
            }
            
            if (validCachedData.size == coinIds.size) {
                return NetworkResult.Success(validCachedData.map { it.toDomainModel() })
            }
        }
        
        // Fetch from network
        if (!networkConnectivityManager.isConnected()) {
            val cachedData = database.coinMarketDataDao().getMarketDataByIds(coinIds)
            if (cachedData.isNotEmpty()) {
                return NetworkResult.Success(cachedData.map { it.toDomainModel() })
            }
            return NetworkResult.Error(Exception("No internet connection and no cached data available"))
        }
        
        return when (val result = remoteDataSource.getMarketData(currency = currency, ids = coinIds)) {
            is NetworkResult.Success -> {
                // Cache the data
                val entities = result.data.map { it.toEntity() }
                database.coinMarketDataDao().insertMarketData(entities)
                result
            }
            is NetworkResult.Error -> {
                // Try to return cached data on error
                val cachedData = database.coinMarketDataDao().getMarketDataByIds(coinIds)
                if (cachedData.isNotEmpty()) {
                    NetworkResult.Success(cachedData.map { it.toDomainModel() })
                } else {
                    result
                }
            }
            is NetworkResult.Loading -> result
        }
    }
    
    override suspend fun getCoinDetails(
        coinId: String,
        forceRefresh: Boolean
    ): NetworkResult<CoinDetails> {
        // Check cache first
        if (!forceRefresh) {
            val cachedDetails = database.coinDetailsDao().getCoinDetails(coinId)
            if (cachedDetails != null && 
                System.currentTimeMillis() - cachedDetails.cachedAt < DETAILS_CACHE_EXPIRY_TIME_MS) {
                return NetworkResult.Success(cachedDetails.toDomainModel(moshi))
            }
        }
        
        // Fetch from network
        if (!networkConnectivityManager.isConnected()) {
            val cachedDetails = database.coinDetailsDao().getCoinDetails(coinId)
            if (cachedDetails != null) {
                return NetworkResult.Success(cachedDetails.toDomainModel(moshi))
            }
            return NetworkResult.Error(Exception("No internet connection and no cached data available"))
        }
        
        return when (val result = remoteDataSource.getCoinDetails(coinId)) {
            is NetworkResult.Success -> {
                // Cache the data
                val entity = result.data.toEntity(moshi)
                database.coinDetailsDao().insertCoinDetails(entity)
                result
            }
            is NetworkResult.Error -> {
                // Try to return cached data on error
                val cachedDetails = database.coinDetailsDao().getCoinDetails(coinId)
                if (cachedDetails != null) {
                    NetworkResult.Success(cachedDetails.toDomainModel(moshi))
                } else {
                    result
                }
            }
            is NetworkResult.Loading -> result
        }
    }
    
    override suspend fun getCoinPriceHistory(
        coinId: String,
        currency: String,
        days: String,
        forceRefresh: Boolean
    ): NetworkResult<CoinPriceHistory> {
        val cacheKey = "${coinId}_${currency}_$days"
        
        // Check cache first
        if (!forceRefresh) {
            val cachedHistory = database.priceHistoryDao().getPriceHistory(cacheKey)
            if (cachedHistory != null && 
                System.currentTimeMillis() - cachedHistory.cachedAt < PRICE_HISTORY_CACHE_EXPIRY_TIME_MS) {
                return NetworkResult.Success(cachedHistory.toDomainModel(moshi))
            }
        }
        
        // Fetch from network
        if (!networkConnectivityManager.isConnected()) {
            val cachedHistory = database.priceHistoryDao().getPriceHistory(cacheKey)
            if (cachedHistory != null) {
                return NetworkResult.Success(cachedHistory.toDomainModel(moshi))
            }
            return NetworkResult.Error(Exception("No internet connection and no cached data available"))
        }
        
        return when (val result = remoteDataSource.getCoinPriceHistory(coinId, currency, days)) {
            is NetworkResult.Success -> {
                // Cache the data
                val entity = result.data.toEntity(moshi)
                database.priceHistoryDao().insertPriceHistory(entity)
                result
            }
            is NetworkResult.Error -> {
                // Try to return cached data on error
                val cachedHistory = database.priceHistoryDao().getPriceHistory(cacheKey)
                if (cachedHistory != null) {
                    NetworkResult.Success(cachedHistory.toDomainModel(moshi))
                } else {
                    result
                }
            }
            is NetworkResult.Loading -> result
        }
    }
    
    override suspend fun searchCoins(query: String): NetworkResult<List<SearchCoin>> {
        // Search is always done from network (no caching for search results)
        if (!networkConnectivityManager.isConnected()) {
            return NetworkResult.Error(Exception("No internet connection available for search"))
        }
        
        return remoteDataSource.searchCoins(query)
    }
    
    override fun getMarketDataFlow(): Flow<List<CoinMarketData>> {
        return database.coinMarketDataDao().getAllMarketData()
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override suspend fun clearCache() {
        database.coinMarketDataDao().clearAllMarketData()
        database.coinDetailsDao().clearAllDetails()
        database.priceHistoryDao().clearAllHistory()
    }
    
    override suspend fun isCacheValid(cacheKey: String): Boolean {
        return when (cacheKey) {
            "market_data" -> {
                val latestCacheTime = database.coinMarketDataDao().getLatestCacheTime()
                latestCacheTime?.let { 
                    System.currentTimeMillis() - it < CACHE_EXPIRY_TIME_MS 
                } ?: false
            }
            else -> false
        }
    }
}