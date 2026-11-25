package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReportPage
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetTeamReportsUseCaseTest {
    private val reputationRepository: ReputationRepository = mockk()
    private val sut = GetTeamReportsUseCase(reputationRepository)

    @Test
    fun `invoke returns paginated team reports`() =
        runTest {
            val expected = TeamReportPage(items = emptyList(), page = 1, pageSize = 20, totalPages = 1, totalItems = 0)
            coEvery {
                reputationRepository.getTeamReports("t1", ReportDirection.RECEIVED, 1, 20)
            } returns Result.Success(expected)

            val actual = sut("t1", ReportDirection.RECEIVED, 1, 20)

            assertEquals(Result.Success(expected), actual)
        }
}
