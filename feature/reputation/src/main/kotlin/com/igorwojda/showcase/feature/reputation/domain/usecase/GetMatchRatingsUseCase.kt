package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRatings
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

/** Loads the given and received ratings for a match from the perspective of [teamId]. */
class GetMatchRatingsUseCase(
    private val reputationRepository: ReputationRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        teamId: String,
    ): Result<MatchRatings> = reputationRepository.getMatchRatings(matchId = matchId, teamId = teamId)
}
