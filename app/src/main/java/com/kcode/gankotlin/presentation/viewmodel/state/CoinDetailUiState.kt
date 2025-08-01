package com.kcode.gankotlin.presentation.viewmodel.state

import com.kcode.gankotlin.domain.model.CoinDetails

/**
 * UI state for the coin detail screen
 */
sealed class CoinDetailUiState {
    /**
     * Loading state while fetching coin details
     */
    object Loading : CoinDetailUiState()
    
    /**
     * Success state with coin details data
     */
    data class Success(
        val coinDetails: CoinDetails
    ) : CoinDetailUiState()
    
    /**
     * Error state when coin details loading fails
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error occurred"
    ) : CoinDetailUiState()
}