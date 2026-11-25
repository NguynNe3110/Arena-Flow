package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Fetches join requests sent to a match. */
class GetMatchRequestsUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        status: String? = null,
    ): Result<List<MatchRequest>> = matchRepository.getMatchRequests(matchId = matchId, status = status)
}
