package com.kcode.gankotlin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching coin details
 */
@Entity(tableName = "coin_details")
data class CoinDetailsEntity(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val name: String,
    val description: String,
    val imageThumb: String,
    val imageSmall: String,
    val imageLarge: String,
    val currentPriceUsd: Double?,
    val marketCapUsd: Long?,
    val totalVolumeUsd: Long?,
    val high24hUsd: Double?,
    val low24hUsd: Double?,
    val priceChange24h: Double?,
    val priceChangePercentage24h: Double?,
    val priceChangePercentage7d: Double?,
    val priceChangePercentage30d: Double?,
    val marketCapChange24h: Double?,
    val marketCapChangePercentage24h: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val circulatingSupply: Double?,
    val sparkline7d: String?, // JSON string of price array
    val lastUpdated: String,
    val cachedAt: Long = System.currentTimeMillis()
)