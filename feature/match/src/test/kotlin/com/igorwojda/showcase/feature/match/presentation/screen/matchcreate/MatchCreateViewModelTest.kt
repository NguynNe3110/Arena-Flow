package com.igorwojda.showcase.feature.match.presentation.screen.matchcreate

import com.igorwojda.showcase.feature.base.domain.model.ActingTeamOption
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.CreateMatchUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.GetMatchDetailUseCase
import com.igorwojda.showcase.feature.match.domain.usecase.UpdateMatchUseCase
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
class MatchCreateViewModelTest {
    private val createMatchUseCase: CreateMatchUseCase = mockk()
    private val getMatchDetailUseCase: GetMatchDetailUseCase = mockk()
    private val getSessionStateUseCase: GetSessionStateUseCase = mockk()
    private val matchDraftHolder = MatchDraftHolder()
    private val sessionRepository: SessionRepository = mockk()
    private val updateMatchUseCase: UpdateMatchUseCase = mockk()

    private lateinit var sut: MatchCreateViewModel

    @BeforeEach
    fun setUp() {
        every { getSessionStateUseCase() } returns
            flowOf(
                ArenaSessionState(
                    actingTeamId = "team-alpha",
                    captainedTeams = listOf(ActingTeamOption("team-alpha", "FC Sao Mai", "An Khánh", 92, "ACTIVE")),
                ),
            )
        sut =
            MatchCreateViewModel(
                createMatchUseCase = createMatchUseCase,
                getMatchDetailUseCase = getMatchDetailUseCase,
                getSessionStateUseCase = getSessionStateUseCase,
                matchDraftHolder = matchDraftHolder,
                sessionRepository = sessionRepository,
                updateMatchUseCase = updateMatchUseCase,
            )
    }

    @Test
    fun `ConsumePendingVenue updates draft venueId and venueName`() =
        runTest {
            coEvery {
                sessionRepository.consumePendingVenueSelection(MATCH_VENUE_PICKER_REQUEST_KEY)
            } returns SelectedVenueResult(MATCH_VENUE_PICKER_REQUEST_KEY, "ven-anphu", "Sân An Phú", "An Khánh")

            sut.onEvent(MatchCreateEvent.ConsumePendingVenue)
            advanceUntilIdle()

            sut.uiStateFlow.value.draft.venueId shouldBeEqualTo "ven-anphu"
            sut.uiStateFlow.value.draft.venueName shouldBeEqualTo "Sân An Phú"
        }
}
