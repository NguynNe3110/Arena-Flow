package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Updates an existing open match post. */
class UpdateMatchUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        draft: MatchDraft,
    ): Result<MatchDetail> = matchRepository.updateMatch(matchId = matchId, draft = draft)
}
