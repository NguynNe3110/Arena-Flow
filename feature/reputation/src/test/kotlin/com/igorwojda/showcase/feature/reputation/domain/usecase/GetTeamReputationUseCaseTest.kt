package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.CompletionStats
import com.igorwojda.showcase.feature.reputation.domain.model.RatingSummary
import com.igorwojda.showcase.feature.reputation.domain.model.ReliabilityLevel
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation
import com.igorwojda.showcase.feature.reputation.domain.model.VerificationBadge
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetTeamReputationUseCaseTest {
    private val reputationRepository: ReputationRepository = mockk()
    private val sut = GetTeamReputationUseCase(reputationRepository)

    @Test
    fun `invoke returns team reputation`() =
        runTest {
            val expected =
                TeamReputation(
                    teamId = "t1",
                    reliabilityScore = 96,
                    reliabilityLevel = ReliabilityLevel.EXCELLENT,
                    completion = CompletionStats(10, 10, 100, false),
                    rating = RatingSummary(4.8, 10),
                    verification = VerificationBadge(true, "2026-11-10T10:00:00"),
                )
            coEvery { reputationRepository.getTeamReputation("t1") } returns Result.Success(expected)

            val actual = sut("t1")

            assertEquals(Result.Success(expected), actual)
        }
}
