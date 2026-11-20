package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchDraft
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.repository.MatchRepository

internal class CreateMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(draft: MatchDraft): Result<MatchSummary> = matchRepository.createMatch(draft)
}
