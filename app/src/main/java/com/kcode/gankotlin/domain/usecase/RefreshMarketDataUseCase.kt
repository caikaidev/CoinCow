package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import com.kcode.gankotlin.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to refresh market data for both general market and watchlist
 */
class RefreshMarketDataUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : UseCase<RefreshMarketDataUseCase.Params, RefreshMarketDataUseCase.RefreshResult>() {
    
    data class Params(
        val currency: String = "usd",
        val refreshWatchlistOnly: Boolean = false
    )
    
    data class RefreshResult(
        val marketData: List<CoinMarketData>,
        val watchlistData: List<CoinMarketData>,
        val lastRefreshTime: Long = System.currentTimeMillis()
    )
    
    override suspend fun execute(parameters: Params): NetworkResult<RefreshResult> {
        return try {
            val currency = parameters.currency
            val currentTime = System.currentTimeMillis()
            
            // Get user's watchlist
            val watchlist = userPreferencesRepository.getWatchlist().first()
            
            if (parameters.refreshWatchlistOnly && watchlist.isNotEmpty()) {
                // Only refresh watchlist data
                val watchlistResult = cryptoRepository.getWatchlistMarketData(
                    coinIds = watchlist,
                    currency = currency,
                    forceRefresh = true
                )
                
                when (watchlistResult) {
                    is NetworkResult.Success -> {
                        val sortedWatchlistData = watchlist.mapNotNull { coinId ->
                            watchlistResult.data.find { it.id == coinId }
                        }
                        
                        NetworkResult.Success(
                            RefreshResult(
                                marketData = emptyList(),
                                watchlistData = sortedWatchlistData,
                                lastRefreshTime = currentTime
                            )
                        )
                    }
                    is NetworkResult.Error -> NetworkResult.Error(watchlistResult.exception, watchlistResult.message)
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            } else {
                // Refresh general market data
                val marketDataResult = cryptoRepository.getMarketData(
                    currency = currency,
                    forceRefresh = true
                )
                
                when (marketDataResult) {
                    is NetworkResult.Success -> {
                        val marketData = marketDataResult.data
                        
                        // Extract watchlist data from market data if available
                        val watchlistData = if (watchlist.isNotEmpty()) {
                            watchlist.mapNotNull { coinId ->
                                marketData.find { it.id == coinId }
                            }
                        } else {
                            emptyList()
                        }
                        
                        NetworkResult.Success(
                            RefreshResult(
                                marketData = marketData,
                                watchlistData = watchlistData,
                                lastRefreshTime = currentTime
                            )
                        )
                    }
                    is NetworkResult.Error -> NetworkResult.Error(marketDataResult.exception, marketDataResult.message)
                    is NetworkResult.Loading -> NetworkResult.Loading()
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Failed to refresh market data")
        }
    }
}