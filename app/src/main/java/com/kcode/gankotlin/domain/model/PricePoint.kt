package com.kcode.gankotlin.domain.model

/**
 * Represents a single price point for chart data
 */
data class PricePoint(
    val timestamp: Long,
    val price: Double
) {
    /**
     * Validates if the price point data is valid
     */
    fun isValid(): Boolean {
        return timestamp > 0 && price > 0 && price.isFinite()
    }
}

/**
 * Represents historical price data for a cryptocurrency
 */
data class CoinPriceHistory(
    val coinId: String,
    val currency: String,
    val prices: List<PricePoint>,
    val marketCaps: List<PricePoint>?,
    val totalVolumes: List<PricePoint>?
) {
    /**
     * Returns filtered valid price points
     */
    fun getValidPrices(): List<PricePoint> {
        return prices.filter { it.isValid() }
    }
    
    /**
     * Returns the price change percentage over the period
     */
    fun getPriceChangePercentage(): Double? {
        val validPrices = getValidPrices()
        if (validPrices.size < 2) return null
        
        val firstPrice = validPrices.first().price
        val lastPrice = validPrices.last().price
        
        return ((lastPrice - firstPrice) / firstPrice) * 100
    }
    
    /**
     * Returns the highest price in the period
     */
    fun getHighestPrice(): Double? {
        return getValidPrices().maxOfOrNull { it.price }
    }
    
    /**
     * Returns the lowest price in the period
     */
    fun getLowestPrice(): Double? {
        return getValidPrices().minOfOrNull { it.price }
    }
}