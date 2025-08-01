package com.kcode.gankotlin.data.mapper

import com.kcode.gankotlin.data.remote.dto.*
import com.kcode.gankotlin.domain.model.*

/**
 * Mapper functions to convert DTOs to domain models
 */

fun CoinMarketDataDto.toDomainModel(): CoinMarketData {
    return CoinMarketData(
        id = id,
        symbol = symbol,
        name = name,
        image = image,
        currentPrice = currentPrice,
        marketCap = marketCap,
        marketCapRank = marketCapRank,
        fullyDilutedValuation = fullyDilutedValuation,
        totalVolume = totalVolume,
        high24h = high24h,
        low24h = low24h,
        priceChange24h = priceChange24h,
        priceChangePercentage24h = priceChangePercentage24h,
        marketCapChange24h = marketCapChange24h,
        marketCapChangePercentage24h = marketCapChangePercentage24h,
        circulatingSupply = circulatingSupply,
        totalSupply = totalSupply,
        maxSupply = maxSupply,
        ath = ath,
        athChangePercentage = athChangePercentage,
        athDate = athDate,
        atl = atl,
        atlChangePercentage = atlChangePercentage,
        atlDate = atlDate,
        lastUpdated = lastUpdated
    )
}

fun CoinDetailsDto.toDomainModel(): CoinDetails {
    return CoinDetails(
        id = id,
        symbol = symbol,
        name = name,
        description = description.en,
        image = image.toDomainModel(),
        marketData = marketData.toDomainModel(),
        communityData = communityData?.toDomainModel(),
        developerData = developerData?.toDomainModel(),
        publicInterestStats = publicInterestStats?.toDomainModel(),
        lastUpdated = lastUpdated
    )
}

fun CoinImageDto.toDomainModel(): CoinImage {
    return CoinImage(
        thumb = thumb,
        small = small,
        large = large
    )
}

fun CoinMarketDetailsDto.toDomainModel(): CoinMarketDetails {
    return CoinMarketDetails(
        currentPrice = currentPrice,
        marketCap = marketCap,
        totalVolume = totalVolume,
        high24h = high24h,
        low24h = low24h,
        priceChange24h = priceChange24h,
        priceChangePercentage24h = priceChangePercentage24h,
        priceChangePercentage7d = priceChangePercentage7d,
        priceChangePercentage14d = priceChangePercentage14d,
        priceChangePercentage30d = priceChangePercentage30d,
        priceChangePercentage60d = priceChangePercentage60d,
        priceChangePercentage200d = priceChangePercentage200d,
        priceChangePercentage1y = priceChangePercentage1y,
        marketCapChange24h = marketCapChange24h,
        marketCapChangePercentage24h = marketCapChangePercentage24h,
        totalSupply = totalSupply,
        maxSupply = maxSupply,
        circulatingSupply = circulatingSupply,
        sparkline7d = sparkline7d?.toDomainModel()
    )
}

fun SparklineDataDto.toDomainModel(): SparklineData {
    return SparklineData(price = price)
}

fun CoinCommunityDataDto.toDomainModel(): CoinCommunityData {
    return CoinCommunityData(
        facebookLikes = facebookLikes,
        twitterFollowers = twitterFollowers,
        redditAveragePosts48h = redditAveragePosts48h,
        redditAverageComments48h = redditAverageComments48h,
        redditSubscribers = redditSubscribers,
        redditAccountsActive48h = redditAccountsActive48h
    )
}

fun CoinDeveloperDataDto.toDomainModel(): CoinDeveloperData {
    return CoinDeveloperData(
        forks = forks,
        stars = stars,
        subscribers = subscribers,
        totalIssues = totalIssues,
        closedIssues = closedIssues,
        pullRequestsMerged = pullRequestsMerged,
        pullRequestContributors = pullRequestContributors,
        codeAdditionsDeletions4Weeks = codeAdditionsDeletions4Weeks?.toDomainModel(),
        commitCount4Weeks = commitCount4Weeks
    )
}

fun CodeChangesDto.toDomainModel(): CodeChanges {
    return CodeChanges(
        additions = additions,
        deletions = deletions
    )
}

fun CoinPublicInterestStatsDto.toDomainModel(): CoinPublicInterestStats {
    return CoinPublicInterestStats(
        alexaRank = alexaRank,
        bingMatches = bingMatches
    )
}

fun CoinPriceHistoryDto.toDomainModel(coinId: String, currency: String): CoinPriceHistory {
    val pricePoints = prices.mapNotNull { priceArray ->
        if (priceArray.size >= 2) {
            PricePoint(
                timestamp = priceArray[0].toLong(),
                price = priceArray[1]
            )
        } else null
    }
    
    val marketCapPoints = marketCaps?.mapNotNull { capArray ->
        if (capArray.size >= 2) {
            PricePoint(
                timestamp = capArray[0].toLong(),
                price = capArray[1]
            )
        } else null
    }
    
    val volumePoints = totalVolumes?.mapNotNull { volumeArray ->
        if (volumeArray.size >= 2) {
            PricePoint(
                timestamp = volumeArray[0].toLong(),
                price = volumeArray[1]
            )
        } else null
    }
    
    return CoinPriceHistory(
        coinId = coinId,
        currency = currency,
        prices = pricePoints,
        marketCaps = marketCapPoints,
        totalVolumes = volumePoints
    )
}

fun SearchCoinDto.toDomainModel(): SearchCoin {
    return SearchCoin(
        id = id,
        name = name,
        symbol = symbol,
        marketCapRank = marketCapRank,
        thumb = thumb,
        large = large
    )
}

