package com.kcode.gankotlin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.usecase.AddCoinToWatchlistUseCase
import com.kcode.gankotlin.domain.usecase.GetCoinDetailsUseCase
import com.kcode.gankotlin.domain.usecase.GetCoinPriceHistoryUseCase
import com.kcode.gankotlin.domain.usecase.IsInWatchlistUseCase
import com.kcode.gankotlin.domain.usecase.RemoveCoinFromWatchlistUseCase
import com.kcode.gankotlin.presentation.component.ChartTimePeriod
import com.kcode.gankotlin.presentation.viewmodel.state.CoinDetailUiState
import com.kcode.gankotlin.presentation.viewmodel.state.PriceHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the coin detail screen
 */
@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val getCoinPriceHistoryUseCase: GetCoinPriceHistoryUseCase,
    private val addCoinToWatchlistUseCase: AddCoinToWatchlistUseCase,
    private val removeCoinFromWatchlistUseCase: RemoveCoinFromWatchlistUseCase,
    private val isInWatchlistUseCase: IsInWatchlistUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CoinDetailUiState>(CoinDetailUiState.Loading)
    val uiState: StateFlow<CoinDetailUiState> = _uiState.asStateFlow()
    
    private val _priceHistoryState = MutableStateFlow<PriceHistoryUiState>(PriceHistoryUiState.Loading)
    val priceHistoryState: StateFlow<PriceHistoryUiState> = _priceHistoryState.asStateFlow()
    
    private val _selectedChartPeriod = MutableStateFlow(ChartTimePeriod.SEVEN_DAYS)
    val selectedChartPeriod: StateFlow<ChartTimePeriod> = _selectedChartPeriod.asStateFlow()
    
    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist: StateFlow<Boolean> = _isInWatchlist.asStateFlow()
    
    private var currentCoinId: String = ""
    
    /**
     * Load coin details for the specified coin ID
     */
    fun loadCoinDetails(coinId: String, forceRefresh: Boolean = false) {
        if (coinId.isEmpty()) return
        
        currentCoinId = coinId
        
        viewModelScope.launch {
            _uiState.value = CoinDetailUiState.Loading
            
            // Load coin details
            val params = GetCoinDetailsUseCase.Params(
                coinId = coinId,
                forceRefresh = forceRefresh
            )
            
            when (val result = getCoinDetailsUseCase(params)) {
                is NetworkResult.Success -> {
                    _uiState.value = CoinDetailUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = CoinDetailUiState.Error(result.exception)
                }
                is NetworkResult.Loading -> {
                    _uiState.value = CoinDetailUiState.Loading
                }
            }
            
            // Check if coin is in watchlist
            checkWatchlistStatus(coinId)
        }
    }
    
    /**
     * Add coin to watchlist
     */
    fun addToWatchlist(coinId: String) {
        viewModelScope.launch {
            try {
                val params = AddCoinToWatchlistUseCase.Params(coinId)
                addCoinToWatchlistUseCase(params)
                _isInWatchlist.value = true
            } catch (e: Exception) {
                // Handle error silently for now
                // Could emit a snackbar event here
            }
        }
    }
    
    /**
     * Remove coin from watchlist
     */
    fun removeFromWatchlist(coinId: String) {
        viewModelScope.launch {
            try {
                val params = RemoveCoinFromWatchlistUseCase.Params(coinId)
                removeCoinFromWatchlistUseCase(params)
                _isInWatchlist.value = false
            } catch (e: Exception) {
                // Handle error silently for now
                // Could emit a snackbar event here
            }
        }
    }
    
    /**
     * Check if the current coin is in the watchlist
     */
    private suspend fun checkWatchlistStatus(coinId: String) {
        try {
            val params = IsInWatchlistUseCase.Params(coinId)
            val result = isInWatchlistUseCase.execute(params)
            _isInWatchlist.value = result
        } catch (e: Exception) {
            // Default to false if check fails
            _isInWatchlist.value = false
        }
    }
    
    /**
     * Load price history for the specified coin and time period
     */
    fun loadPriceHistory(coinId: String, period: ChartTimePeriod, forceRefresh: Boolean = false) {
        if (coinId.isEmpty()) return
        
        viewModelScope.launch {
            _priceHistoryState.value = PriceHistoryUiState.Loading
            _selectedChartPeriod.value = period
            
            val params = GetCoinPriceHistoryUseCase.Params(
                coinId = coinId,
                currency = "usd",
                days = period.apiValue,
                forceRefresh = forceRefresh
            )
            
            when (val result = getCoinPriceHistoryUseCase(params)) {
                is NetworkResult.Success -> {
                    _priceHistoryState.value = PriceHistoryUiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _priceHistoryState.value = PriceHistoryUiState.Error(result.exception)
                }
                is NetworkResult.Loading -> {
                    _priceHistoryState.value = PriceHistoryUiState.Loading
                }
            }
        }
    }
    
    /**
     * Refresh the current coin details and price history
     */
    fun refresh() {
        if (currentCoinId.isNotEmpty()) {
            loadCoinDetails(currentCoinId, forceRefresh = true)
            loadPriceHistory(currentCoinId, _selectedChartPeriod.value, forceRefresh = true)
        }
    }
}