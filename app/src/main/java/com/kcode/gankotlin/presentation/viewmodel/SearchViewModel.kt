package com.kcode.gankotlin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.SearchCoin
import com.kcode.gankotlin.domain.usecase.AddCoinToWatchlistUseCase
import com.kcode.gankotlin.domain.usecase.SearchCoinsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the search screen
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCoinsUseCase: SearchCoinsUseCase,
    private val addCoinToWatchlistUseCase: AddCoinToWatchlistUseCase
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _uiState = MutableStateFlow(SearchUiState.initial())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<SearchEvent>()
    val events: SharedFlow<SearchEvent> = _events.asSharedFlow()
    
    // Popular coins to show when search is empty
    private val popularCoins = listOf(
        "bitcoin", "ethereum", "binancecoin", "cardano", "solana",
        "polkadot", "dogecoin", "avalanche-2", "chainlink", "polygon"
    )
    
    init {
        // Set up search with debouncing
        _searchQuery
            .debounce(300) // Wait 300ms after user stops typing
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _uiState.value = SearchUiState.initial()
                } else {
                    searchCoins(query)
                }
            }
            .launchIn(viewModelScope)
        
        // Load popular coins initially
        loadPopularCoins()
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Search for coins
     */
    private fun searchCoins(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val params = SearchCoinsUseCase.Params(
                query = query,
                limit = 50
            )
            
            when (val result = searchCoinsUseCase(params)) {
                is NetworkResult.Success -> {
                    _uiState.value = SearchUiState.success(
                        searchResults = result.data,
                        query = query
                    )
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Search failed"
                    _uiState.value = SearchUiState.error(
                        error = errorMessage,
                        query = query
                    )
                    _events.emit(SearchEvent.Error(errorMessage))
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    /**
     * Load popular coins for initial display
     */
    private fun loadPopularCoins() {
        viewModelScope.launch {
            // For now, we'll show empty state until user searches
            // In a real implementation, we might fetch popular coins from the API
            _uiState.value = SearchUiState.initial()
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
                        _events.emit(SearchEvent.CoinAddedToWatchlist(coinId))
                    } else {
                        _events.emit(SearchEvent.CoinAlreadyInWatchlist(coinId))
                    }
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.message ?: "Failed to add coin to watchlist"
                    _events.emit(SearchEvent.Error(errorMessage))
                }
                is NetworkResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    /**
     * Clear search
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = SearchUiState.initial()
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for search screen
 */
data class SearchUiState(
    val searchResults: List<SearchCoin> = emptyList(),
    val popularCoins: List<SearchCoin> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val isEmpty: Boolean = true
) {
    companion object {
        fun initial() = SearchUiState()
        
        fun loading(query: String) = SearchUiState(
            isLoading = true,
            query = query,
            isEmpty = false
        )
        
        fun success(
            searchResults: List<SearchCoin>,
            query: String
        ) = SearchUiState(
            searchResults = searchResults,
            isLoading = false,
            error = null,
            query = query,
            isEmpty = searchResults.isEmpty()
        )
        
        fun error(
            error: String,
            query: String
        ) = SearchUiState(
            isLoading = false,
            error = error,
            query = query,
            isEmpty = true
        )
    }
}

/**
 * Events that can be emitted by the SearchViewModel
 */
sealed class SearchEvent {
    data class CoinAddedToWatchlist(val coinId: String) : SearchEvent()
    data class CoinAlreadyInWatchlist(val coinId: String) : SearchEvent()
    data class Error(val message: String) : SearchEvent()
}