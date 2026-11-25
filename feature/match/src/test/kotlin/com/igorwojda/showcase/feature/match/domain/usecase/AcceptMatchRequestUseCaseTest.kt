package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.AcceptRequestResult
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AcceptMatchRequestUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = AcceptMatchRequestUseCase(matchRepository)

    @Test
    fun `when invoked then delegates to repository`() =
        runTest {
            val expected =
                Result.Success(
                    AcceptRequestResult(
                        matchId = "match-1",
                        matchStatus = "CONFIRMED",
                        requestId = "req-1",
                        requestStatus = "ACCEPTED",
                        opponentTeamId = "team-beta",
                        opponentTeamName = "Sài Gòn United",
                        respondedAt = "2026-11-18T10:00:00",
                    ),
                )
            coEvery { matchRepository.acceptMatchRequest("req-1") } returns expected

            val actual = sut("req-1")

            actual shouldBeEqualTo expected
        }
}
