package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Creates a new match post from a validated draft. */
class CreateMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(draft: MatchDraft): Result<MatchDetail> = matchRepository.createMatch(draft)
}
