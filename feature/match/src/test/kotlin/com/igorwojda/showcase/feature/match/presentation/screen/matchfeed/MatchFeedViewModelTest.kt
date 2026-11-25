package com.igorwojda.showcase.feature.match.presentation.screen.matchfeed

import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.domain.usecase.UpdateActingTeamUseCase
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.model.MatchSummary
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchesUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMyMatchesUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.SendMatchRequestUseCase
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
class MatchFeedViewModelTest {
    private val getMatchesUseCase: GetMatchesUseCase = mockk()
    private val getMyMatchesUseCase: GetMyMatchesUseCase = mockk()
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private val sendMatchRequestUseCase: SendMatchRequestUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val updateActingTeamUseCase: UpdateActingTeamUseCase = mockk(relaxed = true)

    private lateinit var sut: MatchFeedViewModel

    @BeforeEach
    fun setUp() {
        every { getSessionStateUseCase() } returns
            flowOf(
                ArenaSessionState(
                    actingTeamId = "team-alpha",
                    captainedTeams = listOf(ActingTeamOption("team-alpha", "FC Sao Mai", "An Khánh", 92, "ACTIVE")),
                ),
            )
        every { sessionRepository.getWards() } returns emptyList()
    }

    @Test
    fun `init loads open matches into uiState`() =
        runTest {
            val match =
                MatchSummary(
                    matchId = "match-1",
                    type = MatchType.HAS_VENUE,
                    authorTeamName = "Sài Gòn United",
                    venueName = "Sân An Phú",
                )
            coEvery { getMatchesUseCase(any()) } returns Result.Success(MatchPage(listOf(match)))

            sut =
                MatchFeedViewModel(
                    getMatchesUseCase = getMatchesUseCase,
                    getMyMatchesUseCase = getMyMatchesUseCase,
                    getSessionStateUseCase = getSessionStateUseCase,
                    sendMatchRequestUseCase = sendMatchRequestUseCase,
                    sessionRepository = sessionRepository,
                    updateActingTeamUseCase = updateActingTeamUseCase,
                )
            advanceUntilIdle()

            sut.uiStateFlow.value.matches shouldBeEqualTo listOf(match)
            sut.uiStateFlow.value.isLoading shouldBeEqualTo false
        }
}
