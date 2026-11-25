package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class RejectMatchRequestUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = RejectMatchRequestUseCase(matchRepository)

    @Test
    fun `when invoked then delegates to repository`() =
        runTest {
            val expected = Result.Success(Unit)
            coEvery { matchRepository.rejectMatchRequest("req-1", null) } returns expected

            val actual = sut("req-1")

            actual shouldBeEqualTo expected
        }
}
