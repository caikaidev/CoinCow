package com.kcode.gankotlin.data.remote

import com.kcode.gankotlin.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {
    
    @GET("ping")
    suspend fun ping(): Map<String, String>
    
    /**
     * Get market data for cryptocurrencies
     */
    @GET("coins/markets")
    suspend fun getMarketData(
        @Query("vs_currency") currency: String = "usd",
        @Query("ids") ids: String? = null,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Query("price_change_percentage") priceChangePercentage: String? = null
    ): List<CoinMarketDataDto>
    
    /**
     * Get detailed information about a specific coin
     */
    @GET("coins/{id}")
    suspend fun getCoinDetails(
        @Path("id") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = true,
        @Query("developer_data") developerData: Boolean = true,
        @Query("sparkline") sparkline: Boolean = true
    ): CoinDetailsDto
    
    /**
     * Get historical price data for a coin
     */
    @GET("coins/{id}/market_chart")
    suspend fun getCoinPriceHistory(
        @Path("id") coinId: String,
        @Query("vs_currency") currency: String = "usd",
        @Query("days") days: String,
        @Query("interval") interval: String? = null
    ): CoinPriceHistoryDto
    
    /**
     * Search for coins
     */
    @GET("search")
    suspend fun searchCoins(
        @Query("query") query: String
    ): SearchCoinsResponseDto
}