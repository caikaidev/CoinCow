package com.kcode.gankotlin.data.cache

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for image caching and lazy loading optimization
 */
@Singleton
class ImageCacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Create optimized ImageLoader for cryptocurrency images
     */
    fun createOptimizedImageLoader(): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB disk cache
                    .build()
            }
            .respectCacheHeaders(false) // Crypto images don't change often
            .build()
    }
    
    /**
     * Get cache policy for different image types
     */
    fun getCachePolicy(imageType: ImageType): CachePolicy {
        return when (imageType) {
            ImageType.COIN_ICON -> CachePolicy.ENABLED // Cache coin icons aggressively
            ImageType.CHART_THUMBNAIL -> CachePolicy.READ_ONLY // Charts change frequently
            ImageType.PROFILE_AVATAR -> CachePolicy.ENABLED
        }
    }
    
    /**
     * Clear image cache when memory is low
     */
    fun clearCache() {
        val imageLoader = createOptimizedImageLoader()
        imageLoader.memoryCache?.clear()
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        val imageLoader = createOptimizedImageLoader()
        val memoryCache = imageLoader.memoryCache
        val diskCache = imageLoader.diskCache
        
        return CacheStats(
            memoryCacheSize = memoryCache?.size ?: 0,
            memoryCacheMaxSize = memoryCache?.maxSize ?: 0,
            diskCacheSize = diskCache?.size ?: 0,
            diskCacheMaxSize = diskCache?.maxSize ?: 0
        )
    }
}

enum class ImageType {
    COIN_ICON,
    CHART_THUMBNAIL,
    PROFILE_AVATAR
}

data class CacheStats(
    val memoryCacheSize: Int,
    val memoryCacheMaxSize: Int,
    val diskCacheSize: Long,
    val diskCacheMaxSize: Long
)