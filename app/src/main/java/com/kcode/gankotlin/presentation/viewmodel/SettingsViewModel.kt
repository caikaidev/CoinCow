package com.kcode.gankotlin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kcode.gankotlin.domain.usecase.GetThemePreferenceUseCase
import com.kcode.gankotlin.domain.usecase.SetThemePreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getThemePreferenceUseCase: GetThemePreferenceUseCase,
    private val setThemePreferenceUseCase: SetThemePreferenceUseCase
) : ViewModel() {
    
    private val _currentTheme = MutableStateFlow("system")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()
    
    init {
        // Load current theme preference
        viewModelScope.launch {
            getThemePreferenceUseCase().collect { theme ->
                _currentTheme.value = theme
            }
        }
    }
    
    /**
     * Set theme preference
     */
    fun setTheme(theme: String) {
        viewModelScope.launch {
            setThemePreferenceUseCase(theme)
        }
    }
}