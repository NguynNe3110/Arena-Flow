@file:Suppress("LongMethod")

package com.igorwojda.showcase.feature.team.presentation.screen.teamhub

import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary
import com.igorwojda.showcase.feature.team.domain.usecase.AcceptInvitationUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyInvitationsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyTeamsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetSystemTeamsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.RejectInvitationUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class TeamHubViewModelTest {
    @JvmField
    @RegisterExtension
    val coroutinesTestExtension = CoroutinesTestDispatcherExtension()

    private val acceptInvitationUseCase: AcceptInvitationUseCase = mockk()
    private val getMyInvitationsUseCase: GetMyInvitationsUseCase = mockk()
    private val getMyTeamsUseCase: GetMyTeamsUseCase = mockk()
    private val getSystemTeamsUseCase: GetSystemTeamsUseCase = mockk()
    private val rejectInvitationUseCase: RejectInvitationUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()

    private lateinit var sut: TeamHubViewModel

    @Test
    fun `init loads my teams and system teams for logged in user`() =
        runTest {
            val user =
                SessionUserSummary(
                    id = "u1",
                    phone = "0987654321",
                    displayName = "Nguyễn Văn A",
                    preferredPosition = null,
                    wardId = null,
                    role = AccountRole.USER,
                    isNewUser = false,
                    reputationScore = 100,
                    matchCount = 1,
                )
            every { sessionRepository.observeSession() } returns flowOf(ArenaSessionState(currentUser = user))
            coEvery { getSystemTeamsUseCase(status = "ALL") } returns
                Result.Success(
                    listOf(
                        TeamSummary(
                            teamId = "t1",
                            teamName = "FC Sao Mai",
                            avatarUrl = null,
                            skillLevels = listOf("Chill"),
                            status = TeamStatus.ACTIVE,
                            captainName = "Nguyễn Văn A",
                            memberCount = 5,
                            createdAt = "2026-11-01T10:00:00",
                            disbandedByName = null,
                        ),
                    ),
                )
            coEvery { getMyTeamsUseCase() } returns
                Result.Success(
                    listOf(
                        MyTeam(
                            teamId = "t1",
                            teamName = "FC Sao Mai",
                            avatarUrl = null,
                            skillLevels = listOf("Chill"),
                            status = TeamStatus.ACTIVE,
                            captainName = "Nguyễn Văn A",
                            memberCount = 5,
                            role = TeamRole.CAPTAIN,
                            joinedAt = "2026-11-01T10:00:00",
                        ),
                    ),
                )
            coEvery { getMyInvitationsUseCase() } returns Result.Success(emptyList())

            sut =
                TeamHubViewModel(
                    acceptInvitationUseCase = acceptInvitationUseCase,
                    getMyInvitationsUseCase = getMyInvitationsUseCase,
                    getMyTeamsUseCase = getMyTeamsUseCase,
                    getSystemTeamsUseCase = getSystemTeamsUseCase,
                    rejectInvitationUseCase = rejectInvitationUseCase,
                    sessionRepository = sessionRepository,
                )
            advanceUntilIdle()

            val state = sut.uiStateFlow.value
            assertFalse(state.isLoading)
            assertEquals(1, state.myTeams.size)
            assertEquals(1, state.systemTeams.size)
        }
}
