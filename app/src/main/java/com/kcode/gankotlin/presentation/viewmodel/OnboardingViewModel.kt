package com.kcode.gankotlin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()
    
    init {
        viewModelScope.launch {
            userPreferencesRepository.isFirstLaunch().collect { isFirst ->
                _isFirstLaunch.value = isFirst
            }
        }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.markFirstLaunchCompleted()
        }
    }
    
    fun resetOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.resetFirstLaunchStatus()
        }
    }
}