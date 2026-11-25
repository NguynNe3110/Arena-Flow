package com.igorwojda.showcase.feature.reputation.data.repository

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.CompletionStatsDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.RatingSummaryDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.TeamReputationDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.VerificationBadgeDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationCommandRetrofitService
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationQueryRetrofitService
import com.igorwojda.showcase.feature.reputation.domain.model.ReliabilityLevel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReputationRepositoryImplTest {
    private val queryService: ReputationQueryRetrofitService = mockk()
    private val commandService: ReputationCommandRetrofitService = mockk()
    private val sut = ReputationRepositoryImpl(queryService, commandService)

    @Test
    fun `getTeamReputation maps CRITICAL when score below 40`() =
        runTest {
            val dto =
                TeamReputationDto(
                    teamId = "t1",
                    reliabilityScore = 35,
                    reliabilityLevel = "WARNING",
                    completion = CompletionStatsDto(2, 6, 33, false),
                    rating = RatingSummaryDto(2.5, 4),
                    verification = VerificationBadgeDto(false, null),
                )
            coEvery { queryService.getTeamReputation("t1") } returns ApiResult.Success(ApiEnvelope(dto))

            val result = sut.getTeamReputation("t1")

            assertTrue(result is Result.Success)
            val reputation = (result as Result.Success).value
            assertEquals(ReliabilityLevel.CRITICAL, reputation.reliabilityLevel)
            assertEquals(35, reputation.reliabilityScore)
        }

    @Test
    fun `getTeamReputation maps ApiErrorEnvelope to DomainError`() =
        runTest {
            val errorJson = """{"error":{"code":"TEAM_001","message":"Không tìm thấy đội"}}"""
            coEvery { queryService.getTeamReputation("missing") } returns
                ApiResult.Error(code = 404, message = "Not Found", errorBody = errorJson)

            val result = sut.getTeamReputation("missing")

            assertTrue(result is Result.Failure)
            val error = (result as Result.Failure).error
            assertTrue(error is AppError.DomainError)
            assertEquals("TEAM_001", (error as AppError.DomainError).code)
        }
}
