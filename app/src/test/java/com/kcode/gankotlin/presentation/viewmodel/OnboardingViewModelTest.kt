package com.kcode.gankotlin.presentation.viewmodel

import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class OnboardingViewModelTest {
    
    @Mock
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    
    private lateinit var viewModel: OnboardingViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        `when`(userPreferencesRepository.isFirstLaunch()).thenReturn(flowOf(true))
        viewModel = OnboardingViewModel(userPreferencesRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `completeOnboarding should call markFirstLaunchCompleted`() = runTest(testDispatcher) {
        // When
        viewModel.completeOnboarding()
        
        // Advance the dispatcher to complete coroutine execution
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(userPreferencesRepository).markFirstLaunchCompleted()
    }
    
    @Test
    fun `resetOnboarding should call resetFirstLaunchStatus`() = runTest(testDispatcher) {
        // When
        viewModel.resetOnboarding()
        
        // Advance the dispatcher to complete coroutine execution
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(userPreferencesRepository).resetFirstLaunchStatus()
    }
}