package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResolveAdminReportUseCaseTest {
    private val adminRepository: AdminRepository = mockk()
    private val sut = ResolveAdminReportUseCase(adminRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            val report =
                AdminReport(
                    reportId = "rep-1",
                    reporterType = "TEAM",
                    reporterId = "t1",
                    reporterName = "FC A",
                    isReporterDeleted = false,
                    reportedId = "t2",
                    reportedName = "FC B",
                    isReportedDeleted = false,
                    matchId = null,
                    type = "NOSHOW",
                    reason = "Bùng",
                    status = AdminReportStatus.CONFIRMED,
                    createdAt = "2026-11-18T10:00:00",
                    canDisputeUntil = null,
                    disputeNote = null,
                    adminDecisionNote = "OK",
                )
            val expected = Result.Success(report)
            coEvery { adminRepository.resolveReport("rep-1", ReportDecision.CONFIRM, "OK") } returns expected

            val actual = sut("rep-1", ReportDecision.CONFIRM, "OK")

            assertEquals(expected, actual)
            coVerify { adminRepository.resolveReport("rep-1", ReportDecision.CONFIRM, "OK") }
        }
}
