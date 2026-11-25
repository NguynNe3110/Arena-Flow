package com.igorwojda.showcase.feature.base.domain.usecase

import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository

/** Updates the active acting team for multi-team captains (TEAM-14). */
class UpdateActingTeamUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(teamId: String) {
        sessionRepository.setActingTeam(teamId)
    }
}
