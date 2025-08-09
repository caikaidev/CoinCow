package com.kcode.gankotlin.presentation.viewmodel

import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(userPreferencesRepository.isFirstLaunch()).thenReturn(flowOf(true))
        viewModel = OnboardingViewModel(userPreferencesRepository)
    }
    
    @Test
    fun `completeOnboarding should call markFirstLaunchCompleted`() = runTest {
        // When
        viewModel.completeOnboarding()
        
        // Then
        verify(userPreferencesRepository).markFirstLaunchCompleted()
    }
    
    @Test
    fun `resetOnboarding should call resetFirstLaunchStatus`() = runTest {
        // When
        viewModel.resetOnboarding()
        
        // Then
        verify(userPreferencesRepository).resetFirstLaunchStatus()
    }
}