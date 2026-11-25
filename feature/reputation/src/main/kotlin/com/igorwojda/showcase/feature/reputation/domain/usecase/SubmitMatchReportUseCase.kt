package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

/** Submits a post-match violation report against the opposing team. */
class SubmitMatchReportUseCase(
    private val reputationRepository: ReputationRepository,
) {
    suspend operator fun invoke(
        matchId: String,
        reportType: ReportType,
        reason: String,
    ): Result<TeamReport> =
        reputationRepository.submitMatchReport(
            matchId = matchId,
            reportType = reportType,
            reason = reason,
        )
}
