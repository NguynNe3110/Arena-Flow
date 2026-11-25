package com.igorwojda.showcase.feature.match.data.repository

import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MockMatchCommandRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MockMatchQueryRetrofitService
import com.igorwojda.showcase.feature.match.data.datasource.api.service.MockMatchRequestRetrofitService
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.jupiter.api.Test

class MatchRepositoryImplTest {
    private val store = ArenaInMemoryStore()
    private val sut =
        MatchRepositoryImpl(
            queryService = MockMatchQueryRetrofitService(store),
            commandService = MockMatchCommandRetrofitService(store),
            requestService = MockMatchRequestRetrofitService(store),
        )

    @Test
    fun `getMatches returns open matches from store`() =
        runTest {
            val result = sut.getMatches(MatchFilter())

            result shouldBeInstanceOf Result.Success::class
            val page = (result as Result.Success).value
            page.items.shouldNotBeEmpty()
        }

    @Test
    fun `createMatch appends match to store when valid`() =
        runTest {
            val result =
                sut.createMatch(
                    MatchDraft(
                        kind = MatchType.HAS_VENUE,
                        hostTeamId = "team-alpha",
                        hostTeamName = "FC Sao Mai",
                        venueId = "ven-anphu",
                        matchDate = "2026-11-22",
                        startTime = "19:00",
                        endTime = "20:30",
                        expiresAt = "2026-11-22T18:00:00",
                    ),
                )

            result shouldBeInstanceOf Result.Success::class
            (result as Result.Success).value.authorTeam.teamId shouldBeEqualTo "team-alpha"
        }

    @Test
    fun `sendMatchRequest returns DomainError when team requests own match`() =
        runTest {
            val result = sut.sendMatchRequest(matchId = "match-a-open", teamId = "team-alpha")

            result shouldBeInstanceOf Result.Failure::class
            val error = (result as Result.Failure).error
            error shouldBeInstanceOf AppError.DomainError::class
            (error as AppError.DomainError).code shouldBeEqualTo "MATCH_007"
        }
}
