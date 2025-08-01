package com.kcode.gankotlin.domain.usecase.base

import com.kcode.gankotlin.data.remote.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base class for use cases that execute business logic
 */
abstract class UseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    /**
     * Executes the use case asynchronously and returns a [NetworkResult]
     */
    suspend operator fun invoke(parameters: P): NetworkResult<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
    
    /**
     * Override this to set the code to be executed.
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): NetworkResult<R>
}

/**
 * Base class for use cases that don't require parameters
 */
abstract class NoParamsUseCase<R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    suspend operator fun invoke(): NetworkResult<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute()
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
    
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(): NetworkResult<R>
}

/**
 * Base class for use cases that return Flow
 */
abstract class FlowUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    
    suspend operator fun invoke(parameters: P) = withContext(coroutineDispatcher) {
        execute(parameters)
    }
    
    protected abstract suspend fun execute(parameters: P): kotlinx.coroutines.flow.Flow<R>
}