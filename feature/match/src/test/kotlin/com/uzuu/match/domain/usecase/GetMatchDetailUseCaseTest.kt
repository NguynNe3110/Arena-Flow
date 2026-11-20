package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMatchDetailUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetMatchDetailUseCase(matchRepository)

    @Test
    fun `invoke returns match detail from repository`() =
        runTest {
            val expected = Result.Success(MatchDetail(MatchSummary("id", MatchType.HAS_VENUE, "Team A", null)))
            coEvery { matchRepository.getMatch("id") } returns expected

            val actual = sut("id")

            assertEquals(expected, actual)
        }
}
