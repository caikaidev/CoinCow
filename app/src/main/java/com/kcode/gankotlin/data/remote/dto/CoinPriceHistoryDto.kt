package com.kcode.gankotlin.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO for CoinGecko price history API response
 * The API returns arrays of [timestamp, price] pairs
 */
@JsonClass(generateAdapter = true)
data class CoinPriceHistoryDto(
    @Json(name = "prices")
    val prices: List<List<Double>>,
    @Json(name = "market_caps")
    val marketCaps: List<List<Double>>?,
    @Json(name = "total_volumes")
    val totalVolumes: List<List<Double>>?
)

/**
 * DTO for search coins API response
 */
@JsonClass(generateAdapter = true)
data class SearchCoinsResponseDto(
    @Json(name = "coins")
    val coins: List<SearchCoinDto>
)

@JsonClass(generateAdapter = true)
data class SearchCoinDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "symbol")
    val symbol: String,
    @Json(name = "market_cap_rank")
    val marketCapRank: Int?,
    @Json(name = "thumb")
    val thumb: String,
    @Json(name = "large")
    val large: String
)