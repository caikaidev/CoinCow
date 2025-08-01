package com.kcode.gankotlin.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.kcode.gankotlin.data.local.dao.*
import com.kcode.gankotlin.data.local.entity.*

/**
 * Room database for caching cryptocurrency data
 */
@Database(
    entities = [
        CoinMarketDataEntity::class,
        CoinDetailsEntity::class,
        PriceHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {
    
    abstract fun coinMarketDataDao(): CoinMarketDataDao
    abstract fun coinDetailsDao(): CoinDetailsDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    
    companion object {
        const val DATABASE_NAME = "crypto_database"
        
        @Volatile
        private var INSTANCE: CryptoDatabase? = null
        
        fun getDatabase(context: Context): CryptoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CryptoDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}