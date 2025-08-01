package com.kcode.gankotlin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.usecase.*
import com.kcode.gankotlin.presentation.viewmodel.state.MarketUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the market screen
 */
@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getMarketDataUseCase: GetMarketDataUseCase,
    private val addCoinToWatchlistUseCase: AddCoinToWatchlistUseCase,
    private val refreshMarketDataUseCase: RefreshMarketDataUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MarketUiState.initial())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<MarketEvent>()
    val events: SharedFlow<MarketEvent> = _events.asSharedFlow()
    
    /**
     * Load market data
     */
    fun loadMarketData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (_uiState.value.marketData.isEmpty()) {
                _uiState.value = MarketUiState.loading()
            }
            
            val params = GetMarketDataUseCase.Params(
                currency = _uiState.value.currency,
                forceRefresh = forceRefresh
            )
            
            try {
                when (val result = getMarketDataUseCase(params)) {
                    is NetworkResult.Success -> {
                        _uiState.value = MarketUiState.success(
                            data = result.data,
                            currency = _uiState.value.currency,
                            lastRefreshTime = System.currentTimeMillis()
                        )
                    }
                    is NetworkResult.Error -> {
                        val errorMessage = result.message ?: "Failed to load market data"
                        _uiState.value = MarketUiState.error(
                            error = errorMessage,
                            currentData = _uiState.value.marketData
                        )
                        _events.emit(MarketEvent.Error(errorMessage))
                    }
                    is NetworkResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Unexpected error: ${e.message}"
                _uiState.value = MarketUiState.error(
                    error = errorMessage,
                    currentData = _uiState.value.marketData
                )
                _events.emit(MarketEvent.Error(errorMessage))
            }
        }
    }
    
    /**
     * Refresh market data
     */
    fun refreshMarketData() {
        viewModelScope.launch {
            _uiState.value = MarketUiState.refreshing(_uiState.value.marketData)
            
            val params = RefreshMarketDataUseCase.Params(
                currency = _uiState.value.currency,
                refreshWatchlistOnly = false
            )
            
            when (val result = refreshMarketDataUseCase(params)) {
                is NetworkResult.Success -> {
                    _uiState.value = MarketUiState.success(
                        data = result.data.marketData,
                        currency = _uiState.value.currency,
                        lastRefreshTime = result.data.lastRefreshTime
                    )
                    _events.emit(MarketEvent.RefreshSuccess)
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Failed to refresh market data"
                    _uiState.value = MarketUiState.error(
                        error = errorMessage,
                        currentData = _uiState.value.marketData
                    )
                    _events.emit(MarketEvent.Error(errorMessage))
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    /**
     * Add coin to watchlist
     */
    fun addToWatchlist(coinId: String) {
        viewModelScope.launch {
            val params = AddCoinToWatchlistUseCase.Params(coinId = coinId)
            
            when (val result = addCoinToWatchlistUseCase(params)) {
                is NetworkResult.Success -> {
                    if (result.data) {
                        _events.emit(MarketEvent.CoinAddedToWatchlist(coinId))
                    } else {
                        _events.emit(MarketEvent.CoinAlreadyInWatchlist(coinId))
                    }
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Failed to add coin to watchlist"
                    _events.emit(MarketEvent.Error(errorMessage))
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    /**
     * Change currency
     */
    fun changeCurrency(currency: String) {
        if (currency != _uiState.value.currency) {
            _uiState.value = _uiState.value.copy(currency = currency)
            loadMarketData(forceRefresh = true)
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Retry loading market data
     */
    fun retry() {
        loadMarketData(forceRefresh = true)
    }
    
    /**
     * Get coin by ID from current market data
     */
    fun getCoinById(coinId: String) = _uiState.value.marketData.find { it.id == coinId }
    
    /**
     * Test API connectivity
     */
    fun testApiConnectivity() {
        viewModelScope.launch {
            try {
                // Try to get a small amount of data to test connectivity
                val params = GetMarketDataUseCase.Params(
                    currency = "usd",
                    forceRefresh = true
                )
                
                println("Testing API connectivity...")
                when (val result = getMarketDataUseCase(params)) {
                    is NetworkResult.Success -> {
                        println("API test successful: ${result.data.size} coins received")
                        _events.emit(MarketEvent.RefreshSuccess)
                    }
                    is NetworkResult.Error -> {
                        println("API test failed: ${result.message}")
                        _events.emit(MarketEvent.Error("API test failed: ${result.message}"))
                    }
                    is NetworkResult.Loading -> {
                        println("API test loading...")
                    }
                }
            } catch (e: Exception) {
                println("API test exception: ${e.message}")
                _events.emit(MarketEvent.Error("API test exception: ${e.message}"))
            }
        }
    }
}

/**
 * Events that can be emitted by the MarketViewModel
 */
sealed class MarketEvent {
    object RefreshSuccess : MarketEvent()
    data class CoinAddedToWatchlist(val coinId: String) : MarketEvent()
    data class CoinAlreadyInWatchlist(val coinId: String) : MarketEvent()
    data class Error(val message: String) : MarketEvent()
}