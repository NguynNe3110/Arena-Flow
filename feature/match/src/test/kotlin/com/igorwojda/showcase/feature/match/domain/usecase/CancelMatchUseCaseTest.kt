package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.CancelMatchResult
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class CancelMatchUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = CancelMatchUseCase(matchRepository)

    @Test
    fun `when invoked then delegates to repository`() =
        runTest {
            val expected =
                Result.Success(
                    CancelMatchResult(
                        matchId = "match-1",
                        status = "CANCELLED",
                        cancelledByTeamId = "team-alpha",
                        cancelledAt = "2026-11-18T12:00:00",
                        cancelReason = "Thiếu người",
                        reliabilityPenalty = -2,
                    ),
                )
            coEvery { matchRepository.cancelMatch("match-1", "team-alpha", "CONFIRMED", "Thiếu người") } returns expected

            val actual = sut("match-1", "team-alpha", "CONFIRMED", "Thiếu người")

            actual shouldBeEqualTo expected
        }
}
