package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchPage
import com.uzuu.match.domain.repository.MatchRepository

internal class GetMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(): Result<MatchPage> = matchRepository.getMatches()
}
