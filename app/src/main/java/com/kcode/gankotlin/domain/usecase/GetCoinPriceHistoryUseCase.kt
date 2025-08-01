package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.data.validator.DataValidator
import com.kcode.gankotlin.domain.model.CoinPriceHistory
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.usecase.base.UseCase
import javax.inject.Inject

/**
 * Use case to get historical price data for a coin
 */
class GetCoinPriceHistoryUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : UseCase<GetCoinPriceHistoryUseCase.Params, CoinPriceHistory>() {
    
    data class Params(
        val coinId: String,
        val currency: String = "usd",
        val days: String = "7",
        val forceRefresh: Boolean = false
    )
    
    override suspend fun execute(parameters: Params): NetworkResult<CoinPriceHistory> {
        return try {
            val coinId = parameters.coinId.trim()
            
            if (coinId.isEmpty()) {
                return NetworkResult.Error(
                    IllegalArgumentException("Coin ID cannot be empty"),
                    "Invalid coin ID"
                )
            }
            
            // Validate days parameter
            val validDays = listOf("1", "7", "14", "30", "90", "180", "365", "max")
            if (!validDays.contains(parameters.days)) {
                return NetworkResult.Error(
                    IllegalArgumentException("Invalid days parameter: ${parameters.days}"),
                    "Invalid time period"
                )
            }
            
            // Get price history from repository
            val result = cryptoRepository.getCoinPriceHistory(
                coinId = coinId,
                currency = parameters.currency,
                days = parameters.days,
                forceRefresh = parameters.forceRefresh
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    val priceHistory = result.data
                    
                    // Validate and sanitize the price data
                    val validationResult = DataValidator.validatePriceHistory(priceHistory.prices)
                    if (validationResult is com.kcode.gankotlin.data.validator.ValidationResult.Error) {
                        // Try to sanitize the data
                        val sanitizedPrices = DataValidator.sanitizePriceHistory(priceHistory.prices)
                        if (sanitizedPrices.isEmpty()) {
                            return NetworkResult.Error(
                                IllegalStateException("No valid price data available"),
                                "Invalid price data received"
                            )
                        }
                        
                        // Return sanitized data
                        val sanitizedHistory = priceHistory.copy(prices = sanitizedPrices)
                        NetworkResult.Success(sanitizedHistory)
                    } else {
                        result
                    }
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Failed to get price history")
        }
    }
}