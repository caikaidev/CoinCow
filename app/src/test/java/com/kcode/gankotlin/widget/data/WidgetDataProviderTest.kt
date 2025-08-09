package com.kcode.gankotlin.widget.data

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WidgetDataProviderTest {
    
    private lateinit var cryptoRepository: CryptoRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var widgetDataProvider: WidgetDataProvider
    
    @Before
    fun setup() {
        cryptoRepository = mockk()
        userPreferencesRepository = mockk()
        widgetDataProvider = WidgetDataProvider(cryptoRepository, userPreferencesRepository)
    }
    
    @Test
    fun `getWidgetData returns watchlist data when watchlist is not empty`() = runTest {
        // Given
        val watchlistIds = listOf("bitcoin", "ethereum")
        val mockMarketData = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0),
            createMockCoinMarketData("ethereum", "Ethereum", "ETH", 3000.0)
        )
        
        coEvery { userPreferencesRepository.getWatchlist() } returns flowOf(watchlistIds)
        coEvery { 
            cryptoRepository.getWatchlistMarketData(watchlistIds, "usd", false) 
        } returns NetworkResult.Success(mockMarketData)
        
        // When
        val result = widgetDataProvider.getWidgetData()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("bitcoin", result[0].id)
        assertEquals("ethereum", result[1].id)
        assertEquals("Bitcoin", result[0].name)
        assertEquals("Ethereum", result[1].name)
    }
    
    @Test
    fun `getWidgetData returns top coins when watchlist is empty`() = runTest {
        // Given
        val mockMarketData = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0),
            createMockCoinMarketData("ethereum", "Ethereum", "ETH", 3000.0),
            createMockCoinMarketData("binancecoin", "BNB", "BNB", 300.0)
        )
        
        coEvery { userPreferencesRepository.getWatchlist() } returns flowOf(emptyList())
        coEvery { 
            cryptoRepository.getMarketData("usd", false) 
        } returns NetworkResult.Success(mockMarketData)
        
        // When
        val result = widgetDataProvider.getWidgetData()
        
        // Then
        assertEquals(3, result.size)
        assertEquals("bitcoin", result[0].id)
        assertEquals("ethereum", result[1].id)
        assertEquals("binancecoin", result[2].id)
    }
    
    @Test
    fun `getWidgetData limits watchlist to 5 coins`() = runTest {
        // Given
        val watchlistIds = listOf("bitcoin", "ethereum", "binancecoin", "cardano", "solana", "polkadot", "chainlink")
        val mockMarketData = watchlistIds.map { id ->
            createMockCoinMarketData(id, id.replaceFirstChar { it.uppercase() }, id.take(3).uppercase(), 1000.0)
        }
        
        coEvery { userPreferencesRepository.getWatchlist() } returns flowOf(watchlistIds)
        coEvery { 
            cryptoRepository.getWatchlistMarketData(watchlistIds.take(5), "usd", false) 
        } returns NetworkResult.Success(mockMarketData.take(5))
        
        // When
        val result = widgetDataProvider.getWidgetData()
        
        // Then
        assertEquals(5, result.size)
    }
    
    @Test
    fun `getWidgetData returns cached data on network error`() = runTest {
        // Given
        val watchlistIds = listOf("bitcoin")
        val cachedData = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 44000.0)
        )
        
        coEvery { userPreferencesRepository.getWatchlist() } returns flowOf(watchlistIds)
        coEvery { 
            cryptoRepository.getWatchlistMarketData(watchlistIds, "usd", false) 
        } returns NetworkResult.Error(Exception("Network error"), "Network error")
        coEvery { 
            cryptoRepository.getMarketData("usd", false) 
        } returns NetworkResult.Success(cachedData)
        
        // When
        val result = widgetDataProvider.getWidgetData()
        
        // Then
        assertEquals(1, result.size)
        assertEquals("bitcoin", result[0].id)
        assertEquals(44000.0, result[0].currentPrice, 0.01)
    }
    
    @Test
    fun `getWidgetData returns empty list on complete failure`() = runTest {
        // Given
        coEvery { userPreferencesRepository.getWatchlist() } throws Exception("Preferences error")
        
        // When
        val result = widgetDataProvider.getWidgetData()
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `shouldRefreshData returns true when cache is invalid`() = runTest {
        // Given
        coEvery { cryptoRepository.isCacheValid("market_data") } returns false
        
        // When
        val result = widgetDataProvider.shouldRefreshData()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `shouldRefreshData returns false when cache is valid`() = runTest {
        // Given
        coEvery { cryptoRepository.isCacheValid("market_data") } returns true
        
        // When
        val result = widgetDataProvider.shouldRefreshData()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `shouldRefreshData returns true on exception`() = runTest {
        // Given
        coEvery { cryptoRepository.isCacheValid("market_data") } throws Exception("Cache check failed")
        
        // When
        val result = widgetDataProvider.shouldRefreshData()
        
        // Then
        assertTrue(result)
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