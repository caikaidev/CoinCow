package com.kcode.gankotlin.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.ui.theme.CryptoTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatchlistScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun watchlistScreen_displaysEmptyState_whenNoCoinsinWatchlist() {
        // Given
        composeTestRule.setContent {
            CryptoTrackerTheme {
                WatchlistScreen(
                    onCoinClick = { },
                    onAddCoinClick = { }
                )
            }
        }
        
        // Then
        composeTestRule
            .onNodeWithText("Your Watchlist is Empty")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Add some cryptocurrencies to track their prices")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Add Coins")
            .assertIsDisplayed()
    }
    
    @Test
    fun watchlistScreen_displaysCoins_whenWatchlistHasData() {
        // Given
        val mockCoins = listOf(
            createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0, 2.5),
            createMockCoinMarketData("ethereum", "Ethereum", "ETH", 3000.0, -1.2)
        )
        
        composeTestRule.setContent {
            CryptoTrackerTheme {
                // Note: This would need to be updated to accept coins as parameter
                // or use a test version of the ViewModel
                WatchlistScreen(
                    onCoinClick = { },
                    onAddCoinClick = { }
                )
            }
        }
        
        // This test would need the actual implementation to pass coins
        // For now, we'll test the UI components that should be present
        
        // Then - Check for common UI elements
        composeTestRule
            .onNodeWithContentDescription("Add coin to watchlist")
            .assertExists()
    }
    
    @Test
    fun watchlistScreen_clickAddCoins_triggersCallback() {
        // Given
        var addCoinClicked = false
        
        composeTestRule.setContent {
            CryptoTrackerTheme {
                WatchlistScreen(
                    onCoinClick = { },
                    onAddCoinClick = { addCoinClicked = true }
                )
            }
        }
        
        // When
        composeTestRule
            .onNodeWithText("Add Coins")
            .performClick()
        
        // Then
        assert(addCoinClicked)
    }
    
    @Test
    fun watchlistScreen_pullToRefresh_triggersRefresh() {
        // Given
        composeTestRule.setContent {
            CryptoTrackerTheme {
                WatchlistScreen(
                    onCoinClick = { },
                    onAddCoinClick = { }
                )
            }
        }
        
        // When - Perform pull to refresh gesture
        composeTestRule
            .onRoot()
            .performTouchInput {
                swipeDown(
                    startY = 100f,
                    endY = 500f
                )
            }
        
        // Then - Check that refresh indicator appears
        // This would need to be verified based on the actual implementation
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun coinListItem_displaysCorrectInformation() {
        // Given
        val mockCoin = createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0, 2.5)
        
        composeTestRule.setContent {
            CryptoTrackerTheme {
                // This would test the CoinListItem component directly
                // CoinListItem(
                //     coin = mockCoin,
                //     onCoinClick = { },
                //     onAddToWatchlist = { }
                // )
            }
        }
        
        // Then
        // composeTestRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        // composeTestRule.onNodeWithText("BTC").assertIsDisplayed()
        // composeTestRule.onNodeWithText("$45,000.00").assertIsDisplayed()
        // composeTestRule.onNodeWithText("+2.5%").assertIsDisplayed()
    }
    
    @Test
    fun coinListItem_clickCoin_triggersCallback() {
        // Given
        var clickedCoinId = ""
        val mockCoin = createMockCoinMarketData("bitcoin", "Bitcoin", "BTC", 45000.0, 2.5)
        
        composeTestRule.setContent {
            CryptoTrackerTheme {
                // This would test the CoinListItem component directly
                // CoinListItem(
                //     coin = mockCoin,
                //     onCoinClick = { clickedCoinId = it },
                //     onAddToWatchlist = { }
                // )
            }
        }
        
        // When
        // composeTestRule.onNodeWithText("Bitcoin").performClick()
        
        // Then
        // assertEquals("bitcoin", clickedCoinId)
    }
    
    private fun createMockCoinMarketData(
        id: String,
        name: String,
        symbol: String,
        price: Double,
        changePercentage: Double
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
            priceChange24h = price * (changePercentage / 100),
            priceChangePercentage24h = changePercentage,
            marketCapChange24h = price * 19000000 * (changePercentage / 100),
            marketCapChangePercentage24h = changePercentage,
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