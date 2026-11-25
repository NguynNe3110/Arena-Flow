package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Rejects a pending match join request with an optional reason. */
class RejectMatchRequestUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        requestId: String,
        reason: String? = null,
    ): Result<Unit> = matchRepository.rejectMatchRequest(requestId = requestId, reason = reason)
}
