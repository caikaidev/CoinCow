package com.kcode.gankotlin.domain.model

/**
 * Domain model for search results
 */
data class SearchCoin(
    val id: String,
    val name: String,
    val symbol: String,
    val marketCapRank: Int?,
    val thumb: String,
    val large: String
)