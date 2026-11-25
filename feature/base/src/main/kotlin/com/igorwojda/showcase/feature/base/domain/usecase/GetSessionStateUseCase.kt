package com.igorwojda.showcase.feature.base.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

/** Observes the current reactive ArenaFlow session state. */
class GetSessionStateUseCase(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<ArenaSessionState> = sessionRepository.observeSession()
}
