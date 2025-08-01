package com.kcode.gankotlin.data.validator

import com.kcode.gankotlin.domain.model.CoinMarketData
import com.kcode.gankotlin.domain.model.PricePoint

/**
 * Utility class for validating cryptocurrency data integrity
 */
object DataValidator {
    
    /**
     * Validates cryptocurrency market data
     */
    fun validateMarketData(data: CoinMarketData): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate basic fields
        if (data.id.isBlank()) {
            errors.add("Coin ID cannot be empty")
        }
        
        if (data.symbol.isBlank()) {
            errors.add("Coin symbol cannot be empty")
        }
        
        if (data.name.isBlank()) {
            errors.add("Coin name cannot be empty")
        }
        
        // Validate price data
        if (data.currentPrice <= 0) {
            errors.add("Current price must be positive")
        }
        
        if (!data.currentPrice.isFinite()) {
            errors.add("Current price must be a finite number")
        }
        
        // Validate percentage changes (should be reasonable)
        data.priceChangePercentage24h?.let { percentage ->
            if (percentage < -100 || percentage > 10000) {
                errors.add("Price change percentage is unrealistic: $percentage%")
            }
        }
        
        // Validate market cap
        data.marketCap?.let { marketCap ->
            if (marketCap < 0) {
                errors.add("Market cap cannot be negative")
            }
        }
        
        // Validate volume
        data.totalVolume?.let { volume ->
            if (volume < 0) {
                errors.add("Total volume cannot be negative")
            }
        }
        
        // Validate supply data
        data.circulatingSupply?.let { circulating ->
            if (circulating < 0) {
                errors.add("Circulating supply cannot be negative")
            }
            
            data.totalSupply?.let { total ->
                if (circulating > total) {
                    errors.add("Circulating supply cannot exceed total supply")
                }
            }
            
            data.maxSupply?.let { max ->
                if (circulating > max) {
                    errors.add("Circulating supply cannot exceed max supply")
                }
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Validates price point data
     */
    fun validatePricePoint(pricePoint: PricePoint): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (pricePoint.timestamp <= 0) {
            errors.add("Timestamp must be positive")
        }
        
        if (pricePoint.price <= 0) {
            errors.add("Price must be positive")
        }
        
        if (!pricePoint.price.isFinite()) {
            errors.add("Price must be a finite number")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Validates a list of price points for consistency
     */
    fun validatePriceHistory(pricePoints: List<PricePoint>): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (pricePoints.isEmpty()) {
            errors.add("Price history cannot be empty")
            return ValidationResult.Error(errors)
        }
        
        // Validate individual points
        pricePoints.forEachIndexed { index, point ->
            val result = validatePricePoint(point)
            if (result is ValidationResult.Error) {
                errors.add("Price point at index $index: ${result.errors.joinToString()}")
            }
        }
        
        // Check for chronological order
        for (i in 1 until pricePoints.size) {
            if (pricePoints[i].timestamp < pricePoints[i - 1].timestamp) {
                errors.add("Price points are not in chronological order at index $i")
                break
            }
        }
        
        // Check for extreme price variations (more than 1000% change between consecutive points)
        for (i in 1 until pricePoints.size) {
            val prevPrice = pricePoints[i - 1].price
            val currentPrice = pricePoints[i].price
            val changePercentage = kotlin.math.abs((currentPrice - prevPrice) / prevPrice) * 100
            
            if (changePercentage > 1000) {
                errors.add("Extreme price change detected between points $i and ${i-1}: ${changePercentage}%")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Sanitizes price data by removing outliers and invalid values
     */
    fun sanitizePriceHistory(pricePoints: List<PricePoint>): List<PricePoint> {
        if (pricePoints.size < 3) return pricePoints.filter { it.isValid() }
        
        val validPoints = pricePoints.filter { it.isValid() }
        if (validPoints.size < 3) return validPoints
        
        // Calculate median price for outlier detection
        val sortedPrices = validPoints.map { it.price }.sorted()
        val median = sortedPrices[sortedPrices.size / 2]
        
        // Remove extreme outliers (more than 10x or less than 0.1x the median)
        return validPoints.filter { point ->
            val ratio = point.price / median
            ratio >= 0.1 && ratio <= 10.0
        }
    }
    
    /**
     * Validates market data for anomalies and data corruption
     */
    fun validateMarketDataAnomaly(data: CoinMarketData): Boolean {
        // Check for unrealistic price changes
        val priceChange = data.priceChangePercentage24h
        if (priceChange != null && kotlin.math.abs(priceChange) > 1000.0) {
            return false
        }
        
        // Check for negative values where they shouldn't be
        if (data.currentPrice < 0 || 
            (data.marketCap != null && data.marketCap < 0) ||
            (data.totalVolume != null && data.totalVolume < 0)) {
            return false
        }
        
        // Check for infinite or NaN values
        if (!data.currentPrice.isFinite()) {
            return false
        }
        
        return true
    }
    
    /**
     * Detects potential API response corruption
     */
    fun detectResponseCorruption(dataList: List<CoinMarketData>): Boolean {
        if (dataList.isEmpty()) return true
        
        // Check if too many coins have zero price (indicates API issue)
        val zeroPriceCount = dataList.count { it.currentPrice == 0.0 }
        if (zeroPriceCount > dataList.size * 0.1) {
            return true
        }
        
        // Check if all coins have the same price (indicates API corruption)
        val uniquePrices = dataList.map { it.currentPrice }.toSet()
        if (uniquePrices.size == 1 && dataList.size > 1) {
            return true
        }
        
        // Check for duplicate coin IDs
        val uniqueIds = dataList.map { it.id }.toSet()
        if (uniqueIds.size != dataList.size) {
            return true
        }
        
        return false
    }
}

/**
 * Sealed class representing validation results
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
}