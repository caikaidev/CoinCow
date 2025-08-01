package com.kcode.gankotlin.widget.data

/**
 * Data class representing coin information for widget display
 */
data class WidgetCoinData(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val priceChangePercentage24h: Double,
    val imageUrl: String
) {
    /**
     * Formats the price for display in widget
     */
    fun getFormattedPrice(): String {
        return when {
            currentPrice >= 1000 -> "$${String.format("%.0f", currentPrice)}"
            currentPrice >= 1 -> "$${String.format("%.2f", currentPrice)}"
            else -> "$${String.format("%.4f", currentPrice)}"
        }
    }
    
    /**
     * Formats the price change percentage for display
     */
    fun getFormattedPriceChange(): String {
        val sign = if (priceChangePercentage24h >= 0) "+" else ""
        return "$sign${String.format("%.1f", priceChangePercentage24h)}%"
    }
    
    /**
     * Returns true if price change is positive
     */
    fun isPriceChangePositive(): Boolean {
        return priceChangePercentage24h >= 0
    }
}