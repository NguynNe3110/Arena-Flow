package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.CounterpartTeam
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.ReportStatus
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SubmitChatReportUseCaseTest {
    private val reputationRepository: ReputationRepository = mockk()
    private val sut = SubmitChatReportUseCase(reputationRepository)

    @Test
    fun `invoke submits chat report and returns created report`() =
        runTest {
            val expected =
                TeamReport(
                    reportId = "rep2",
                    matchId = "m1",
                    matchStartsAt = "2026-11-18T18:00:00",
                    direction = ReportDirection.SENT,
                    counterpartTeam = CounterpartTeam("t2", "FC Beta", null),
                    reportType = ReportType.BAD_COMMUNICATION,
                    reason = "Xúc phạm trong chat",
                    status = ReportStatus.AUTO_ACCEPTED,
                    disputeDeadlineAt = null,
                    canDispute = false,
                    reliabilityPenalty = null,
                    dispute = null,
                    createdAt = "2026-11-18T20:00:00",
                )
            coEvery {
                reputationRepository.submitChatReport("conv1", "Xúc phạm trong chat")
            } returns Result.Success(expected)

            val actual = sut("conv1", "Xúc phạm trong chat")

            assertEquals(Result.Success(expected), actual)
        }
}
