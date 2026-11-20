package com.uzuu.match.domain.usecase

import com.uzuu.match.domain.repository.MatchRepository

internal class GetMatchDetailUseCase(private val matchRepository: MatchRepository) {
    suspend operator fun invoke(matchId: String) = matchRepository.getMatch(matchId)
}
