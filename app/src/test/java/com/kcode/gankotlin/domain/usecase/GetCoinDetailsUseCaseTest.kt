package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.*
import com.kcode.gankotlin.domain.repository.CryptoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetCoinDetailsUseCaseTest {
    
    private lateinit var cryptoRepository: CryptoRepository
    private lateinit var getCoinDetailsUseCase: GetCoinDetailsUseCase
    
    @Before
    fun setup() {
        cryptoRepository = mockk()
        getCoinDetailsUseCase = GetCoinDetailsUseCase(cryptoRepository)
    }
    
    @Test
    fun `invoke with valid coin ID returns success`() = runTest {
        // Given
        val coinId = "bitcoin"
        val expectedCoinDetails = createMockCoinDetails(coinId)
        val params = GetCoinDetailsUseCase.Params(coinId)
        
        coEvery { 
            cryptoRepository.getCoinDetails(coinId, false) 
        } returns NetworkResult.Success(expectedCoinDetails)
        
        // When
        val result = getCoinDetailsUseCase(params)
        
        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedCoinDetails, (result as NetworkResult.Success).data)
    }
    
    @Test
    fun `invoke with empty coin ID returns error`() = runTest {
        // Given
        val params = GetCoinDetailsUseCase.Params("")
        
        // When
        val result = getCoinDetailsUseCase(params)
        
        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Invalid coin ID", (result as NetworkResult.Error).message)
    }
    
    @Test
    fun `invoke with blank coin ID returns error`() = runTest {
        // Given
        val params = GetCoinDetailsUseCase.Params("   ")
        
        // When
        val result = getCoinDetailsUseCase(params)
        
        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Invalid coin ID", (result as NetworkResult.Error).message)
    }
    
    @Test
    fun `invoke with repository error returns error`() = runTest {
        // Given
        val coinId = "bitcoin"
        val params = GetCoinDetailsUseCase.Params(coinId)
        val exception = Exception("Network error")
        
        coEvery { 
            cryptoRepository.getCoinDetails(coinId, false) 
        } returns NetworkResult.Error(exception, "Network error")
        
        // When
        val result = getCoinDetailsUseCase(params)
        
        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Network error", (result as NetworkResult.Error).message)
    }
    
    @Test
    fun `invoke with force refresh calls repository with correct parameter`() = runTest {
        // Given
        val coinId = "ethereum"
        val expectedCoinDetails = createMockCoinDetails(coinId)
        val params = GetCoinDetailsUseCase.Params(coinId, forceRefresh = true)
        
        coEvery { 
            cryptoRepository.getCoinDetails(coinId, true) 
        } returns NetworkResult.Success(expectedCoinDetails)
        
        // When
        val result = getCoinDetailsUseCase(params)
        
        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedCoinDetails, (result as NetworkResult.Success).data)
    }
    
    private fun createMockCoinDetails(coinId: String): CoinDetails {
        return CoinDetails(
            id = coinId,
            symbol = "BTC",
            name = "Bitcoin",
            description = "Bitcoin is a cryptocurrency",
            image = CoinImage(
                thumb = "https://example.com/thumb.png",
                small = "https://example.com/small.png",
                large = "https://example.com/large.png"
            ),
            marketData = CoinMarketDetails(
                currentPrice = mapOf("usd" to 45000.0),
                marketCap = mapOf("usd" to 850000000000.0),
                totalVolume = mapOf("usd" to 25000000000.0),
                high24h = mapOf("usd" to 46000.0),
                low24h = mapOf("usd" to 44000.0),
                priceChange24h = 1000.0,
                priceChangePercentage24h = 2.3,
                priceChangePercentage7d = 5.1,
                priceChangePercentage14d = -2.1,
                priceChangePercentage30d = 15.5,
                priceChangePercentage60d = 25.2,
                priceChangePercentage200d = 125.8,
                priceChangePercentage1y = 300.5,
                marketCapChange24h = 20000000000.0,
                marketCapChangePercentage24h = 2.4,
                totalSupply = 21000000.0,
                maxSupply = 21000000.0,
                circulatingSupply = 19500000.0,
                sparkline7d = null
            ),
            communityData = null,
            developerData = null,
            publicInterestStats = null,
            lastUpdated = "2024-01-01T00:00:00Z"
        )
    }
}