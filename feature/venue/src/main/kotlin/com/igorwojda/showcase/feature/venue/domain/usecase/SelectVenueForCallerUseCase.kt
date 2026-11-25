package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository

class SelectVenueForCallerUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(result: SelectedVenueResult) {
        sessionRepository.setPendingVenueSelection(result)
    }
}
