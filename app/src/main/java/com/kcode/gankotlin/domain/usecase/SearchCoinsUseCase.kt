package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.data.remote.NetworkResult
import com.kcode.gankotlin.domain.model.SearchCoin
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.usecase.base.UseCase
import javax.inject.Inject

/**
 * Use case to search for coins by name or symbol
 */
class SearchCoinsUseCase @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : UseCase<SearchCoinsUseCase.Params, List<SearchCoin>>() {
    
    data class Params(
        val query: String,
        val limit: Int = 50
    )
    
    override suspend fun execute(parameters: Params): NetworkResult<List<SearchCoin>> {
        return try {
            val query = parameters.query.trim()
            
            if (query.isEmpty()) {
                return NetworkResult.Success(emptyList())
            }
            
            if (query.length < 2) {
                return NetworkResult.Error(
                    IllegalArgumentException("Search query must be at least 2 characters"),
                    "Search query too short"
                )
            }
            
            // Search for coins
            val result = cryptoRepository.searchCoins(query)
            
            when (result) {
                is NetworkResult.Success -> {
                    // Filter and limit results
                    val filteredResults = result.data
                        .filter { coin ->
                            // Filter by relevance - exact matches first, then partial matches
                            coin.name.contains(query, ignoreCase = true) ||
                            coin.symbol.contains(query, ignoreCase = true) ||
                            coin.id.contains(query, ignoreCase = true)
                        }
                        .sortedWith(compareBy<SearchCoin> { coin ->
                            // Sort by relevance: exact symbol match, exact name match, then partial matches
                            when {
                                coin.symbol.equals(query, ignoreCase = true) -> 0
                                coin.name.equals(query, ignoreCase = true) -> 1
                                coin.symbol.startsWith(query, ignoreCase = true) -> 2
                                coin.name.startsWith(query, ignoreCase = true) -> 3
                                else -> 4
                            }
                        }.thenBy { it.marketCapRank ?: Int.MAX_VALUE })
                        .take(parameters.limit)
                    
                    NetworkResult.Success(filteredResults)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        } catch (e: Exception) {
            NetworkResult.Error(e, "Failed to search coins")
        }
    }
}