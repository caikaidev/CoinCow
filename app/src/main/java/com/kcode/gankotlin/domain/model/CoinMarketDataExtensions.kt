package com.kcode.gankotlin.domain.model

/**
 * Extension functions for CoinMarketData to handle text processing and formatting
 */

/**
 * Get display name with smart truncation
 */
fun CoinMarketData.getDisplayName(maxLength: Int = 15): String {
    return if (name.length > maxLength) {
        name.smartTruncate(maxLength)
    } else {
        name
    }
}

/**
 * Get formatted market cap with appropriate units
 */
fun CoinMarketData.getFormattedMarketCap(): String? {
    return marketCap?.let { cap ->
        when {
            cap >= 1_000_000_000_000 -> "${(cap.toDouble() / 1_000_000_000_000).safeFormat(1)}T"
            cap >= 1_000_000_000 -> "${(cap.toDouble() / 1_000_000_000).safeFormat(1)}B"
            cap >= 1_000_000 -> "${(cap.toDouble() / 1_000_000).safeFormat(1)}M"
            cap >= 1_000 -> "${(cap.toDouble() / 1_000).safeFormat(1)}K"
            else -> null // Small market cap not displayed
        }
    }
}

/**
 * Check if market cap should be displayed based on screen size
 */
fun CoinMarketData.shouldShowMarketCap(isCompactScreen: Boolean): Boolean {
    return !isCompactScreen && marketCap != null && marketCap!! >= 1_000_000
}

/**
 * Smart text truncation that prefers word boundaries
 */
fun String.smartTruncate(maxLength: Int): String {
    return safeSmartTruncate(maxLength)
}

/**
 * Safe smart text truncation with enhanced error handling
 */
fun String.safeSmartTruncate(maxLength: Int): String {
    if (maxLength <= 0) return ""
    if (length <= maxLength) return this
    
    // Handle special characters and emojis
    val cleanText = this.filter { it.isLetterOrDigit() || it.isWhitespace() || it in ".-_" }
    
    if (cleanText.length <= maxLength) return cleanText
    
    // Smart truncation with word boundaries
    val breakChars = listOf(' ', '-', '.', '_')
    for (breakChar in breakChars) {
        val breakIndex = cleanText.lastIndexOf(breakChar, maxLength - 2)
        if (breakIndex > maxLength / 2) {
            return "${cleanText.take(breakIndex)}…"
        }
    }
    
    // Fallback to character boundary
    return "${cleanText.take(maxLength - 1)}…"
}



/**
 * Safe number formatting with fallbacks
 */
fun Double.safeFormat(decimals: Int = 2): String {
    return try {
        when {
            !isFinite() -> "N/A"
            this == 0.0 -> "0"
            else -> "%.${decimals}f".format(this)
        }
    } catch (e: Exception) {
        "N/A"
    }
}

/**
 * Check if price is up (positive change)
 */
fun CoinMarketData.isPriceUp(): Boolean {
    return priceChangePercentage24h?.let { it >= 0 } ?: false
}

/**
 * Get formatted price change percentage with sign
 */
fun CoinMarketData.getFormattedPriceChangePercentage(): String {
    return priceChangePercentage24h?.let { change ->
        val sign = if (change >= 0) "+" else ""
        "$sign${"%.2f".format(change)}%"
    } ?: "N/A"
}