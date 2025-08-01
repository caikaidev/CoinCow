package com.kcode.gankotlin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching price history data
 */
@Entity(tableName = "price_history")
data class PriceHistoryEntity(
    @PrimaryKey
    val cacheKey: String, // coinId_currency_days
    val coinId: String,
    val currency: String,
    val days: String,
    val pricesJson: String, // JSON string of price points
    val marketCapsJson: String?, // JSON string of market cap points
    val totalVolumesJson: String?, // JSON string of volume points
    val cachedAt: Long = System.currentTimeMillis()
)