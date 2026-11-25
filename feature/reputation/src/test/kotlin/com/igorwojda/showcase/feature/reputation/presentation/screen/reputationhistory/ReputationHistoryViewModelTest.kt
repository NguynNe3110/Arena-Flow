package com.igorwojda.showcase.feature.reputation.presentation.screen.reputationhistory

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.CompletionStats
import com.igorwojda.showcase.feature.reputation.domain.model.RatingSummary
import com.igorwojda.showcase.feature.reputation.domain.model.ReliabilityLevel
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReportPage
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation
import com.igorwojda.showcase.feature.reputation.domain.model.VerificationBadge
import com.igorwojda.showcase.feature.reputation.domain.usecase.DisputeReportUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetTeamReportsUseCase
import com.igorwojda.showcase.feature.reputation.domain.usecase.GetTeamReputationUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class ReputationHistoryViewModelTest {
    private val disputeReportUseCase: DisputeReportUseCase = mockk()
    private val getTeamReportsUseCase: GetTeamReportsUseCase = mockk()
    private val getTeamReputationUseCase: GetTeamReputationUseCase = mockk()
    private val sut =
        ReputationHistoryViewModel(
            disputeReportUseCase = disputeReportUseCase,
            getTeamReportsUseCase = getTeamReportsUseCase,
            getTeamReputationUseCase = getTeamReputationUseCase,
        )

    @Test
    fun `load populates team reputation and reports`() =
        runTest {
            val reputation =
                TeamReputation(
                    teamId = "t1",
                    reliabilityScore = 95,
                    reliabilityLevel = ReliabilityLevel.EXCELLENT,
                    completion = CompletionStats(8, 8, 100, false),
                    rating = RatingSummary(4.9, 8),
                    verification = VerificationBadge(true, "2026-11-10T10:00:00"),
                )
            val page = TeamReportPage(items = emptyList(), page = 1, pageSize = 20, totalPages = 1, totalItems = 0)
            coEvery { getTeamReputationUseCase("t1") } returns Result.Success(reputation)
            coEvery { getTeamReportsUseCase("t1", ReportDirection.RECEIVED, 1, 20) } returns Result.Success(page)

            sut.onEvent(ReputationHistoryEvent.Load("t1"))
            advanceUntilIdle()

            assertEquals(reputation, sut.uiStateFlow.value.reputation)
            assertEquals(emptyList<Any>(), sut.uiStateFlow.value.reports)
            assertFalse(sut.uiStateFlow.value.isLoading)
        }
}
