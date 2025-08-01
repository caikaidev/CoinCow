package com.kcode.gankotlin.presentation.util

import com.kcode.gankotlin.data.remote.NetworkResult
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility object for handling errors and converting them to user-friendly messages
 */
object ErrorHandler {
    
    /**
     * Convert exception to user-friendly error message
     */
    fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is UnknownHostException -> "No internet connection. Please check your network and try again."
            is SocketTimeoutException -> "Request timeout. The server is taking too long to respond."
            is IOException -> "Network error. Please check your connection and try again."
            is HttpException -> {
                when (exception.code()) {
                    400 -> "Invalid request. Please check your input and try again."
                    401 -> "Authentication failed. Please check your API key."
                    403 -> "Access denied. You don't have permission to access this resource."
                    404 -> "The requested data was not found."
                    429 -> "Rate limit exceeded. Please wait a moment before trying again."
                    500 -> "Internal server error. Please try again later."
                    502 -> "Bad gateway. The server is temporarily unavailable."
                    503 -> "Service unavailable. Please try again later."
                    504 -> "Gateway timeout. The server is taking too long to respond."
                    in 500..599 -> "Server error (${exception.code()}). Please try again later."
                    else -> "Network error (${exception.code()}). Please try again."
                }
            }
            is IllegalArgumentException -> "Invalid data: ${exception.message}"
            is IllegalStateException -> "Application error: ${exception.message}"
            else -> exception.message ?: "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Convert NetworkResult.Error to user-friendly message
     */
    fun getErrorMessage(networkError: NetworkResult.Error<*>): String {
        return networkError.message ?: getErrorMessage(networkError.exception)
    }
    
    /**
     * Check if error is recoverable (user can retry)
     */
    fun isRecoverableError(exception: Exception): Boolean {
        return when (exception) {
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException -> true
            is HttpException -> {
                when (exception.code()) {
                    429, // Rate limit
                    in 500..599 -> true // Server errors
                    else -> false
                }
            }
            else -> false
        }
    }
    
    /**
     * Check if error suggests network connectivity issues
     */
    fun isNetworkError(exception: Exception): Boolean {
        return when (exception) {
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException -> true
            else -> false
        }
    }
    
    /**
     * Get retry delay based on error type
     */
    fun getRetryDelay(exception: Exception): Long {
        return when (exception) {
            is HttpException -> {
                when (exception.code()) {
                    429 -> 60_000L // 1 minute for rate limit
                    502, 503, 504 -> 10_000L // 10 seconds for gateway errors
                    in 500..599 -> 5_000L // 5 seconds for server errors
                    else -> 2_000L // 2 seconds for other HTTP errors
                }
            }
            is SocketTimeoutException -> 3_000L // 3 seconds for timeout
            is UnknownHostException -> 5_000L // 5 seconds for network issues
            else -> 2_000L // 2 seconds default
        }
    }
    
    /**
     * Get error severity level
     */
    fun getErrorSeverity(exception: Exception): ErrorSeverity {
        return when (exception) {
            is UnknownHostException -> ErrorSeverity.CRITICAL
            is HttpException -> {
                when (exception.code()) {
                    in 400..499 -> ErrorSeverity.WARNING
                    in 500..599 -> ErrorSeverity.ERROR
                    else -> ErrorSeverity.INFO
                }
            }
            is SocketTimeoutException -> ErrorSeverity.WARNING
            is IOException -> ErrorSeverity.ERROR
            else -> ErrorSeverity.INFO
        }
    }
    
    /**
     * Check if error should trigger offline mode
     */
    fun shouldTriggerOfflineMode(exception: Exception): Boolean {
        return when (exception) {
            is UnknownHostException -> true
            is SocketTimeoutException -> true
            is HttpException -> exception.code() in 500..599
            else -> false
        }
    }
}

enum class ErrorSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}