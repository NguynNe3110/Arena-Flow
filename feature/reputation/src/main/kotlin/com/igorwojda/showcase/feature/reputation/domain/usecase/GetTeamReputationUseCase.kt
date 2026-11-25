package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

/** Loads the reliability score, completion rate, rating summary, and verification badge of [teamId]. */
class GetTeamReputationUseCase(
    private val reputationRepository: ReputationRepository,
) {
    suspend operator fun invoke(teamId: String): Result<TeamReputation> = reputationRepository.getTeamReputation(teamId)
}
