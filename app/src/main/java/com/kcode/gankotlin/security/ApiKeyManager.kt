package com.kcode.gankotlin.security

import com.kcode.gankotlin.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure API key management
 */
@Singleton
class ApiKeyManager @Inject constructor() {
    
    /**
     * Get CoinGecko API key securely
     */
    fun getCoinGeckoApiKey(): String {
        // In production, this should be retrieved from a secure source
        // like Android Keystore or encrypted preferences
        return if (BuildConfig.COINGECKO_API_KEY.isNotEmpty()) {
            BuildConfig.COINGECKO_API_KEY
        } else {
            // Fallback for development/testing
            ""
        }
    }
    
    /**
     * Validate API key format
     */
    fun isValidApiKey(apiKey: String): Boolean {
        return apiKey.isNotEmpty() && 
               apiKey.length >= 32 && // Minimum expected length
               apiKey.matches(Regex("[a-zA-Z0-9-_]+")) // Expected format
    }
    
    /**
     * Obfuscate API key for logging
     */
    fun obfuscateApiKey(apiKey: String): String {
        return if (apiKey.length > 8) {
            "${apiKey.take(4)}****${apiKey.takeLast(4)}"
        } else {
            "****"
        }
    }
    
    /**
     * Check if running in debug mode
     */
    fun isDebugMode(): Boolean {
        return BuildConfig.DEBUG
    }
    
    /**
     * Check if logging is enabled
     */
    fun isLoggingEnabled(): Boolean {
        return BuildConfig.ENABLE_LOGGING
    }
}