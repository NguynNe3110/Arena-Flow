package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.DisputeSubmission
import com.igorwojda.showcase.feature.reputation.domain.model.ReportStatus
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DisputeReportUseCaseTest {
    private val reputationRepository: ReputationRepository = mockk()
    private val sut = DisputeReportUseCase(reputationRepository)

    @Test
    fun `invoke submits dispute evidence`() =
        runTest {
            val expected =
                DisputeSubmission(
                    disputeId = "d1",
                    reportId = "r1",
                    status = "PENDING",
                    evidence = "Có mặt đúng giờ",
                    reportStatus = ReportStatus.DISPUTED,
                    createdAt = "2026-11-18T21:00:00",
                )
            coEvery { reputationRepository.disputeReport("r1", "Có mặt đúng giờ") } returns Result.Success(expected)

            val actual = sut("r1", "Có mặt đúng giờ")

            assertEquals(Result.Success(expected), actual)
        }
}
