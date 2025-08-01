package com.kcode.gankotlin.di

import com.kcode.gankotlin.data.repository.CryptoRepositoryImpl
import com.kcode.gankotlin.data.repository.UserPreferencesRepositoryImpl
import com.kcode.gankotlin.domain.repository.CryptoRepository
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindCryptoRepository(
        cryptoRepositoryImpl: CryptoRepositoryImpl
    ): CryptoRepository
    
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}