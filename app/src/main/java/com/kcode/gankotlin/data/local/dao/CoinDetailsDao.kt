package com.kcode.gankotlin.data.local.dao

import androidx.room.*
import com.kcode.gankotlin.data.local.entity.CoinDetailsEntity

/**
 * DAO for coin details operations
 */
@Dao
interface CoinDetailsDao {
    
    @Query("SELECT * FROM coin_details WHERE id = :coinId")
    suspend fun getCoinDetails(coinId: String): CoinDetailsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinDetails(details: CoinDetailsEntity)
    
    @Query("DELETE FROM coin_details WHERE id = :coinId")
    suspend fun deleteCoinDetails(coinId: String)
    
    @Query("DELETE FROM coin_details")
    suspend fun clearAllDetails()
    
    @Query("DELETE FROM coin_details WHERE cachedAt < :expireTime")
    suspend fun deleteExpiredDetails(expireTime: Long)
    
    @Query("SELECT cachedAt FROM coin_details WHERE id = :coinId")
    suspend fun getCacheTime(coinId: String): Long?
}