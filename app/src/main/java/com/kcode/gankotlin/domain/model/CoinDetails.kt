package com.kcode.gankotlin.domain.model

/**
 * Detailed information about a specific cryptocurrency
 */
data class CoinDetails(
    val id: String,
    val symbol: String,
    val name: String,
    val description: String,
    val image: CoinImage,
    val marketData: CoinMarketDetails,
    val communityData: CoinCommunityData?,
    val developerData: CoinDeveloperData?,
    val publicInterestStats: CoinPublicInterestStats?,
    val lastUpdated: String
)

data class CoinImage(
    val thumb: String,
    val small: String,
    val large: String
)

data class CoinMarketDetails(
    val currentPrice: Map<String, Double>,
    val marketCap: Map<String, Double>,
    val totalVolume: Map<String, Double>,
    val high24h: Map<String, Double>,
    val low24h: Map<String, Double>,
    val priceChange24h: Double?,
    val priceChangePercentage24h: Double?,
    val priceChangePercentage7d: Double?,
    val priceChangePercentage14d: Double?,
    val priceChangePercentage30d: Double?,
    val priceChangePercentage60d: Double?,
    val priceChangePercentage200d: Double?,
    val priceChangePercentage1y: Double?,
    val marketCapChange24h: Double?,
    val marketCapChangePercentage24h: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val circulatingSupply: Double?,
    val sparkline7d: SparklineData?
)

data class SparklineData(
    val price: List<Double>
)

data class CoinCommunityData(
    val facebookLikes: Int?,
    val twitterFollowers: Int?,
    val redditAveragePosts48h: Double?,
    val redditAverageComments48h: Double?,
    val redditSubscribers: Int?,
    val redditAccountsActive48h: Int?
)

data class CoinDeveloperData(
    val forks: Int?,
    val stars: Int?,
    val subscribers: Int?,
    val totalIssues: Int?,
    val closedIssues: Int?,
    val pullRequestsMerged: Int?,
    val pullRequestContributors: Int?,
    val codeAdditionsDeletions4Weeks: CodeChanges?,
    val commitCount4Weeks: Int?
)

data class CodeChanges(
    val additions: Int?,
    val deletions: Int?
)

data class CoinPublicInterestStats(
    val alexaRank: Int?,
    val bingMatches: Int?
)