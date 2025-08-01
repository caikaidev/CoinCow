package com.kcode.gankotlin.di

import android.content.Context
import coil.ImageLoader
import com.kcode.gankotlin.data.cache.ImageCacheManager
import com.kcode.gankotlin.data.performance.PerformanceMonitor
import com.kcode.gankotlin.data.recovery.CrashRecoveryManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Main application module for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        imageCacheManager: ImageCacheManager
    ): ImageLoader {
        return imageCacheManager.createOptimizedImageLoader()
    }
    
    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }
    
    @Provides
    @Singleton
    fun provideCrashRecoveryManager(
        @ApplicationContext context: Context,
        database: com.kcode.gankotlin.data.local.CryptoDatabase,
        userPreferencesRepository: com.kcode.gankotlin.domain.repository.UserPreferencesRepository
    ): CrashRecoveryManager {
        return CrashRecoveryManager(context, database, userPreferencesRepository)
    }
}