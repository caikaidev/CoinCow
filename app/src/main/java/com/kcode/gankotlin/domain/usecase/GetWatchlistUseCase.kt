package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import com.kcode.gankotlin.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to get watchlist coins with their market data
 */
class GetWatchlistUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : UseCase<GetWatchlistUseCase.Params, List<CoinMarketData>>() {
    
    data class Params(
        val forceRefresh: Boolean = false,
        val currency: String = "usd"
    )
    
    override suspend fun execute(parameters: Params): NetworkResult<List<CoinMarketData>> {
        return try {
            // Get user's watchlist
            val watchlist = userPreferencesRepository.getWatchlist().first()
            
            if (watchlist.isEmpty()) {
                return NetworkResult.Success(emptyList())
            }
            
            // Get market data for watchlist coins
            val result = cryptoRepository.getWatchlistMarketData(
                coinIds = watchlist,
                currency = parameters.currency,
                forceRefresh = parameters.forceRefresh
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    // Sort by watchlist order (maintain user's preferred order)
                    val sortedData = watchlist.mapNotNull { coinId ->
                        result.data.find { it.id == coinId }
                    }
                    NetworkResult.Success(sortedData)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Failed to get watchlist data")
        }
    }
}