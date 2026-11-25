package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.SubmitRatingParams
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

/** Submits a post-match rating (1-5 stars or opponent no-show). */
class SubmitMatchRatingUseCase(
    private val reputationRepository: ReputationRepository,
) {
    suspend operator fun invoke(params: SubmitRatingParams): Result<MatchRating> = reputationRepository.submitMatchRating(params)
}
