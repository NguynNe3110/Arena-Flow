package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.CancelMatchResult
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Cancels a match with expectedStatus guard and optional/required reason. */
class CancelMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        teamId: String,
        expectedStatus: String,
        reason: String? = null,
    ): Result<CancelMatchResult> =
        matchRepository.cancelMatch(
            matchId = matchId,
            teamId = teamId,
            expectedStatus = expectedStatus,
            reason = reason,
        )
}
