package com.kcode.gankotlin.presentation.viewmodel

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.usecase.GetWatchlistUseCase
import com.kcode.gankotlin.domain.usecase.RefreshMarketDataUseCase
import com.kcode.gankotlin.presentation.viewmodel.state.WatchlistUiState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {
    
    private lateinit var getWatchlistUseCase: GetWatchlistUseCase
    private lateinit var refreshMarketDataUseCase: RefreshMarketDataUseCase
    private lateinit var viewModel: WatchlistViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getWatchlistUseCase = mockk()
        refreshMarketDataUseCase = mockk()
        
        viewModel = WatchlistViewModel(
            getWatchlistUseCase,
            refreshMarketDataUseCase
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is loading`() {
        // Then
        assertTrue(viewModel.uiState.value is WatchlistUiState.Loading)
    }
    
    @Test
    fun `loadWatchlist with successful data updates state to success`() = runTest {
        // Given
        val mockCoins = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0),
            createMockCoinMarketData("ethereum", "Ethereum", "ETH", 3000.0)
        )
        
        coEvery { getWatchlistUseCase() } returns flowOf(mockCoins)
        
        // When
        viewModel.loadWatchlist()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WatchlistUiState.Success)
        assertEquals(mockCoins, (state as WatchlistUiState.Success).coins)
        assertFalse(state.isRefreshing)
    }
    
    @Test
    fun `loadWatchlist with empty data updates state to empty`() = runTest {
        // Given
        coEvery { getWatchlistUseCase() } returns flowOf(emptyList())
        
        // When
        viewModel.loadWatchlist()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WatchlistUiState.Empty)
    }
    
    @Test
    fun `loadWatchlist with exception updates state to error`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { getWatchlistUseCase() } throws exception
        
        // When
        viewModel.loadWatchlist()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WatchlistUiState.Error)
        assertEquals(exception, (state as WatchlistUiState.Error).exception)
    }
    
    @Test
    fun `refresh with successful result maintains success state with refreshing flag`() = runTest {
        // Given
        val mockCoins = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0)
        )
        
        coEvery { getWatchlistUseCase() } returns flowOf(mockCoins)
        coEvery { refreshMarketDataUseCase() } returns NetworkResult.Success(Unit)
        
        // When
        viewModel.loadWatchlist()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.refresh()
        
        // Check intermediate refreshing state
        val refreshingState = viewModel.uiState.value
        assertTrue(refreshingState is WatchlistUiState.Success)
        assertTrue((refreshingState as WatchlistUiState.Success).isRefreshing)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val finalState = viewModel.uiState.value
        assertTrue(finalState is WatchlistUiState.Success)
        assertFalse((finalState as WatchlistUiState.Success).isRefreshing)
    }
    
    @Test
    fun `refresh with error maintains current state and stops refreshing`() = runTest {
        // Given
        val mockCoins = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0)
        )
        
        coEvery { getWatchlistUseCase() } returns flowOf(mockCoins)
        coEvery { refreshMarketDataUseCase() } returns NetworkResult.Error(
            Exception("Refresh failed"), 
            "Refresh failed"
        )
        
        // When
        viewModel.loadWatchlist()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is WatchlistUiState.Success)
        assertFalse((state as WatchlistUiState.Success).isRefreshing)
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
            roi = null,
            lastUpdated = "2024-01-01T00:00:00.000Z"
        )
    }
}