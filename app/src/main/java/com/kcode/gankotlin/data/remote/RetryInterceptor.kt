package com.kcode.gankotlin.data.remote

import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor for automatic retry with exponential backoff
 */
@Singleton
class RetryInterceptor @Inject constructor(
    private val networkConnectivityManager: NetworkConnectivityManager
) : Interceptor {
    
    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_DELAY_MS = 1000L
        private const val BACKOFF_MULTIPLIER = 2.0
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        for (attempt in 0..MAX_RETRIES) {
            try {
                // Check network connectivity before making request
                if (!networkConnectivityManager.isConnected()) {
                    throw UnknownHostException("No internet connection")
                }
                
                response?.close() // Close previous response if exists
                response = chain.proceed(request)
                
                // If successful or client error (4xx), don't retry
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }
                
                // Server errors (5xx) and rate limiting (429) should be retried
                if (response.code in 500..599 || response.code == 429) {
                    if (attempt < MAX_RETRIES) {
                        response.close()
                        val delay = calculateDelay(attempt, response.code)
                        Thread.sleep(delay)
                        continue
                    }
                }
                
                return response
                
            } catch (e: IOException) {
                exception = e
                
                // Don't retry on certain exceptions
                if (!shouldRetry(e) || attempt >= MAX_RETRIES) {
                    throw e
                }
                
                // Wait before retry
                val delay = calculateDelay(attempt)
                Thread.sleep(delay)
            }
        }
        
        // This should never be reached, but just in case
        throw exception ?: IOException("Max retries exceeded")
    }
    
    private fun shouldRetry(exception: IOException): Boolean {
        return when (exception) {
            is SocketTimeoutException -> true
            is UnknownHostException -> true
            else -> false
        }
    }
    
    private fun calculateDelay(attempt: Int, statusCode: Int? = null): Long {
        val baseDelay = when (statusCode) {
            429 -> 60_000L // 1 minute for rate limiting
            in 500..599 -> 5_000L // 5 seconds for server errors
            else -> INITIAL_DELAY_MS
        }
        
        return (baseDelay * Math.pow(BACKOFF_MULTIPLIER, attempt.toDouble())).toLong()
    }
}