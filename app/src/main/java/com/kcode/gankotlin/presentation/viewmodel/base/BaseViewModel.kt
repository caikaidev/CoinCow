package com.kcode.gankotlin.presentation.viewmodel.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common functionality
 */
abstract class BaseViewModel : ViewModel() {
    
    private val _errors = MutableSharedFlow<String>()
    val errors: SharedFlow<String> = _errors.asSharedFlow()
    
    private val _loading = MutableSharedFlow<Boolean>()
    val loading: SharedFlow<Boolean> = _loading.asSharedFlow()
    
    /**
     * Exception handler for coroutines
     */
    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleError(exception)
    }
    
    /**
     * Launch a coroutine with error handling
     */
    protected fun launchWithErrorHandling(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                setLoading(true)
                block()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Handle errors
     */
    protected open fun handleError(throwable: Throwable) {
        val errorMessage = when (throwable) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Request timeout"
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    429 -> "Too many requests. Please try again later."
                    500, 502, 503, 504 -> "Server error. Please try again later."
                    else -> "Network error: ${throwable.code()}"
                }
            }
            else -> throwable.message ?: "An unexpected error occurred"
        }
        
        viewModelScope.launch {
            _errors.emit(errorMessage)
        }
    }
    
    /**
     * Set loading state
     */
    protected fun setLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _loading.emit(isLoading)
        }
    }
    
    /**
     * Emit error message
     */
    protected fun emitError(message: String) {
        viewModelScope.launch {
            _errors.emit(message)
        }
    }
}