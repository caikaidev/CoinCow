package com.kcode.gankotlin.di

import android.content.Context
import androidx.room.Room
import com.kcode.gankotlin.data.local.CryptoDatabase
import com.kcode.gankotlin.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideCryptoDatabase(@ApplicationContext context: Context): CryptoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            CryptoDatabase::class.java,
            CryptoDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideCoinMarketDataDao(database: CryptoDatabase): CoinMarketDataDao {
        return database.coinMarketDataDao()
    }
    
    @Provides
    fun provideCoinDetailsDao(database: CryptoDatabase): CoinDetailsDao {
        return database.coinDetailsDao()
    }
    
    @Provides
    fun providePriceHistoryDao(database: CryptoDatabase): PriceHistoryDao {
        return database.priceHistoryDao()
    }
}