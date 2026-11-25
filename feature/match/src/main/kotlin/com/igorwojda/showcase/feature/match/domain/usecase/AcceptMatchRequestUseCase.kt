package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.AcceptRequestResult
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Accepts a pending match join request, confirming the match. */
class AcceptMatchRequestUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(requestId: String): Result<AcceptRequestResult> = matchRepository.acceptMatchRequest(requestId)
}
