package com.kcode.gankotlin.data.local.dao

import androidx.room.*
import com.kcode.gankotlin.data.local.entity.CoinMarketDataEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for coin market data operations
 */
@Dao
interface CoinMarketDataDao {
    
    @Query("SELECT * FROM coin_market_data ORDER BY marketCapRank ASC")
    fun getAllMarketData(): Flow<List<CoinMarketDataEntity>>
    
    @Query("SELECT * FROM coin_market_data WHERE id IN (:coinIds)")
    suspend fun getMarketDataByIds(coinIds: List<String>): List<CoinMarketDataEntity>
    
    @Query("SELECT * FROM coin_market_data WHERE id IN (:coinIds)")
    fun getMarketDataByIdsFlow(coinIds: List<String>): Flow<List<CoinMarketDataEntity>>
    
    @Query("SELECT * FROM coin_market_data WHERE id = :coinId")
    suspend fun getMarketDataById(coinId: String): CoinMarketDataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketData(data: List<CoinMarketDataEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleMarketData(data: CoinMarketDataEntity)
    
    @Query("DELETE FROM coin_market_data")
    suspend fun clearAllMarketData()
    
    @Query("DELETE FROM coin_market_data WHERE cachedAt < :expireTime")
    suspend fun deleteExpiredData(expireTime: Long)
    
    @Query("SELECT COUNT(*) FROM coin_market_data WHERE cachedAt > :validTime")
    suspend fun getCachedDataCount(validTime: Long): Int
    
    @Query("SELECT MAX(cachedAt) FROM coin_market_data")
    suspend fun getLatestCacheTime(): Long?
}