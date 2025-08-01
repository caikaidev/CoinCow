package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinDetails
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.usecase.base.UseCase
import javax.inject.Inject

/**
 * Use case to get detailed information about a specific coin
 */
class GetCoinDetailsUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : UseCase<GetCoinDetailsUseCase.Params, CoinDetails>() {
    
    data class Params(
        val coinId: String,
        val forceRefresh: Boolean = false
    )
    
    override suspend fun execute(parameters: Params): NetworkResult<CoinDetails> {
        return try {
            val coinId = parameters.coinId.trim()
            
            if (coinId.isEmpty()) {
                return NetworkResult.Error(
                    IllegalArgumentException("Coin ID cannot be empty"),
                    "Invalid coin ID"
                )
            }
            
            // Get coin details from repository
            val result = cryptoRepository.getCoinDetails(
                coinId = coinId,
                forceRefresh = parameters.forceRefresh
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    // Validate the returned data
                    val coinDetails = result.data
                    if (coinDetails.id.isEmpty() || coinDetails.name.isEmpty()) {
                        NetworkResult.Error(
                            IllegalStateException("Invalid coin details received"),
                            "Coin details are incomplete"
                        )
                    } else {
                        result
                    }
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Failed to get coin details")
        }
    }
}