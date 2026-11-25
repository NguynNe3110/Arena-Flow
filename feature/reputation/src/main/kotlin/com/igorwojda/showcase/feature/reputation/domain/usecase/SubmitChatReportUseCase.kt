package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

/** Submits a bad-communication report from a two-team chat conversation. */
class SubmitChatReportUseCase(
    private val reputationRepository: ReputationRepository,
) {
    suspend operator fun invoke(
        conversationId: String,
        reason: String,
    ): Result<TeamReport> =
        reputationRepository.submitChatReport(
            conversationId = conversationId,
            reason = reason,
        )
}
