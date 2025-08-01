package com.kcode.gankotlin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.usecase.*
import com.kcode.gankotlin.presentation.viewmodel.state.WatchlistUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the watchlist screen
 */
@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val removeCoinFromWatchlistUseCase: RemoveCoinFromWatchlistUseCase,
    private val refreshMarketDataUseCase: RefreshMarketDataUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WatchlistUiState.initial())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<WatchlistEvent>()
    val events: SharedFlow<WatchlistEvent> = _events.asSharedFlow()
    
    /**
     * Load watchlist data
     */
    fun loadWatchlist(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (_uiState.value.watchlistCoins.isEmpty()) {
                _uiState.value = WatchlistUiState.loading()
            }
            
            val params = GetWatchlistUseCase.Params(
                forceRefresh = forceRefresh,
                currency = "usd"
            )
            
            when (val result = getWatchlistUseCase(params)) {
                is NetworkResult.Success -> {
                    _uiState.value = WatchlistUiState.success(
                        coins = result.data,
                        lastRefreshTime = System.currentTimeMillis()
                    )
                    
                    if (result.data.isEmpty()) {
                        _events.emit(WatchlistEvent.EmptyWatchlist)
                    }
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Failed to load watchlist"
                    _uiState.value = WatchlistUiState.error(
                        error = errorMessage,
                        currentCoins = _uiState.value.watchlistCoins
                    )
                    _events.emit(WatchlistEvent.Error(errorMessage))
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    /**
     * Refresh watchlist data
     */
    fun refreshWatchlist() {
        viewModelScope.launch {
            _uiState.value = WatchlistUiState.refreshing(_uiState.value.watchlistCoins)
            
            val params = RefreshMarketDataUseCase.Params(
                currency = "usd",
                refreshWatchlistOnly = true
            )
            
            when (val result = refreshMarketDataUseCase(params)) {
                is NetworkResult.Success -> {
                    _uiState.value = WatchlistUiState.success(
                        coins = result.data.watchlistData,
                        lastRefreshTime = result.data.lastRefreshTime
                    )
                    _events.emit(WatchlistEvent.RefreshSuccess)
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Failed to refresh watchlist"
                    _uiState.value = WatchlistUiState.error(
                        error = errorMessage,
                        currentCoins = _uiState.value.watchlistCoins
                    )
                    _events.emit(WatchlistEvent.Error(errorMessage))
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    /**
     * Remove coin from watchlist
     */
    fun removeCoinFromWatchlist(coinId: String) {
        viewModelScope.launch {
            val params = RemoveCoinFromWatchlistUseCase.Params(coinId = coinId)
            
            when (val result = removeCoinFromWatchlistUseCase(params)) {
                is NetworkResult.Success -> {
                    if (result.data) {
                        // Remove coin from current state
                        val updatedCoins = _uiState.value.watchlistCoins.filter { it.id != coinId }
                        _uiState.value = _uiState.value.copy(
                            watchlistCoins = updatedCoins,
                            isEmpty = updatedCoins.isEmpty()
                        )
                        _events.emit(WatchlistEvent.CoinRemoved(coinId))
                        
                        if (updatedCoins.isEmpty()) {
                            _events.emit(WatchlistEvent.EmptyWatchlist)
                        }
                    }
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Failed to remove coin from watchlist"
                    _events.emit(WatchlistEvent.Error(errorMessage))
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Retry loading watchlist
     */
    fun retry() {
        loadWatchlist(forceRefresh = true)
    }
}

/**
 * Events that can be emitted by the WatchlistViewModel
 */
sealed class WatchlistEvent {
    object EmptyWatchlist : WatchlistEvent()
    object RefreshSuccess : WatchlistEvent()
    data class CoinRemoved(val coinId: String) : WatchlistEvent()
    data class Error(val message: String) : WatchlistEvent()
}