package com.igorwojda.showcase.feature.match.presentation.screen.matchdetail

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.match.domain.model.MatchDetail
import com.igorwojda.showcase.feature.match.domain.model.MatchTeamSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.domain.usecase.AcceptMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.CancelMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.ForfeitMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchDetailUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchRequestsUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.RejectMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.SendMatchRequestUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.UpdateCostAgreementUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.WithdrawMatchRequestUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutinesTestDispatcherExtension::class)
class MatchDetailViewModelTest {
    private val acceptMatchRequestUseCase: AcceptMatchRequestUseCase = mockk()
    private val cancelMatchUseCase: CancelMatchUseCase = mockk()
    private val forfeitMatchUseCase: ForfeitMatchUseCase = mockk()
    private val getMatchDetailUseCase: GetMatchDetailUseCase = mockk()
    private val getMatchRequestsUseCase: GetMatchRequestsUseCase = mockk()
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private val rejectMatchRequestUseCase: RejectMatchRequestUseCase = mockk()
    private val sendMatchRequestUseCase: SendMatchRequestUseCase = mockk()
    private val updateCostAgreementUseCase: UpdateCostAgreementUseCase = mockk()
    private val withdrawMatchRequestUseCase: WithdrawMatchRequestUseCase = mockk()

    private lateinit var sut: MatchDetailViewModel

    @BeforeEach
    fun setUp() {
        every { getSessionStateUseCase() } returns flowOf(ArenaSessionState())
        sut =
            MatchDetailViewModel(
                acceptMatchRequestUseCase = acceptMatchRequestUseCase,
                cancelMatchUseCase = cancelMatchUseCase,
                forfeitMatchUseCase = forfeitMatchUseCase,
                getMatchDetailUseCase = getMatchDetailUseCase,
                getMatchRequestsUseCase = getMatchRequestsUseCase,
                getSessionStateUseCase = getSessionStateUseCase,
                rejectMatchRequestUseCase = rejectMatchRequestUseCase,
                sendMatchRequestUseCase = sendMatchRequestUseCase,
                updateCostAgreementUseCase = updateCostAgreementUseCase,
                withdrawMatchRequestUseCase = withdrawMatchRequestUseCase,
            )
    }

    @Test
    fun `Load event populates match detail and requests`() =
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
            coEvery { getMatchDetailUseCase("match-1") } returns Result.Success(detail)
            coEvery { getMatchRequestsUseCase("match-1", "ALL") } returns Result.Success(emptyList())

            sut.onEvent(MatchDetailEvent.Load("match-1"))
            advanceUntilIdle()

            sut.uiStateFlow.value.detail shouldBeEqualTo detail
            sut.uiStateFlow.value.isLoading shouldBeEqualTo false
        }
}
