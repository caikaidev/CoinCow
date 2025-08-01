package com.kcode.gankotlin.data.validator

import com.kcode.gankotlin.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validator for data integrity and consistency
 */
@Singleton
class DataIntegrityValidator @Inject constructor() {
    
    /**
     * Validate coin market data for integrity issues
     */
    fun validateCoinMarketData(data: CoinMarketData): Boolean {
        // Check for unrealistic price changes
        val priceChange = data.priceChangePercentage24h
        if (priceChange != null && kotlin.math.abs(priceChange) > 1000.0) {
            return false
        }
        
        // Check for negative values where they shouldn't be
        if (data.currentPrice < 0 || 
            (data.marketCap != null && data.marketCap < 0) ||
            (data.totalVolume != null && data.totalVolume < 0)) {
            return false
        }
        
        // Check for infinite or NaN values
        if (!data.currentPrice.isFinite()) {
            return false
        }
        
        // Basic field validation
        if (data.id.isBlank() || data.symbol.isBlank() || data.name.isBlank()) {
            return false
        }
        
        return true
    }
    
    /**
     * Sanitize coin market data by fixing invalid values
     */
    fun sanitizeCoinMarketData(data: CoinMarketData): CoinMarketData {
        return data.copy(
            currentPrice = if (data.currentPrice.isFinite() && data.currentPrice >= 0) {
                data.currentPrice
            } else {
                0.0
            },
            marketCap = if (data.marketCap != null && data.marketCap >= 0) {
                data.marketCap
            } else {
                null
            },
            totalVolume = if (data.totalVolume != null && data.totalVolume >= 0) {
                data.totalVolume
            } else {
                null
            },
            priceChangePercentage24h = if (data.priceChangePercentage24h?.isFinite() == true && 
                kotlin.math.abs(data.priceChangePercentage24h) <= 1000.0) {
                data.priceChangePercentage24h
            } else {
                null
            },
            marketCapRank = if (data.marketCapRank != null && data.marketCapRank > 0) {
                data.marketCapRank
            } else {
                null
            }
        )
    }
    
    /**
     * Validate price history data
     */
    fun validatePriceHistory(priceHistory: CoinPriceHistory): Boolean {
        if (priceHistory.coinId.isBlank() || priceHistory.currency.isBlank()) {
            return false
        }
        
        if (priceHistory.prices.isEmpty()) {
            return false
        }
        
        // Check for invalid price points
        val invalidPrices = priceHistory.prices.filter { !it.isValid() }
        if (invalidPrices.size > priceHistory.prices.size * 0.2) { // More than 20% invalid
            return false
        }
        
        return true
    }
    
    /**
     * Sanitize price history by removing invalid price points
     */
    fun sanitizePriceHistory(priceHistory: CoinPriceHistory): CoinPriceHistory {
        val validPrices = priceHistory.prices
            .filter { it.isValid() }
            .distinctBy { it.timestamp }
            .sortedBy { it.timestamp }
        
        return priceHistory.copy(
            prices = validPrices,
            marketCaps = priceHistory.marketCaps?.filter { 
                it.timestamp > 0 && it.price >= 0 && it.price.isFinite() 
            }?.distinctBy { it.timestamp }?.sortedBy { it.timestamp },
            totalVolumes = priceHistory.totalVolumes?.filter { 
                it.timestamp > 0 && it.price >= 0 && it.price.isFinite() 
            }?.distinctBy { it.timestamp }?.sortedBy { it.timestamp }
        )
    }
    
    /**
     * Detect potential API response corruption
     */
    fun detectResponseCorruption(dataList: List<CoinMarketData>): Boolean {
        if (dataList.isEmpty()) return true
        
        // Check if too many coins have zero price (indicates API issue)
        val zeroPriceCount = dataList.count { it.currentPrice == 0.0 }
        if (zeroPriceCount > dataList.size * 0.1) {
            return true
        }
        
        // Check if all coins have the same price (indicates API corruption)
        val uniquePrices = dataList.map { it.currentPrice }.toSet()
        if (uniquePrices.size == 1 && dataList.size > 1) {
            return true
        }
        
        // Check for duplicate coin IDs
        val uniqueIds = dataList.map { it.id }.toSet()
        if (uniqueIds.size != dataList.size) {
            return true
        }
        
        return false
    }
}