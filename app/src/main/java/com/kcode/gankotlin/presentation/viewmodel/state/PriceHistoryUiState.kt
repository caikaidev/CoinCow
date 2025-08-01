package com.kcode.gankotlin.presentation.viewmodel.state

import com.kcode.gankotlin.domain.model.CoinPriceHistory

/**
 * UI state for price history data
 */
sealed class PriceHistoryUiState {
    /**
     * Loading state while fetching price history
     */
    object Loading : PriceHistoryUiState()
    
    /**
     * Success state with price history data
     */
    data class Success(
        val priceHistory: CoinPriceHistory
    ) : PriceHistoryUiState()
    
    /**
     * Error state when price history loading fails
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error occurred"
    ) : PriceHistoryUiState()
}