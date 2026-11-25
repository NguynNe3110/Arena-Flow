package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Forfeits a confirmed match on behalf of the acting team. */
class ForfeitMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        teamId: String,
        reason: String? = null,
    ): Result<Unit> = matchRepository.forfeitMatch(matchId = matchId, teamId = teamId, reason = reason)
}
