package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import com.kcode.gankotlin.domain.usecase.base.UseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to add a coin to user's watchlist
 */
class AddCoinToWatchlistUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : UseCase<AddCoinToWatchlistUseCase.Params, Boolean>() {
    
    data class Params(
        val coinId: String
    )
    
    override suspend fun execute(parameters: Params): NetworkResult<Boolean> {
        return try {
            val coinId = parameters.coinId.trim()
            
            if (coinId.isEmpty()) {
                return NetworkResult.Error(
                    IllegalArgumentException("Coin ID cannot be empty"),
                    "Invalid coin ID"
                )
            }
            
            // Check if coin is already in watchlist
            val currentWatchlist = userPreferencesRepository.getWatchlist().first()
            if (currentWatchlist.contains(coinId)) {
                return NetworkResult.Success(false) // Already in watchlist
            }
            
            // Add to watchlist
            userPreferencesRepository.addToWatchlist(coinId)
            
            NetworkResult.Success(true)
        } catch (e: Exception) {
            NetworkResult.Error(e, "Failed to add coin to watchlist")
        }
    }
}