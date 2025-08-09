package com.kcode.gankotlin.domain.model

/**
 * Core domain model representing cryptocurrency market data
 */
data class CoinMarketData(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val currentPrice: Double,
    val marketCap: Double?,
    val marketCapRank: Int?,
    val fullyDilutedValuation: Double?,
    val totalVolume: Double?,
    val high24h: Double?,
    val low24h: Double?,
    val priceChange24h: Double?,
    val priceChangePercentage24h: Double?,
    val marketCapChange24h: Double?,
    val marketCapChangePercentage24h: Double?,
    val circulatingSupply: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val ath: Double?,
    val athChangePercentage: Double?,
    val athDate: String?,
    val atl: Double?,
    val atlChangePercentage: Double?,
    val atlDate: String?,
    val lastUpdated: String
) {
    /**
     * Validates if the price data is valid and within reasonable bounds
     */
    fun isValidPriceData(): Boolean {
        return currentPrice > 0 && 
               priceChangePercentage24h?.let { it >= -100 && it <= 10000 } ?: true
    }
    
    /**
     * Returns formatted price change percentage with proper sign
     */
    fun getFormattedPriceChangePercentage(): String {
        return priceChangePercentage24h?.let { percentage ->
            val sign = if (percentage >= 0) "+" else ""
            "$sign%.2f%%".format(percentage)
        } ?: "N/A"
    }
    
    /**
     * Returns true if the price is trending up in the last 24h
     */
    fun isPriceUp(): Boolean {
        return priceChangePercentage24h?.let { it > 0 } ?: false
    }
}