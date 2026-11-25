package com.igorwojda.showcase.feature.match.presentation.screen.matchreview

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchDraft
import com.igorwojda.showcase.feature.match.domain.model.MatchTeamSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.domain.usecase.CreateMatchUseCase
import com.igorwojda.showcase.feature.match.presentation.screen.matchcreate.MatchDraftHolder
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutinesTestDispatcherExtension::class)
class MatchReviewViewModelTest {
    private val createMatchUseCase: CreateMatchUseCase = mockk()
    private val matchDraftHolder = MatchDraftHolder()

    private lateinit var sut: MatchReviewViewModel

    @BeforeEach
    fun setUp() {
        matchDraftHolder.set(MatchDraft(hostTeamId = "team-alpha", hostTeamName = "FC Sao Mai"))
        sut = MatchReviewViewModel(createMatchUseCase = createMatchUseCase, matchDraftHolder = matchDraftHolder)
    }

    @Test
    fun `ConfirmPost sets postedMatchId on success`() =
        runTest {
            val detail =
                MatchDetail(
                    matchId = "match-new",
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
            coEvery { createMatchUseCase(any()) } returns Result.Success(detail)

            sut.onEvent(MatchReviewEvent.ConfirmPost)
            advanceUntilIdle()

            sut.uiStateFlow.value.postedMatchId shouldBeEqualTo "match-new"
        }
}
