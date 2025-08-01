package com.kcode.gankotlin.widget.data

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data provider for widget content
 */
@Singleton
class WidgetDataProvider @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Get widget coin data for display
     */
    suspend fun getWidgetData(): List<WidgetCoinData> {
        return try {
            // Get user's watchlist
            val watchlistIds = userPreferencesRepository.getWatchlist().first()
            
            if (watchlistIds.isEmpty()) {
                // If no watchlist, return top 3 coins
                getTopCoinsData()
            } else {
                // Get data for watchlist coins (max 5)
                getWatchlistData(watchlistIds.take(5))
            }
        } catch (e: Exception) {
            // Return cached data or empty list on error
            getCachedData()
        }
    }
    
    /**
     * Get top coins data when no watchlist is configured
     */
    private suspend fun getTopCoinsData(): List<WidgetCoinData> {
        return when (val result = cryptoRepository.getMarketData(forceRefresh = false)) {
            is NetworkResult.Success -> {
                result.data.take(3).map { coin ->
                    WidgetCoinData(
                        id = coin.id,
                        symbol = coin.symbol,
                        name = coin.name,
                        currentPrice = coin.currentPrice,
                        priceChangePercentage24h = coin.priceChangePercentage24h ?: 0.0,
                        imageUrl = coin.image
                    )
                }
            }
            else -> getCachedData()
        }
    }
    
    /**
     * Get watchlist coins data
     */
    private suspend fun getWatchlistData(coinIds: List<String>): List<WidgetCoinData> {
        return when (val result = cryptoRepository.getWatchlistMarketData(coinIds, forceRefresh = false)) {
            is NetworkResult.Success -> {
                result.data.map { coin ->
                    WidgetCoinData(
                        id = coin.id,
                        symbol = coin.symbol,
                        name = coin.name,
                        currentPrice = coin.currentPrice,
                        priceChangePercentage24h = coin.priceChangePercentage24h ?: 0.0,
                        imageUrl = coin.image
                    )
                }
            }
            else -> getCachedData()
        }
    }
    
    /**
     * Get cached data as fallback
     */
    private suspend fun getCachedData(): List<WidgetCoinData> {
        return try {
            // Try to get cached market data
            when (val result = cryptoRepository.getMarketData(forceRefresh = false)) {
                is NetworkResult.Success -> {
                    result.data.take(3).map { coin ->
                        WidgetCoinData(
                            id = coin.id,
                            symbol = coin.symbol,
                            name = coin.name,
                            currentPrice = coin.currentPrice,
                            priceChangePercentage24h = coin.priceChangePercentage24h ?: 0.0,
                            imageUrl = coin.image
                        )
                    }
                }
                else -> {
                    // Return sample data as last resort
                    listOf(
                        WidgetCoinData(
                            id = "bitcoin",
                            symbol = "BTC",
                            name = "Bitcoin",
                            currentPrice = 0.0,
                            priceChangePercentage24h = 0.0,
                            imageUrl = ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Check if widget data needs refresh
     */
    suspend fun shouldRefreshData(): Boolean {
        return try {
            // Check if cache is still valid (1 minute for widgets)
            !cryptoRepository.isCacheValid("market_data")
        } catch (e: Exception) {
            true // Refresh on error
        }
    }
}