package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchRequest
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetMatchRequestsUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetMatchRequestsUseCase(matchRepository)

    @Test
    fun `when invoked then returns match requests`() =
        runTest {
            val expected = Result.Success(emptyList<MatchRequest>())
            coEvery { matchRepository.getMatchRequests("match-1", null) } returns expected

            val actual = sut("match-1")

            actual shouldBeEqualTo expected
        }
}
