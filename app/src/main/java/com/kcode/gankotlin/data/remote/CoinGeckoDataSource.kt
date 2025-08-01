package com.kcode.gankotlin.data.remote

import com.kcode.gankotlin.data.mapper.toDomainModel
import com.kcode.gankotlin.domain.model.*
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CryptoDataSource using CoinGecko API
 */
@Singleton
class CoinGeckoDataSource @Inject constructor(
    private val api: CoinGeckoApi
) : CryptoDataSource {
    
    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
        private const val RATE_LIMIT_DELAY_MS = 60000L // 1 minute
    }
    
    override suspend fun getMarketData(
        currency: String,
        ids: List<String>?,
        page: Int,
        perPage: Int
    ): NetworkResult<List<CoinMarketData>> {
        return safeApiCall {
            val idsString = ids?.joinToString(",")
            val response = api.getMarketData(
                currency = currency,
                ids = idsString,
                page = page,
                perPage = perPage,
                sparkline = false,
                priceChangePercentage = "24h"
            )
            val domainData = response.map { it.toDomainModel() }
            // Log success for debugging
            println("CoinGecko API: Successfully fetched ${domainData.size} coins")
            domainData
        }
    }
    
    override suspend fun getCoinDetails(coinId: String): NetworkResult<CoinDetails> {
        return safeApiCall {
            val response = api.getCoinDetails(coinId)
            response.toDomainModel()
        }
    }
    
    override suspend fun getCoinPriceHistory(
        coinId: String,
        currency: String,
        days: String
    ): NetworkResult<CoinPriceHistory> {
        return safeApiCall {
            val response = api.getCoinPriceHistory(
                coinId = coinId,
                currency = currency,
                days = days
            )
            response.toDomainModel(coinId, currency)
        }
    }
    
    override suspend fun searchCoins(query: String): NetworkResult<List<SearchCoin>> {
        return safeApiCall {
            val response = api.searchCoins(query)
            response.coins.map { it.toDomainModel() }
        }
    }
    
    override suspend fun ping(): NetworkResult<Boolean> {
        return safeApiCall {
            api.ping()
            true
        }
    }
    
    /**
     * Safe API call wrapper with retry logic and error handling
     */
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        var lastException: Exception? = null
        
        for (attempt in 0 until MAX_RETRY_ATTEMPTS) {
            try {
                println("CoinGecko API: Attempting request (attempt ${attempt + 1}/$MAX_RETRY_ATTEMPTS)")
                val result = apiCall()
                println("CoinGecko API: Request successful")
                return NetworkResult.Success(result)
            } catch (e: Exception) {
                lastException = e
                println("CoinGecko API: Request failed - ${e.javaClass.simpleName}: ${e.message}")
                
                when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            429 -> {
                                // Rate limit exceeded
                                println("CoinGecko API: Rate limit exceeded (429)")
                                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                                    delay(RATE_LIMIT_DELAY_MS)
                                } else {
                                    return NetworkResult.Error(
                                        e,
                                        "API rate limit exceeded. Please try again later."
                                    )
                                }
                            }
                            in 500..599 -> {
                                // Server error - retry with exponential backoff
                                println("CoinGecko API: Server error (${e.code()})")
                                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                                    delay(RETRY_DELAY_MS * (attempt + 1))
                                } else {
                                    return NetworkResult.Error(
                                        e,
                                        "Server error. Please try again later."
                                    )
                                }
                            }
                            404 -> {
                                return NetworkResult.Error(
                                    e,
                                    "Requested data not found."
                                )
                            }
                            else -> {
                                return NetworkResult.Error(
                                    e,
                                    "HTTP error: ${e.code()}"
                                )
                            }
                        }
                    }
                    is SocketTimeoutException -> {
                        println("CoinGecko API: Request timeout")
                        if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                            delay(RETRY_DELAY_MS * (attempt + 1))
                        } else {
                            return NetworkResult.Error(
                                e,
                                "Request timeout. Please check your internet connection."
                            )
                        }
                    }
                    is IOException -> {
                        println("CoinGecko API: Network error")
                        if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                            delay(RETRY_DELAY_MS * (attempt + 1))
                        } else {
                            return NetworkResult.Error(
                                e,
                                "Network error. Please check your internet connection."
                            )
                        }
                    }
                    else -> {
                        return NetworkResult.Error(
                            e,
                            "An unexpected error occurred: ${e.message}"
                        )
                    }
                }
            }
        }
        
        return NetworkResult.Error(
            lastException ?: Exception("Unknown error"),
            "Failed after $MAX_RETRY_ATTEMPTS attempts"
        )
    }
}