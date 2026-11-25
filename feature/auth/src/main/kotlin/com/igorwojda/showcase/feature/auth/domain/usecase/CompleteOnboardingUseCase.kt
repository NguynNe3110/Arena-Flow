package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository

/** Marks the onboarding flow as completed. */
class CompleteOnboardingUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke() = authRepository.completeOnboarding()
}
