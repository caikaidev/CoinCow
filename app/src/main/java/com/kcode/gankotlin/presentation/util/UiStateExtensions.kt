package com.kcode.gankotlin.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Extension functions for UI state management
 */

/**
 * Collect state flow as state with initial loading
 */
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycle(): androidx.compose.runtime.State<T> {
    return collectAsState()
}

/**
 * Handle one-time events from SharedFlow
 */
@Composable
fun <T> Flow<T>.HandleEvents(
    onEvent: (T) -> Unit
) {
    LaunchedEffect(this) {
        collect { event ->
            onEvent(event)
        }
    }
}

/**
 * Data class for UI loading states
 */
data class LoadingState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val hasError: Boolean get() = error != null
    val isIdle: Boolean get() = !isLoading && !isRefreshing && error == null
    
    companion object {
        fun idle() = LoadingState()
        fun loading() = LoadingState(isLoading = true)
        fun refreshing() = LoadingState(isRefreshing = true)
        fun error(message: String) = LoadingState(error = message)
    }
}

/**
 * Generic UI state wrapper
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    object Idle : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isIdle: Boolean get() = this is Idle
    
    fun getDataOrNull(): T? = if (this is Success) data else null
    fun getErrorOrNull(): String? = if (this is Error) message else null
}

/**
 * Transform UiState
 */
inline fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> {
    return when (this) {
        is UiState.Success -> UiState.Success(transform(data))
        is UiState.Error -> this
        is UiState.Loading -> this
        is UiState.Idle -> this
    }
}

/**
 * Handle UiState with callbacks
 */
inline fun <T> UiState<T>.handle(
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (String) -> Unit = {},
    onIdle: () -> Unit = {}
) {
    when (this) {
        is UiState.Loading -> onLoading()
        is UiState.Success -> onSuccess(data)
        is UiState.Error -> onError(message)
        is UiState.Idle -> onIdle()
    }
}