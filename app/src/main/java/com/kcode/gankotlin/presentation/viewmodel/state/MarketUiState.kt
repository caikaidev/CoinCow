package com.kcode.gankotlin.presentation.viewmodel.state

import com.kcode.gankotlin.domain.model.CoinMarketData

/**
 * UI state for the market screen
 */
data class MarketUiState(
    val marketData: List<CoinMarketData> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastRefreshTime: Long = 0L,
    val currency: String = "usd"
) {
    companion object {
        fun initial() = MarketUiState()
        
        fun loading() = MarketUiState(isLoading = true)
        
        fun refreshing(currentData: List<CoinMarketData>) = MarketUiState(
            marketData = currentData,
            isRefreshing = true
        )
        
        fun success(
            data: List<CoinMarketData>,
            currency: String = "usd",
            lastRefreshTime: Long = System.currentTimeMillis()
        ) = MarketUiState(
            marketData = data,
            isLoading = false,
            isRefreshing = false,
            error = null,
            lastRefreshTime = lastRefreshTime,
            currency = currency
        )
        
        fun error(
            error: String,
            currentData: List<CoinMarketData> = emptyList()
        ) = MarketUiState(
            marketData = currentData,
            isLoading = false,
            isRefreshing = false,
            error = error
        )
    }
}