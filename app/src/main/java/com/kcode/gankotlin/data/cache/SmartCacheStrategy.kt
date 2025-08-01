package com.kcode.gankotlin.data.cache

import com.kcode.gankotlin.data.remote.NetworkConnectivityManager
import com.kcode.gankotlin.data.remote.NetworkType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Smart caching strategy that adapts based on network conditions
 */
@Singleton
class SmartCacheStrategy @Inject constructor(
    private val networkConnectivityManager: NetworkConnectivityManager
) {
    
    /**
     * Get cache expiry time based on network conditions and data type
     */
    fun getCacheExpiryTime(dataType: CacheDataType): Long {
        val networkType = networkConnectivityManager.getNetworkType()
        val baseExpiry = getBaseExpiryTime(dataType)
        
        return when (networkType) {
            NetworkType.WIFI -> baseExpiry // Normal expiry on WiFi
            NetworkType.CELLULAR -> baseExpiry * 2 // Longer cache on cellular
            NetworkType.ETHERNET -> baseExpiry / 2 // Shorter cache on fast connection
            NetworkType.OTHER, NetworkType.NONE -> baseExpiry * 4 // Much longer cache on poor connection
        }
    }
    
    /**
     * Get base cache expiry time for different data types
     */
    private fun getBaseExpiryTime(dataType: CacheDataType): Long {
        return when (dataType) {
            CacheDataType.MARKET_DATA -> 60_000L // 1 minute
            CacheDataType.COIN_DETAILS -> 300_000L // 5 minutes
            CacheDataType.PRICE_HISTORY -> 300_000L // 5 minutes
            CacheDataType.SEARCH_RESULTS -> 600_000L // 10 minutes
            CacheDataType.USER_PREFERENCES -> Long.MAX_VALUE // Never expire
        }
    }
    
    /**
     * Determine if we should use cache based on network conditions
     */
    fun shouldUseCache(dataType: CacheDataType, cacheAge: Long): Boolean {
        val networkType = networkConnectivityManager.getNetworkType()
        val expiryTime = getCacheExpiryTime(dataType)
        
        // Always use cache if no network
        if (networkType == NetworkType.NONE) {
            return true
        }
        
        // Use cache if within expiry time
        if (cacheAge < expiryTime) {
            return true
        }
        
        // On slow networks, be more lenient with cache
        if (networkType == NetworkType.CELLULAR && cacheAge < expiryTime * 2) {
            return true
        }
        
        return false
    }
    
    /**
     * Get prefetch strategy based on network conditions
     */
    fun getPrefetchStrategy(): PrefetchStrategy {
        val networkType = networkConnectivityManager.getNetworkType()
        
        return when (networkType) {
            NetworkType.WIFI -> PrefetchStrategy.AGGRESSIVE // Prefetch more on WiFi
            NetworkType.ETHERNET -> PrefetchStrategy.AGGRESSIVE
            NetworkType.CELLULAR -> PrefetchStrategy.CONSERVATIVE // Prefetch less on cellular
            NetworkType.OTHER -> PrefetchStrategy.MINIMAL
            NetworkType.NONE -> PrefetchStrategy.NONE
        }
    }
    
    /**
     * Get optimal batch size for network requests
     */
    fun getOptimalBatchSize(dataType: CacheDataType): Int {
        val networkType = networkConnectivityManager.getNetworkType()
        
        val baseBatchSize = when (dataType) {
            CacheDataType.MARKET_DATA -> 50
            CacheDataType.COIN_DETAILS -> 10
            CacheDataType.PRICE_HISTORY -> 5
            CacheDataType.SEARCH_RESULTS -> 20
            CacheDataType.USER_PREFERENCES -> 1
        }
        
        return when (networkType) {
            NetworkType.WIFI, NetworkType.ETHERNET -> baseBatchSize
            NetworkType.CELLULAR -> baseBatchSize / 2
            NetworkType.OTHER -> baseBatchSize / 4
            NetworkType.NONE -> 0
        }
    }
    
    /**
     * Determine if we should compress requests
     */
    fun shouldCompressRequests(): Boolean {
        val networkType = networkConnectivityManager.getNetworkType()
        return networkType == NetworkType.CELLULAR || networkType == NetworkType.OTHER
    }
}

enum class CacheDataType {
    MARKET_DATA,
    COIN_DETAILS,
    PRICE_HISTORY,
    SEARCH_RESULTS,
    USER_PREFERENCES
}

enum class PrefetchStrategy {
    NONE,
    MINIMAL,
    CONSERVATIVE,
    AGGRESSIVE
}