package com.kcode.gankotlin.presentation.viewmodel.state

import com.kcode.gankotlin.domain.model.CoinMarketData

/**
 * UI state for the watchlist screen
 */
data class WatchlistUiState(
    val watchlistCoins: List<CoinMarketData> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastRefreshTime: Long = 0L,
    val isEmpty: Boolean = false
) {
    companion object {
        fun initial() = WatchlistUiState()
        
        fun loading() = WatchlistUiState(isLoading = true)
        
        fun refreshing(currentCoins: List<CoinMarketData>) = WatchlistUiState(
            watchlistCoins = currentCoins,
            isRefreshing = true
        )
        
        fun success(
            coins: List<CoinMarketData>,
            lastRefreshTime: Long = System.currentTimeMillis()
        ) = WatchlistUiState(
            watchlistCoins = coins,
            isLoading = false,
            isRefreshing = false,
            error = null,
            lastRefreshTime = lastRefreshTime,
            isEmpty = coins.isEmpty()
        )
        
        fun error(
            error: String,
            currentCoins: List<CoinMarketData> = emptyList()
        ) = WatchlistUiState(
            watchlistCoins = currentCoins,
            isLoading = false,
            isRefreshing = false,
            error = error,
            isEmpty = currentCoins.isEmpty()
        )
    }
}