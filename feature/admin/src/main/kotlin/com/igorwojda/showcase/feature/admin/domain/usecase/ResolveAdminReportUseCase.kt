package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Resolves or dismisses an admin moderation report. */
internal class ResolveAdminReportUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(
        reportId: String,
        decision: ReportDecision,
        note: String? = null,
    ): Result<AdminReport> = adminRepository.resolveReport(reportId = reportId, decision = decision, note = note)
}
