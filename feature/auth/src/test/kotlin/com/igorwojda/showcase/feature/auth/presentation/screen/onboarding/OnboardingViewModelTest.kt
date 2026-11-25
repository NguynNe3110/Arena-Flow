package com.igorwojda.showcase.feature.auth.presentation.screen.onboarding

import com.igorwojda.showcase.feature.auth.domain.usecase.CompleteOnboardingUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class OnboardingViewModelTest {
    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mockk()
    private val sut = OnboardingViewModel(completeOnboardingUseCase)

    @Test
    fun `nextSlide advances until last slide then completes onboarding`() {
        every { completeOnboardingUseCase() } just runs
        var completed = false

        sut.nextSlide { completed = true }
        assertEquals(1, sut.uiStateFlow.value.currentSlide)

        sut.nextSlide { completed = true }
        assertEquals(2, sut.uiStateFlow.value.currentSlide)
        assertTrue(sut.uiStateFlow.value.isLastSlide)

        sut.nextSlide { completed = true }
        assertTrue(completed)
        verify(exactly = 1) { completeOnboardingUseCase() }
    }

    @Test
    fun `skip completes onboarding immediately`() {
        every { completeOnboardingUseCase() } just runs
        var completed = false

        sut.skip { completed = true }

        assertTrue(completed)
        verify(exactly = 1) { completeOnboardingUseCase() }
    }
}
