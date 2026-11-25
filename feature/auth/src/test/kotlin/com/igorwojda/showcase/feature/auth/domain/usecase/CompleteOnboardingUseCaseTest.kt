package com.igorwojda.showcase.feature.auth.domain.usecase

import com.igorwojda.showcase.feature.auth.domain.repository.AuthRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test

class CompleteOnboardingUseCaseTest {
    private val authRepository: AuthRepository = mockk()
    private val sut = CompleteOnboardingUseCase(authRepository)

    @Test
    fun `when invoked then delegates to repository completeOnboarding`() {
        every { authRepository.completeOnboarding() } just runs

        sut()

        verify(exactly = 1) { authRepository.completeOnboarding() }
    }
}
