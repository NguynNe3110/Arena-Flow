package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SendMatchRequestUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = SendMatchRequestUseCase(matchRepository)

    @Test
    fun `when invoked then delegates to repository`() =
        runTest {
            val req =
                MatchRequest(
                    requestId = "req-1",
                    matchId = "match-1",
                    conversationId = "conv-1",
                    requesterTeamId = "team-beta",
                    requesterTeamName = "Sài Gòn United",
                    requesterTeamReliabilityScore = 88,
                    status = "PENDING",
                    requestedAt = "2026-11-18T10:00:00",
                )
            val expected = Result.Success(req)
            coEvery { matchRepository.sendMatchRequest("match-1", "team-beta", null) } returns expected

            val actual = sut("match-1", "team-beta")

            actual shouldBeEqualTo expected
        }
}
