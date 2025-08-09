package com.kcode.gankotlin.data.mapper

import com.kcode.gankotlin.data.local.entity.*
import com.kcode.gankotlin.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Mapper functions to convert between domain models and database entities
 */

fun CoinMarketData.toEntity(): CoinMarketDataEntity {
    return CoinMarketDataEntity(
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

fun CoinMarketDataEntity.toDomainModel(): CoinMarketData {
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

fun CoinDetails.toEntity(moshi: Moshi): CoinDetailsEntity {
    val sparklineJson = marketData.sparkline7d?.let { sparkline ->
        val adapter = moshi.adapter<List<Double>>(
            Types.newParameterizedType(List::class.java, Double::class.javaObjectType)
        )
        adapter.toJson(sparkline.price)
    }
    
    return CoinDetailsEntity(
        id = id,
        symbol = symbol,
        name = name,
        description = description,
        imageThumb = image.thumb,
        imageSmall = image.small,
        imageLarge = image.large,
        currentPriceUsd = marketData.currentPrice["usd"],
        marketCapUsd = marketData.marketCap["usd"],
        totalVolumeUsd = marketData.totalVolume["usd"],
        high24hUsd = marketData.high24h["usd"],
        low24hUsd = marketData.low24h["usd"],
        priceChange24h = marketData.priceChange24h,
        priceChangePercentage24h = marketData.priceChangePercentage24h,
        priceChangePercentage7d = marketData.priceChangePercentage7d,
        priceChangePercentage30d = marketData.priceChangePercentage30d,
        marketCapChange24h = marketData.marketCapChange24h,
        marketCapChangePercentage24h = marketData.marketCapChangePercentage24h,
        totalSupply = marketData.totalSupply,
        maxSupply = marketData.maxSupply,
        circulatingSupply = marketData.circulatingSupply,
        sparkline7d = sparklineJson,
        lastUpdated = lastUpdated
    )
}

fun CoinDetailsEntity.toDomainModel(moshi: Moshi): CoinDetails {
    val sparklineData = sparkline7d?.let { json ->
        val adapter = moshi.adapter<List<Double>>(
            Types.newParameterizedType(List::class.java, Double::class.javaObjectType)
        )
        val priceList = adapter.fromJson(json) ?: emptyList()
        SparklineData(priceList)
    }
    
    return CoinDetails(
        id = id,
        symbol = symbol,
        name = name,
        description = description,
        image = CoinImage(
            thumb = imageThumb,
            small = imageSmall,
            large = imageLarge
        ),
        marketData = CoinMarketDetails(
            currentPrice = mapOf("usd" to (currentPriceUsd ?: 0.0)),
            marketCap = mapOf("usd" to (marketCapUsd ?: 0.0)),
            totalVolume = mapOf("usd" to (totalVolumeUsd ?: 0.0)),
            high24h = mapOf("usd" to (high24hUsd ?: 0.0)),
            low24h = mapOf("usd" to (low24hUsd ?: 0.0)),
            priceChange24h = priceChange24h,
            priceChangePercentage24h = priceChangePercentage24h,
            priceChangePercentage7d = priceChangePercentage7d,
            priceChangePercentage14d = null,
            priceChangePercentage30d = priceChangePercentage30d,
            priceChangePercentage60d = null,
            priceChangePercentage200d = null,
            priceChangePercentage1y = null,
            marketCapChange24h = marketCapChange24h,
            marketCapChangePercentage24h = marketCapChangePercentage24h,
            totalSupply = totalSupply,
            maxSupply = maxSupply,
            circulatingSupply = circulatingSupply,
            sparkline7d = sparklineData
        ),
        communityData = null,
        developerData = null,
        publicInterestStats = null,
        lastUpdated = lastUpdated
    )
}

fun CoinPriceHistory.toEntity(moshi: Moshi): PriceHistoryEntity {
    val pricePointType = Types.newParameterizedType(List::class.java, PricePoint::class.java)
    val adapter = moshi.adapter<List<PricePoint>>(pricePointType)
    
    val pricesJson = adapter.toJson(prices)
    val marketCapsJson = marketCaps?.let { adapter.toJson(it) }
    val totalVolumesJson = totalVolumes?.let { adapter.toJson(it) }
    
    return PriceHistoryEntity(
        cacheKey = "${coinId}_${currency}_${getPeriodFromPrices()}",
        coinId = coinId,
        currency = currency,
        days = getPeriodFromPrices(),
        pricesJson = pricesJson,
        marketCapsJson = marketCapsJson,
        totalVolumesJson = totalVolumesJson
    )
}

fun PriceHistoryEntity.toDomainModel(moshi: Moshi): CoinPriceHistory {
    val pricePointType = Types.newParameterizedType(List::class.java, PricePoint::class.java)
    val adapter = moshi.adapter<List<PricePoint>>(pricePointType)
    
    val prices = adapter.fromJson(pricesJson) ?: emptyList()
    val marketCaps = marketCapsJson?.let { adapter.fromJson(it) }
    val totalVolumes = totalVolumesJson?.let { adapter.fromJson(it) }
    
    return CoinPriceHistory(
        coinId = coinId,
        currency = currency,
        prices = prices,
        marketCaps = marketCaps,
        totalVolumes = totalVolumes
    )
}

/**
 * Helper function to determine period from price data
 */
private fun CoinPriceHistory.getPeriodFromPrices(): String {
    if (prices.isEmpty()) return "1"
    
    val firstTimestamp = prices.first().timestamp
    val lastTimestamp = prices.last().timestamp
    val diffDays = (lastTimestamp - firstTimestamp) / (24 * 60 * 60 * 1000)
    
    return when {
        diffDays <= 1 -> "1"
        diffDays <= 7 -> "7"
        diffDays <= 30 -> "30"
        diffDays <= 90 -> "90"
        diffDays <= 365 -> "365"
        else -> "max"
    }
}