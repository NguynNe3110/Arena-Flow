package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Fetches open matches matching the given discovery filter. */
class GetMatchesUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(filter: MatchFilter = MatchFilter()): Result<MatchPage> = matchRepository.getMatches(filter)
}
