package com.kcode.gankotlin.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager to prevent duplicate network requests
 */
@Singleton
class RequestDeduplicationManager @Inject constructor() {
    
    private val ongoingRequests = mutableMapOf<String, Any>()
    private val mutex = Mutex()
    
    /**
     * Execute request with deduplication
     * If the same request is already in progress, wait for its result
     */
    suspend fun <T> executeWithDeduplication(
        key: String,
        request: suspend () -> T
    ): T {
        mutex.withLock {
            val ongoingRequest = ongoingRequests[key]
            if (ongoingRequest != null) {
                @Suppress("UNCHECKED_CAST")
                return ongoingRequest as T
            }
        }
        
        return try {
            val result = request()
            mutex.withLock {
                ongoingRequests[key] = result as Any
            }
            result
        } finally {
            mutex.withLock {
                ongoingRequests.remove(key)
            }
        }
    }
    
    /**
     * Check if request is currently in progress
     */
    suspend fun isRequestInProgress(key: String): Boolean {
        return mutex.withLock {
            ongoingRequests.containsKey(key)
        }
    }
    
    /**
     * Cancel ongoing request
     */
    suspend fun cancelRequest(key: String) {
        mutex.withLock {
            ongoingRequests.remove(key)
        }
    }
    
    /**
     * Clear all ongoing requests
     */
    suspend fun clearAll() {
        mutex.withLock {
            ongoingRequests.clear()
        }
    }
    
    /**
     * Get statistics about ongoing requests
     */
    suspend fun getStats(): RequestStats {
        return mutex.withLock {
            RequestStats(
                ongoingRequestCount = ongoingRequests.size,
                requestKeys = ongoingRequests.keys.toList()
            )
        }
    }
}

data class RequestStats(
    val ongoingRequestCount: Int,
    val requestKeys: List<String>
)