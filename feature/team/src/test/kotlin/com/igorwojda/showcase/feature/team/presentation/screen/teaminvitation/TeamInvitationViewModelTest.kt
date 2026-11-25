package com.igorwojda.showcase.feature.team.presentation.screen.teaminvitation

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.InvitationTeamSummary
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.usecase.AcceptInvitationUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetMyInvitationsUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.RejectInvitationUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class TeamInvitationViewModelTest {
    @JvmField
    @RegisterExtension
    val coroutinesTestExtension = CoroutinesTestDispatcherExtension()

    private val acceptInvitationUseCase: AcceptInvitationUseCase = mockk()
    private val getMyInvitationsUseCase: GetMyInvitationsUseCase = mockk()
    private val rejectInvitationUseCase: RejectInvitationUseCase = mockk()

    private val sut =
        TeamInvitationViewModel(
            acceptInvitationUseCase = acceptInvitationUseCase,
            getMyInvitationsUseCase = getMyInvitationsUseCase,
            rejectInvitationUseCase = rejectInvitationUseCase,
        )

    @Test
    fun `load fetches invitations and orders focused invitation first`() =
        runTest {
            val inv1 =
                MyInvitation(
                    invitationId = "inv-1",
                    invitedAt = "2026-11-10T10:00:00",
                    team =
                        InvitationTeamSummary(
                            teamId = "t1",
                            teamName = "Team 1",
                            avatarUrl = null,
                            skillLevels = emptyList(),
                            status = TeamStatus.ACTIVE,
                            captainName = "Cap 1",
                            memberCount = 4,
                        ),
                )
            val inv2 =
                MyInvitation(
                    invitationId = "inv-2",
                    invitedAt = "2026-11-11T10:00:00",
                    team =
                        InvitationTeamSummary(
                            teamId = "t2",
                            teamName = "Team 2",
                            avatarUrl = null,
                            skillLevels = emptyList(),
                            status = TeamStatus.ACTIVE,
                            captainName = "Cap 2",
                            memberCount = 5,
                        ),
                )
            coEvery { getMyInvitationsUseCase() } returns Result.Success(listOf(inv1, inv2))

            sut.load(focusInvitationId = "inv-1")
            advanceUntilIdle()

            assertFalse(sut.uiStateFlow.value.isLoading)
            assertEquals(
                "inv-1",
                sut.uiStateFlow.value.invitations
                    .first()
                    .invitationId,
            )
        }
}
