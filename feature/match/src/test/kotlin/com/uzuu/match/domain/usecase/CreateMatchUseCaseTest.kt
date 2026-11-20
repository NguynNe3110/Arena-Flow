package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MatchDraft
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateMatchUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = CreateMatchUseCase(matchRepository)

    @Test
    fun `invoke calls repository createMatch and returns result`() = runTest {
        val draft = MatchDraft(kind = MatchType.HAS_VENUE)
        val expected = Result.Success(
            MatchSummary(
                matchId = "m_new",
                type = MatchType.HAS_VENUE,
                authorTeamName = "Team A",
                venueName = "San A",
            ),
        )
        coEvery { matchRepository.createMatch(draft) } returns expected

        val actual = sut(draft)

        assertEquals(expected, actual)
    }
}
