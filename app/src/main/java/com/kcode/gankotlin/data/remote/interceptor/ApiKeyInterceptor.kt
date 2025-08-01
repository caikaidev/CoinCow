package com.kcode.gankotlin.data.remote.interceptor

import com.kcode.gankotlin.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor to add API key to requests if available
 */
@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add API key if available (CoinGecko free tier doesn't require API key)
        val apiKey = BuildConfig.COINGECKO_API_KEY
        if (apiKey.isNotBlank() && apiKey != "null") {
            val newRequest = originalRequest.newBuilder()
                .addHeader("x-cg-demo-api-key", apiKey)
                .build()
            return chain.proceed(newRequest)
        }
        
        // For free tier, proceed without API key
        return chain.proceed(originalRequest)
    }
}