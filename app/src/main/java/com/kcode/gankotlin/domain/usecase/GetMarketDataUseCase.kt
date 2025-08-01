package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.usecase.base.UseCase
import javax.inject.Inject

/**
 * Use case to get general market data for cryptocurrencies
 */
class GetMarketDataUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : UseCase<GetMarketDataUseCase.Params, List<CoinMarketData>>() {
    
    data class Params(
        val currency: String = "usd",
        val forceRefresh: Boolean = false
    )
    
    override suspend fun execute(parameters: Params): NetworkResult<List<CoinMarketData>> {
        return try {
            println("GetMarketDataUseCase: Starting execution with currency=${parameters.currency}, forceRefresh=${parameters.forceRefresh}")
            
            // Get market data from repository
            val result = cryptoRepository.getMarketData(
                currency = parameters.currency,
                forceRefresh = parameters.forceRefresh
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    println("GetMarketDataUseCase: Repository returned ${result.data.size} coins")
                    
                    // Filter out invalid data and sort by market cap rank
                    val validData = result.data
                        .filter { coin ->
                            coin.isValidPriceData() && 
                            coin.currentPrice > 0 &&
                            coin.name.isNotBlank() &&
                            coin.symbol.isNotBlank()
                        }
                        .sortedBy { it.marketCapRank ?: Int.MAX_VALUE }
                    
                    println("GetMarketDataUseCase: Filtered to ${validData.size} valid coins")
                    NetworkResult.Success(validData)
                }
                is NetworkResult.Error -> {
                    println("GetMarketDataUseCase: Repository returned error: ${result.message}")
                    result
                }
                is NetworkResult.Loading -> {
                    println("GetMarketDataUseCase: Repository returned loading state")
                    result
                }
            }
        } catch (e: Exception) {
            println("GetMarketDataUseCase: Exception occurred: ${e.message}")
            NetworkResult.Error(e, "Failed to get market data")
        }
    }
}