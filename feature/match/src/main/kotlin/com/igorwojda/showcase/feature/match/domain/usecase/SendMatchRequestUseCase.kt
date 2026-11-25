package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Sends a join request to an open flow A match on behalf of the acting team. */
class SendMatchRequestUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        teamId: String,
        message: String? = null,
    ): Result<MatchRequest> = matchRepository.sendMatchRequest(matchId = matchId, teamId = teamId, message = message)
}
