package com.kcode.gankotlin.data.performance

import android.os.Debug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitor for app performance metrics
 */
@Singleton
class PerformanceMonitor @Inject constructor() {
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private val requestTimes = mutableMapOf<String, Long>()
    
    /**
     * Start timing a network request
     */
    fun startRequestTiming(requestId: String) {
        requestTimes[requestId] = System.currentTimeMillis()
    }
    
    /**
     * End timing a network request and record the duration
     */
    fun endRequestTiming(requestId: String) {
        val startTime = requestTimes.remove(requestId) ?: return
        val duration = System.currentTimeMillis() - startTime
        
        val currentMetrics = _performanceMetrics.value
        val updatedMetrics = currentMetrics.copy(
            networkRequestCount = currentMetrics.networkRequestCount + 1,
            averageRequestTime = calculateNewAverage(
                currentMetrics.averageRequestTime,
                currentMetrics.networkRequestCount,
                duration
            ),
            lastRequestTime = duration
        )
        
        _performanceMetrics.value = updatedMetrics
    }
    
    /**
     * Record memory usage
     */
    fun recordMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            memoryUsage = usedMemory,
            maxMemory = maxMemory,
            memoryUsagePercentage = (usedMemory.toFloat() / maxMemory.toFloat() * 100).toInt()
        )
    }
    
    /**
     * Record UI frame rendering time
     */
    fun recordFrameTime(frameTimeNanos: Long) {
        val frameTimeMs = frameTimeNanos / 1_000_000
        
        val currentMetrics = _performanceMetrics.value
        val updatedMetrics = currentMetrics.copy(
            frameCount = currentMetrics.frameCount + 1,
            averageFrameTime = calculateNewAverage(
                currentMetrics.averageFrameTime,
                currentMetrics.frameCount,
                frameTimeMs
            ),
            lastFrameTime = frameTimeMs
        )
        
        _performanceMetrics.value = updatedMetrics
    }
    
    /**
     * Record cache hit/miss
     */
    fun recordCacheHit(isHit: Boolean) {
        val currentMetrics = _performanceMetrics.value
        val updatedMetrics = if (isHit) {
            currentMetrics.copy(cacheHits = currentMetrics.cacheHits + 1)
        } else {
            currentMetrics.copy(cacheMisses = currentMetrics.cacheMisses + 1)
        }
        
        _performanceMetrics.value = updatedMetrics
    }
    
    /**
     * Get current performance summary
     */
    fun getPerformanceSummary(): PerformanceSummary {
        val metrics = _performanceMetrics.value
        val cacheHitRate = if (metrics.cacheHits + metrics.cacheMisses > 0) {
            (metrics.cacheHits.toFloat() / (metrics.cacheHits + metrics.cacheMisses).toFloat() * 100).toInt()
        } else {
            0
        }
        
        return PerformanceSummary(
            memoryUsagePercentage = metrics.memoryUsagePercentage,
            averageRequestTime = metrics.averageRequestTime,
            averageFrameTime = metrics.averageFrameTime,
            cacheHitRate = cacheHitRate,
            networkRequestCount = metrics.networkRequestCount,
            frameCount = metrics.frameCount,
            isPerformanceGood = isPerformanceGood(metrics)
        )
    }
    
    /**
     * Check if current performance is good
     */
    private fun isPerformanceGood(metrics: PerformanceMetrics): Boolean {
        return metrics.memoryUsagePercentage < 80 && // Memory usage under 80%
                metrics.averageRequestTime < 2000 && // Average request under 2 seconds
                metrics.averageFrameTime < 16 // Average frame time under 16ms (60 FPS)
    }
    
    /**
     * Calculate new average with incremental update
     */
    private fun calculateNewAverage(currentAverage: Long, count: Int, newValue: Long): Long {
        return if (count == 0) {
            newValue
        } else {
            (currentAverage * count + newValue) / (count + 1)
        }
    }
    
    /**
     * Reset all metrics
     */
    fun resetMetrics() {
        _performanceMetrics.value = PerformanceMetrics()
        requestTimes.clear()
    }
    
    /**
     * Get memory pressure level
     */
    fun getMemoryPressure(): MemoryPressure {
        val metrics = _performanceMetrics.value
        return when {
            metrics.memoryUsagePercentage > 90 -> MemoryPressure.CRITICAL
            metrics.memoryUsagePercentage > 75 -> MemoryPressure.HIGH
            metrics.memoryUsagePercentage > 50 -> MemoryPressure.MODERATE
            else -> MemoryPressure.LOW
        }
    }
}

data class PerformanceMetrics(
    val networkRequestCount: Int = 0,
    val averageRequestTime: Long = 0,
    val lastRequestTime: Long = 0,
    val memoryUsage: Long = 0,
    val maxMemory: Long = 0,
    val memoryUsagePercentage: Int = 0,
    val frameCount: Int = 0,
    val averageFrameTime: Long = 0,
    val lastFrameTime: Long = 0,
    val cacheHits: Int = 0,
    val cacheMisses: Int = 0
)

data class PerformanceSummary(
    val memoryUsagePercentage: Int,
    val averageRequestTime: Long,
    val averageFrameTime: Long,
    val cacheHitRate: Int,
    val networkRequestCount: Int,
    val frameCount: Int,
    val isPerformanceGood: Boolean
)

enum class MemoryPressure {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}