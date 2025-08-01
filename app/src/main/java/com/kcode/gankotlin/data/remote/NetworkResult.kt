package com.kcode.gankotlin.data.remote

/**
 * A generic wrapper class for network API responses
 */
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: Exception, val message: String? = null) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
}

/**
 * Extension function to handle NetworkResult responses
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (value: T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (exception: Exception) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(exception)
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: (isLoading: Boolean) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) action(isLoading)
    return this
}

/**
 * Maps the success data to another type
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(exception, message)
        is NetworkResult.Loading -> NetworkResult.Loading(isLoading)
    }
}

/**
 * Returns the data if success, null otherwise
 */
fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        else -> null
    }
}

/**
 * Returns the data if success, or the default value
 */
fun <T> NetworkResult<T>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is NetworkResult.Success -> data
        else -> defaultValue
    }
}