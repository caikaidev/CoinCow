package com.kcode.gankotlin.di

import com.kcode.gankotlin.data.scheduler.DataUpdateScheduler
import com.kcode.gankotlin.data.sync.DataSyncManager
import com.kcode.gankotlin.data.realtime.RealTimeDataManager
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for worker and sync related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    // All dependencies are provided through constructor injection
    // DataUpdateScheduler, DataSyncManager, and RealTimeDataManager
    // are already annotated with @Singleton and @Inject
}