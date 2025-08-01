package com.kcode.gankotlin.domain.usecase

import com.kcode.gankotlin.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get theme preference
 */
class GetThemePreferenceUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    /**
     * Get theme preference flow
     * Returns: "light", "dark", or "system"
     */
    operator fun invoke(): Flow<String> {
        return userPreferencesRepository.getThemePreference()
    }
}