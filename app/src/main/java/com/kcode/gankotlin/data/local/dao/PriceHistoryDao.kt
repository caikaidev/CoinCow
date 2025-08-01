package com.kcode.gankotlin.data.local.dao

import androidx.room.*
import com.kcode.gankotlin.data.local.entity.PriceHistoryEntity

/**
 * DAO for price history operations
 */
@Dao
interface PriceHistoryDao {
    
    @Query("SELECT * FROM price_history WHERE cacheKey = :cacheKey")
    suspend fun getPriceHistory(cacheKey: String): PriceHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistoryEntity)
    
    @Query("DELETE FROM price_history WHERE cacheKey = :cacheKey")
    suspend fun deletePriceHistory(cacheKey: String)
    
    @Query("DELETE FROM price_history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM price_history WHERE cachedAt < :expireTime")
    suspend fun deleteExpiredHistory(expireTime: Long)
    
    @Query("SELECT cachedAt FROM price_history WHERE cacheKey = :cacheKey")
    suspend fun getCacheTime(cacheKey: String): Long?
    
    /**
     * Generate cache key for price history
     */
    companion object {
        fun generateCacheKey(coinId: String, currency: String, days: String): String {
            return "${coinId}_${currency}_$days"
        }
    }
}