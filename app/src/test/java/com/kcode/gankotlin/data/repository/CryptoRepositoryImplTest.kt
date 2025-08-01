package com.kcode.gankotlin.data.repository

import com.kcode.gankotlin.data.local.CryptoDatabase
import com.kcode.gankotlin.data.local.dao.CoinMarketDataDao
import com.kcode.gankotlin.data.local.entity.CoinMarketDataEntity
import com.kcode.gankotlin.data.remote.CryptoDataSource
import com.kcode.gankotlin.data.remote.NetworkConnectivityManager
import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CryptoRepositoryImplTest {
    
    private lateinit var remoteDataSource: CryptoDataSource
    private lateinit var database: CryptoDatabase
    private lateinit var networkConnectivityManager: NetworkConnectivityManager
    private lateinit var moshi: Moshi
    private lateinit var coinMarketDataDao: CoinMarketDataDao
    private lateinit var repository: CryptoRepositoryImpl
    
    @Before
    fun setup() {
        remoteDataSource = mockk()
        database = mockk()
        networkConnectivityManager = mockk()
        moshi = mockk()
        coinMarketDataDao = mockk()
        
        every { database.coinMarketDataDao() } returns coinMarketDataDao
        
        repository = CryptoRepositoryImpl(
            remoteDataSource,
            database,
            networkConnectivityManager,
            moshi
        )
    }
    
    @Test
    fun `getMarketData with network available returns fresh data`() = runTest {
        // Given
        val mockNetworkData = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0)
        )
        val mockEntity = createMockCoinMarketDataEntity("bitcoin")
        
        every { networkConnectivityManager.isConnected() } returns true
        coEvery { remoteDataSource.getMarketData("usd") } returns NetworkResult.Success(mockNetworkData)
        coEvery { coinMarketDataDao.insertMarketData(any()) } returns Unit
        
        // When
        val result = repository.getMarketData("usd", forceRefresh = false)
        
        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(mockNetworkData, (result as NetworkResult.Success).data)
        coVerify { coinMarketDataDao.insertMarketData(any()) }
    }
    
    @Test
    fun `getMarketData with no network returns cached data`() = runTest {
        // Given
        val mockCachedEntities = listOf(
            createMockCoinMarketDataEntity("bitcoin")
        )
        
        every { networkConnectivityManager.isConnected() } returns false
        every { coinMarketDataDao.getAllMarketData() } returns flowOf(mockCachedEntities)
        
        // When
        val result = repository.getMarketData("usd", forceRefresh = false)
        
        // Then
        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(1, data.size)
        assertEquals("bitcoin", data[0].id)
    }
    
    @Test
    fun `getMarketData with no network and no cache returns error`() = runTest {
        // Given
        every { networkConnectivityManager.isConnected() } returns false
        every { coinMarketDataDao.getAllMarketData() } returns flowOf(emptyList())
        
        // When
        val result = repository.getMarketData("usd", forceRefresh = false)
        
        // Then
        assertTrue(result is NetworkResult.Error)
        assertTrue((result as NetworkResult.Error).message.contains("No internet connection"))
    }
    
    @Test
    fun `getMarketData with network error falls back to cache`() = runTest {
        // Given
        val mockCachedEntities = listOf(
            createMockCoinMarketDataEntity("bitcoin")
        )
        val networkError = NetworkResult.Error(Exception("Network failed"), "Network failed")
        
        every { networkConnectivityManager.isConnected() } returns true
        coEvery { remoteDataSource.getMarketData("usd") } returns networkError
        every { coinMarketDataDao.getAllMarketData() } returns flowOf(mockCachedEntities)
        
        // When
        val result = repository.getMarketData("usd", forceRefresh = false)
        
        // Then
        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals(1, data.size)
        assertEquals("bitcoin", data[0].id)
    }
    
    @Test
    fun `isCacheValid returns true for recent cache`() = runTest {
        // Given
        val recentTime = System.currentTimeMillis() - 30_000L // 30 seconds ago
        every { coinMarketDataDao.getLatestCacheTime() } returns recentTime
        
        // When
        val result = repository.isCacheValid("market_data")
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isCacheValid returns false for old cache`() = runTest {
        // Given
        val oldTime = System.currentTimeMillis() - 120_000L // 2 minutes ago
        every { coinMarketDataDao.getLatestCacheTime() } returns oldTime
        
        // When
        val result = repository.isCacheValid("market_data")
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isCacheValid returns false for null cache time`() = runTest {
        // Given
        every { coinMarketDataDao.getLatestCacheTime() } returns null
        
        // When
        val result = repository.isCacheValid("market_data")
        
        // Then
        assertFalse(result)
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
    
    private fun createMockCoinMarketDataEntity(id: String): CoinMarketDataEntity {
        return CoinMarketDataEntity(
            id = id,
            symbol = "BTC",
            name = "Bitcoin",
            image = "https://example.com/$id.png",
            currentPrice = 45000.0,
            marketCap = 850000000000L,
            marketCapRank = 1,
            fullyDilutedValuation = null,
            totalVolume = 25000000000L,
            high24h = 46000.0,
            low24h = 44000.0,
            priceChange24h = 1000.0,
            priceChangePercentage24h = 2.3,
            marketCapChange24h = 20000000000.0,
            marketCapChangePercentage24h = 2.4,
            circulatingSupply = 19500000.0,
            totalSupply = 21000000.0,
            maxSupply = 21000000.0,
            ath = 69000.0,
            athChangePercentage = -34.8,
            athDate = "2021-11-10T14:24:11.849Z",
            atl = 67.81,
            atlChangePercentage = 66281.1,
            atlDate = "2013-07-06T00:00:00.000Z",
            roi = null,
            lastUpdated = "2024-01-01T00:00:00.000Z",
            cachedAt = System.currentTimeMillis()
        )
    }
}