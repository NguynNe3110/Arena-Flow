package com.igorwojda.showcase.feature.auth.presentation.screen.onboarding

import com.igorwojda.showcase.feature.auth.domain.usecase.CompleteOnboardingUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel

/** UI state for the 3-slide onboarding flow (APP-02). */
data class OnboardingUiState(
    val currentSlide: Int = 0,
    val totalSlides: Int = TOTAL_SLIDES,
) : BaseState {
    val isLastSlide: Boolean get() = currentSlide >= totalSlides - 1

    private companion object {
        const val TOTAL_SLIDES = 3
    }
}

/** State transitions for [OnboardingViewModel]. */
sealed interface OnboardingAction : BaseAction<OnboardingUiState> {
    data class SelectSlide(
        val index: Int,
    ) : OnboardingAction {
        override fun reduce(state: OnboardingUiState): OnboardingUiState =
            state.copy(currentSlide = index.coerceIn(0, state.totalSlides - 1))
    }
}

/** ViewModel managing the APP-02 onboarding slides and completion state. */
class OnboardingViewModel(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : BaseViewModel<OnboardingUiState, OnboardingAction>(OnboardingUiState()) {
    fun selectSlide(index: Int) {
        sendAction(OnboardingAction.SelectSlide(index))
    }

    fun nextSlide(onCompleted: () -> Unit) {
        val current = uiStateFlow.value
        if (current.isLastSlide) {
            completeOnboarding(onCompleted)
        } else {
            sendAction(OnboardingAction.SelectSlide(current.currentSlide + 1))
        }
    }

    fun skip(onCompleted: () -> Unit) {
        completeOnboarding(onCompleted)
    }

    private fun completeOnboarding(onCompleted: () -> Unit) {
        completeOnboardingUseCase()
        onCompleted()
    }
}
