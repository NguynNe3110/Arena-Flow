package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchTeamSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetMatchDetailUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetMatchDetailUseCase(matchRepository)

    @Test
    fun `when invoked then returns match detail`() =
        runTest {
            val detail =
                MatchDetail(
                    matchId = "match-1",
                    type = MatchType.HAS_VENUE,
                    status = "OPEN",
                    sportId = "sport-football",
                    sportName = "Bóng đá",
                    format = "SEVEN_A_SIDE",
                    startsAt = "2026-11-21T19:00:00",
                    endsAt = "2026-11-21T20:30:00",
                    expiresAt = "2026-11-21T18:00:00",
                    authorTeam = MatchTeamSummary("team-alpha", "FC Sao Mai"),
                )
            val expected = Result.Success(detail)
            coEvery { matchRepository.getMatchDetail("match-1") } returns expected

            val actual = sut("match-1")

            actual shouldBeEqualTo expected
        }
}
