package com.kcode.gankotlin.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CoinDetailsDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "symbol")
    val symbol: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "description")
    val description: DescriptionDto,
    @Json(name = "image")
    val image: CoinImageDto,
    @Json(name = "market_data")
    val marketData: CoinMarketDetailsDto,
    @Json(name = "community_data")
    val communityData: CoinCommunityDataDto?,
    @Json(name = "developer_data")
    val developerData: CoinDeveloperDataDto?,
    @Json(name = "public_interest_stats")
    val publicInterestStats: CoinPublicInterestStatsDto?,
    @Json(name = "last_updated")
    val lastUpdated: String
)

@JsonClass(generateAdapter = true)
data class DescriptionDto(
    @Json(name = "en")
    val en: String
)

@JsonClass(generateAdapter = true)
data class CoinImageDto(
    @Json(name = "thumb")
    val thumb: String,
    @Json(name = "small")
    val small: String,
    @Json(name = "large")
    val large: String
)

@JsonClass(generateAdapter = true)
data class CoinMarketDetailsDto(
    @Json(name = "current_price")
    val currentPrice: Map<String, Double>,
    @Json(name = "market_cap")
    val marketCap: Map<String, Double>,
    @Json(name = "total_volume")
    val totalVolume: Map<String, Double>,
    @Json(name = "high_24h")
    val high24h: Map<String, Double>,
    @Json(name = "low_24h")
    val low24h: Map<String, Double>,
    @Json(name = "price_change_24h")
    val priceChange24h: Double?,
    @Json(name = "price_change_percentage_24h")
    val priceChangePercentage24h: Double?,
    @Json(name = "price_change_percentage_7d")
    val priceChangePercentage7d: Double?,
    @Json(name = "price_change_percentage_14d")
    val priceChangePercentage14d: Double?,
    @Json(name = "price_change_percentage_30d")
    val priceChangePercentage30d: Double?,
    @Json(name = "price_change_percentage_60d")
    val priceChangePercentage60d: Double?,
    @Json(name = "price_change_percentage_200d")
    val priceChangePercentage200d: Double?,
    @Json(name = "price_change_percentage_1y")
    val priceChangePercentage1y: Double?,
    @Json(name = "market_cap_change_24h")
    val marketCapChange24h: Double?,
    @Json(name = "market_cap_change_percentage_24h")
    val marketCapChangePercentage24h: Double?,
    @Json(name = "total_supply")
    val totalSupply: Double?,
    @Json(name = "max_supply")
    val maxSupply: Double?,
    @Json(name = "circulating_supply")
    val circulatingSupply: Double?,
    @Json(name = "sparkline_7d")
    val sparkline7d: SparklineDataDto?
)

@JsonClass(generateAdapter = true)
data class SparklineDataDto(
    @Json(name = "price")
    val price: List<Double>
)

@JsonClass(generateAdapter = true)
data class CoinCommunityDataDto(
    @Json(name = "facebook_likes")
    val facebookLikes: Int?,
    @Json(name = "twitter_followers")
    val twitterFollowers: Int?,
    @Json(name = "reddit_average_posts_48h")
    val redditAveragePosts48h: Double?,
    @Json(name = "reddit_average_comments_48h")
    val redditAverageComments48h: Double?,
    @Json(name = "reddit_subscribers")
    val redditSubscribers: Int?,
    @Json(name = "reddit_accounts_active_48h")
    val redditAccountsActive48h: Int?
)

@JsonClass(generateAdapter = true)
data class CoinDeveloperDataDto(
    @Json(name = "forks")
    val forks: Int?,
    @Json(name = "stars")
    val stars: Int?,
    @Json(name = "subscribers")
    val subscribers: Int?,
    @Json(name = "total_issues")
    val totalIssues: Int?,
    @Json(name = "closed_issues")
    val closedIssues: Int?,
    @Json(name = "pull_requests_merged")
    val pullRequestsMerged: Int?,
    @Json(name = "pull_request_contributors")
    val pullRequestContributors: Int?,
    @Json(name = "code_additions_deletions_4_weeks")
    val codeAdditionsDeletions4Weeks: CodeChangesDto?,
    @Json(name = "commit_count_4_weeks")
    val commitCount4Weeks: Int?
)

@JsonClass(generateAdapter = true)
data class CodeChangesDto(
    @Json(name = "additions")
    val additions: Int?,
    @Json(name = "deletions")
    val deletions: Int?
)

@JsonClass(generateAdapter = true)
data class CoinPublicInterestStatsDto(
    @Json(name = "alexa_rank")
    val alexaRank: Int?,
    @Json(name = "bing_matches")
    val bingMatches: Int?
)