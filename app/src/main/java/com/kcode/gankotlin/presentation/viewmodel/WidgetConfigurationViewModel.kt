package com.kcode.gankotlin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.usecase.GetMarketDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for widget configuration screen
 */
@HiltViewModel
class WidgetConfigurationViewModel @Inject constructor(
    private val getMarketDataUseCase: GetMarketDataUseCase
) : ViewModel() {
    
    private val _availableCoins = MutableStateFlow<List<CoinMarketData>>(emptyList())
    val availableCoins: StateFlow<List<CoinMarketData>> = _availableCoins.asStateFlow()
    
    private val _selectedCoins = MutableStateFlow<List<String>>(emptyList())
    val selectedCoins: StateFlow<List<String>> = _selectedCoins.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Load available coins for selection
     */
    fun loadAvailableCoins() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val params = GetMarketDataUseCase.Params(
                currency = "usd",
                forceRefresh = false
            )
            
            when (val result = getMarketDataUseCase(params)) {
                is NetworkResult.Success -> {
                    // Take top 20 coins for widget configuration
                    _availableCoins.value = result.data.take(20)
                }
                is NetworkResult.Error -> {
                    // Handle error - could show error state
                    _availableCoins.value = emptyList()
                }
                is NetworkResult.Loading -> {
                    // Keep loading state
                }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Select a coin for the widget
     */
    fun selectCoin(coinId: String) {
        val currentSelected = _selectedCoins.value.toMutableList()
        if (!currentSelected.contains(coinId) && currentSelected.size < 5) {
            currentSelected.add(coinId)
            _selectedCoins.value = currentSelected
        }
    }
    
    /**
     * Deselect a coin from the widget
     */
    fun deselectCoin(coinId: String) {
        val currentSelected = _selectedCoins.value.toMutableList()
        currentSelected.remove(coinId)
        _selectedCoins.value = currentSelected
    }
    
    /**
     * Clear all selected coins
     */
    fun clearSelection() {
        _selectedCoins.value = emptyList()
    }
}