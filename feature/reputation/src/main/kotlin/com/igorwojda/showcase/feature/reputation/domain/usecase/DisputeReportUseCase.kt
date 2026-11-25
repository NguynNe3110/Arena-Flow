package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.DisputeSubmission
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

/** Submits textual evidence to dispute a NOSHOW report within 24 hours. */
class DisputeReportUseCase(
    private val reputationRepository: ReputationRepository,
) {
    suspend operator fun invoke(
        reportId: String,
        evidence: String,
    ): Result<DisputeSubmission> =
        reputationRepository.disputeReport(
            reportId = reportId,
            evidence = evidence,
        )
}
