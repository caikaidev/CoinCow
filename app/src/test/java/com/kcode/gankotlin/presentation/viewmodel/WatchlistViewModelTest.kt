package com.kcode.gankotlin.presentation.viewmodel

import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.presentation.viewmodel.state.WatchlistUiState
import org.junit.Test
import org.junit.Assert.*

class WatchlistViewModelTest {
    
    @Test
    fun `WatchlistUiState initial state works correctly`() {
        // When
        val initialState = WatchlistUiState.initial()
        
        // Then
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isRefreshing)
        assertTrue(initialState.watchlistCoins.isEmpty())
        assertNull(initialState.error)
    }
    
    @Test
    fun `WatchlistUiState loading state works correctly`() {
        // When
        val loadingState = WatchlistUiState.loading()
        
        // Then
        assertTrue(loadingState.isLoading)
        assertFalse(loadingState.isRefreshing)
        assertTrue(loadingState.watchlistCoins.isEmpty())
        assertNull(loadingState.error)
    }
    
    @Test
    fun `WatchlistUiState success state works correctly`() {
        // Given
        val coins = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0),
            createMockCoinMarketData("ethereum", "Ethereum", "ETH", 3000.0)
        )
        
        // When
        val successState = WatchlistUiState.success(coins)
        
        // Then
        assertFalse(successState.isLoading)
        assertFalse(successState.isRefreshing)
        assertEquals(2, successState.watchlistCoins.size)
        assertNull(successState.error)
        assertFalse(successState.isEmpty)
    }
    
    @Test
    fun `WatchlistUiState error state works correctly`() {
        // Given
        val errorMessage = "Network error"
        val existingCoins = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0)
        )
        
        // When
        val errorState = WatchlistUiState.error(errorMessage, existingCoins)
        
        // Then
        assertFalse(errorState.isLoading)
        assertFalse(errorState.isRefreshing)
        assertEquals(1, errorState.watchlistCoins.size)
        assertEquals(errorMessage, errorState.error)
        assertFalse(errorState.isEmpty)
    }
    
    @Test
    fun `WatchlistUiState refreshing state works correctly`() {
        // Given
        val existingCoins = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0)
        )
        
        // When
        val refreshingState = WatchlistUiState.refreshing(existingCoins)
        
        // Then
        assertFalse(refreshingState.isLoading)
        assertTrue(refreshingState.isRefreshing)
        assertEquals(1, refreshingState.watchlistCoins.size)
        assertNull(refreshingState.error)
    }
    
    private fun createMockCoinMarketData(
        id: String,
        name: String,
        symbol: String,
        price: Double
    ): CoinMarketData {
        return CoinMarketData(
            id = id,
            symbol = symbol,
            name = name,
            image = "https://example.com/$id.png",
            currentPrice = price,
            marketCap = price * 19000000,
            marketCapRank = 1,
            fullyDilutedValuation = null,
            totalVolume = price * 1000000,
            high24h = price * 1.05,
            low24h = price * 0.95,
            priceChange24h = price * 0.02,
            priceChangePercentage24h = 2.0,
            marketCapChange24h = price * 19000000 * 0.02,
            marketCapChangePercentage24h = 2.0,
            circulatingSupply = 19000000.0,
            totalSupply = 21000000.0,
            maxSupply = 21000000.0,
            ath = price * 2,
            athChangePercentage = -50.0,
            athDate = "2021-11-10T14:24:11.849Z",
            atl = price * 0.1,
            atlChangePercentage = 900.0,
            atlDate = "2013-07-06T00:00:00.000Z",
            lastUpdated = "2024-01-01T00:00:00.000Z"
        )
    }
}