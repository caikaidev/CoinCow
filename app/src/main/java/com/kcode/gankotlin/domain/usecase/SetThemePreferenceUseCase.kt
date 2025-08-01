package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Use case to set theme preference
 */
class SetThemePreferenceUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Set theme preference
     * @param theme "light", "dark", or "system"
     */
    suspend operator fun invoke(theme: String) {
        userPreferencesRepository.setThemePreference(theme)
    }
}