package com.kcode.gankotlin.data.remote.interceptor

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor to handle API rate limiting
 * CoinGecko free tier allows 10-50 calls per minute
 */
@Singleton
class RateLimitInterceptor @Inject constructor() : Interceptor {
    
    companion object {
        private const val MIN_REQUEST_INTERVAL_MS = 1200L // ~50 requests per minute
    }
    
    private val lastRequestTime = AtomicLong(0)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRequest = currentTime - lastRequestTime.get()
        
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            val delayTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest
            runBlocking {
                delay(delayTime)
            }
        }
        
        lastRequestTime.set(System.currentTimeMillis())
        
        val response = chain.proceed(chain.request())
        
        // Handle rate limit response
        if (response.code == 429) {
            response.close()
            
            // Wait longer for rate limit and retry
            runBlocking {
                delay(60000) // Wait 1 minute
            }
            
            lastRequestTime.set(System.currentTimeMillis())
            return chain.proceed(chain.request())
        }
        
        return response
    }
}