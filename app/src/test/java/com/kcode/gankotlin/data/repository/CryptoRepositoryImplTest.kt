package com.kcode.gankotlin.data.repository

import com.kcode.gankotlin.domain.model.CoinMarketData
import org.junit.Test
import org.junit.Assert.*

class CryptoRepositoryImplTest {
    
    @Test
    fun `createMockCoinMarketData creates valid domain model`() {
        // Given
        val id = "bitcoin"
        val name = "Bitcoin"
        val symbol = "BTC"
        val price = 45000.0
        
        // When
        val result = createMockCoinMarketData(id, name, symbol, price)
        
        // Then
        assertEquals(id, result.id)
        assertEquals(name, result.name)
        assertEquals(symbol, result.symbol)
        assertEquals(price, result.currentPrice, 0.0)
        assertTrue(result.isValidPriceData())
    }
    
    @Test
    fun `CoinMarketData formatting functions work correctly`() {
        // Given
        val coin = createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0)
        
        // When & Then
        assertTrue(coin.isPriceUp())
        assertEquals("+2.00%", coin.getFormattedPriceChangePercentage())
        assertTrue(coin.isValidPriceData())
    }
    
    @Test
    fun `CoinMarketData handles negative price change correctly`() {
        // Given
        val coin = CoinMarketData(
            id = "bitcoin",
            symbol = "BTC", 
            name = "Bitcoin",
            image = "https://example.com/bitcoin.png",
            currentPrice = 45000.0,
            marketCap = 850000000000.0,
            marketCapRank = 1,
            fullyDilutedValuation = null,
            totalVolume = 25000000000.0,
            high24h = 46000.0,
            low24h = 44000.0,
            priceChange24h = -1000.0,
            priceChangePercentage24h = -2.0,
            marketCapChange24h = -20000000000.0,
            marketCapChangePercentage24h = -2.0,
            circulatingSupply = 19500000.0,
            totalSupply = 21000000.0,
            maxSupply = 21000000.0,
            ath = 69000.0,
            athChangePercentage = -34.8,
            athDate = "2021-11-10T14:24:11.849Z",
            atl = 67.81,
            atlChangePercentage = 66281.1,
            atlDate = "2013-07-06T00:00:00.000Z",
            lastUpdated = "2024-01-01T00:00:00.000Z"
        )
        
        // When & Then
        assertFalse(coin.isPriceUp())
        assertEquals("-2.00%", coin.getFormattedPriceChangePercentage())
        assertTrue(coin.isValidPriceData())
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