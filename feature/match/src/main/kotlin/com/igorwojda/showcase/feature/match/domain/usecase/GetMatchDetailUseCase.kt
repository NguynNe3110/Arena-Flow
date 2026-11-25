package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Fetches full details for a single match by ID. */
class GetMatchDetailUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String): Result<MatchDetail> = matchRepository.getMatchDetail(matchId)
}
