package com.igorwojda.showcase.feature.team.presentation.screen.teamdetail

import com.igorwojda.showcase.feature.base.domain.model.ArenaSessionState
import com.igorwojda.showcase.feature.base.domain.repository.SessionRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.usecase.DisbandTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetCaptainHistoryUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamMembersUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.InviteTeamMemberUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.LeaveTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.SearchInvitationCandidatesUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.TransferCaptainUseCase
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
internal class TeamDetailViewModelTest {
    @JvmField
    @RegisterExtension
    val coroutinesTestExtension = CoroutinesTestDispatcherExtension()

    private val disbandTeamUseCase: DisbandTeamUseCase = mockk()
    private val getCaptainHistoryUseCase: GetCaptainHistoryUseCase = mockk()
    private val getTeamDetailUseCase: GetTeamDetailUseCase = mockk()
    private val getTeamMembersUseCase: GetTeamMembersUseCase = mockk()
    private val inviteTeamMemberUseCase: InviteTeamMemberUseCase = mockk()
    private val leaveTeamUseCase: LeaveTeamUseCase = mockk()
    private val searchInvitationCandidatesUseCase: SearchInvitationCandidatesUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val transferCaptainUseCase: TransferCaptainUseCase = mockk()

    private lateinit var sut: TeamDetailViewModel

    @Test
    fun `load fetches team detail members and captain history`() =
        runTest {
            every { sessionRepository.observeSession() } returns flowOf(ArenaSessionState())
            val detail =
                TeamDetail(
                    teamId = "team-1",
                    teamName = "FC Sao Mai",
                    avatarUrl = null,
                    description = "Desc",
                    skillLevels = listOf("Chill"),
                    status = TeamStatus.ACTIVE,
                    inactiveReason = null,
                    captainId = "u1",
                    captainName = "Nguyễn Văn A",
                    memberCount = 1,
                    createdBy = "u1",
                    createdAt = "2026-11-01T10:00:00",
                    updatedAt = "2026-11-01T10:00:00",
                    disbandedAt = null,
                    disbandedById = null,
                    disbandedByName = null,
                    disbandReason = null,
                )
            coEvery { getTeamDetailUseCase("team-1") } returns Result.Success(detail)
            coEvery { getTeamMembersUseCase("team-1") } returns Result.Success(emptyList())
            coEvery { getCaptainHistoryUseCase("team-1") } returns Result.Success(emptyList())

            sut =
                TeamDetailViewModel(
                    disbandTeamUseCase = disbandTeamUseCase,
                    getCaptainHistoryUseCase = getCaptainHistoryUseCase,
                    getTeamDetailUseCase = getTeamDetailUseCase,
                    getTeamMembersUseCase = getTeamMembersUseCase,
                    inviteTeamMemberUseCase = inviteTeamMemberUseCase,
                    leaveTeamUseCase = leaveTeamUseCase,
                    searchInvitationCandidatesUseCase = searchInvitationCandidatesUseCase,
                    sessionRepository = sessionRepository,
                    transferCaptainUseCase = transferCaptainUseCase,
                )
            sut.load("team-1")
            advanceUntilIdle()

            assertFalse(sut.uiStateFlow.value.isLoading)
            assertEquals(
                "FC Sao Mai",
                sut.uiStateFlow.value.team
                    ?.teamName,
            )
        }
}
