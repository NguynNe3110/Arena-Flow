package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.WithdrawRequestResult
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Withdraws a match join request with expectedStatus guard. */
class WithdrawMatchRequestUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        requestId: String,
        expectedStatus: String,
        reason: String? = null,
    ): Result<WithdrawRequestResult> =
        matchRepository.withdrawMatchRequest(
            requestId = requestId,
            expectedStatus = expectedStatus,
            reason = reason,
        )
}
