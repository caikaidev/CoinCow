package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Use case to check if a coin is in user's watchlist
 */
class IsInWatchlistUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    data class Params(
        val coinId: String
    )
    
    suspend fun execute(parameters: Params): Boolean {
        return try {
            val coinId = parameters.coinId.trim()
            
            if (coinId.isEmpty()) {
                return false
            }
            
            userPreferencesRepository.isInWatchlist(coinId)
        } catch (e: Exception) {
            false
        }
    }
}