package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.WithdrawRequestResult
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class WithdrawMatchRequestUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = WithdrawMatchRequestUseCase(matchRepository)

    @Test
    fun `when invoked then delegates to repository`() =
        runTest {
            val expected =
                Result.Success(
                    WithdrawRequestResult(
                        requestId = "req-1",
                        matchId = "match-1",
                        status = "WITHDRAWN",
                        withdrawnAt = "2026-11-18T10:00:00",
                        matchStatus = "OPEN",
                        reliabilityPenalty = 0,
                    ),
                )
            coEvery { matchRepository.withdrawMatchRequest("req-1", "PENDING", null) } returns expected

            val actual = sut("req-1", "PENDING")

            actual shouldBeEqualTo expected
        }
}
