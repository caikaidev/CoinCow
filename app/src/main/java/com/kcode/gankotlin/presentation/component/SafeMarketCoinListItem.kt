package com.kcode.gankotlin.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kcode.gankotlin.domain.model.CoinMarketData

/**
 * Safe wrapper for MarketCoinListItem that handles null and invalid data
 */
@Composable
fun SafeMarketCoinListItem(
    coin: CoinMarketData?,
    onClick: () -> Unit,
    onAddToWatchlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        coin == null -> {
            // Show skeleton loading for null data
            SkeletonMarketCoinListItem(modifier = modifier)
        }
        !coin.isValidData() -> {
            // Show skeleton for invalid data
            SkeletonMarketCoinListItem(modifier = modifier)
        }
        else -> {
            // Show normal item with safe data
            val safeCoin = coin.toSafeData()
            MarketCoinListItem(
                coin = safeCoin,
                onClick = onClick,
                onAddToWatchlist = onAddToWatchlist,
                modifier = modifier
            )
        }
    }
}

/**
 * Extension functions for data validation and safety
 */
private fun CoinMarketData.isValidData(): Boolean {
    return id.isNotBlank() && 
           name.isNotBlank() && 
           symbol.isNotBlank() && 
           currentPrice > 0 &&
           !currentPrice.isNaN() && !currentPrice.isInfinite()
}

private fun CoinMarketData.toSafeData(): CoinMarketData {
    return copy(
        name = name.takeIf { it.isNotBlank() } ?: "Unknown Coin",
        symbol = symbol.takeIf { it.isNotBlank() } ?: "N/A",
        currentPrice = if (currentPrice > 0 && !currentPrice.isNaN() && !currentPrice.isInfinite()) currentPrice else 0.0,
        priceChangePercentage24h = priceChangePercentage24h?.takeIf { !it.isNaN() && !it.isInfinite() },
        marketCap = marketCap?.takeIf { it > 0 },
        marketCapRank = marketCapRank?.takeIf { it > 0 }
    )
}