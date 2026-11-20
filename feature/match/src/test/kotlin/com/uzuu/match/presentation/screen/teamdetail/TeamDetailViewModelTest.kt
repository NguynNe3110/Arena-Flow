package com.uzuu.match.presentation.screen.teamdetail

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.model.TeamMember
import com.uzuu.match.domain.usecase.GetMyTeamUseCase
import com.uzuu.match.domain.usecase.InviteTeamMemberUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class TeamDetailViewModelTest {
    private val getMyTeamUseCase: GetMyTeamUseCase = mockk()
    private val inviteTeamMemberUseCase: InviteTeamMemberUseCase = mockk()

    private val sampleTeam =
        TeamDetail(
            id = "t1",
            name = "Bình Minh FC",
            shortName = "BM",
            area = "Hải Châu, Đà Nẵng",
            reputationScore = 100,
            personalReputationScore = 95,
            rating = 4.8,
            matchCount = 25,
            completionRate = 96,
            members =
                listOf(
                    TeamMember("m1", "Lê Hoàng", "Đội trưởng", "Tiền vệ", 95, isCurrentUser = true),
                    TeamMember("m2", "Người Cũ", "Thành viên", "Hậu vệ", 70, isDeletedUser = true),
                ),
        )

    @Test
    fun `loadTeam loads team successfully`() =
        runTest {
            coEvery { getMyTeamUseCase() } returns Result.Success(sampleTeam)

            val sut = TeamDetailViewModel(getMyTeamUseCase, inviteTeamMemberUseCase)
            advanceUntilIdle()

            assertEquals(sampleTeam, sut.uiState.value.team)
            assertFalse(sut.uiState.value.isLoading)
            // AC-P02 deleted user check
            val deletedMember =
                sut.uiState.value.team
                    ?.members
                    ?.find { it.isDeletedUser }
            assertEquals("Người dùng ArenaFlow", deletedMember?.displayName)
        }

    @Test
    fun `selectTab updates selected tab`() =
        runTest {
            coEvery { getMyTeamUseCase() } returns Result.Success(sampleTeam)

            val sut = TeamDetailViewModel(getMyTeamUseCase, inviteTeamMemberUseCase)
            advanceUntilIdle()

            sut.selectTab(1)
            assertEquals(1, sut.uiState.value.selectedTab)

            sut.selectTab(2)
            assertEquals(2, sut.uiState.value.selectedTab)
        }

    @Test
    fun `submitInvite adds member and displays success message`() =
        runTest {
            val newMember = TeamMember("m3", "Trần Minh", "Thành viên", "Hậu vệ", 88)
            coEvery { getMyTeamUseCase() } returns Result.Success(sampleTeam)
            coEvery { inviteTeamMemberUseCase("t1", "Trần Minh", "Hậu vệ") } returns Result.Success(newMember)

            val sut = TeamDetailViewModel(getMyTeamUseCase, inviteTeamMemberUseCase)
            advanceUntilIdle()

            sut.showInviteSheet()
            assertTrue(sut.uiState.value.isInviteSheetVisible)

            sut.updateInviteName("Trần Minh")
            sut.updateInvitePosition("Hậu vệ")
            sut.submitInvite()
            advanceUntilIdle()

            assertFalse(sut.uiState.value.isInviteSheetVisible)
            assertTrue(
                sut.uiState.value.team
                    ?.members
                    ?.contains(newMember) == true,
            )
            assertTrue(
                sut.uiState.value.inviteSuccessMessage
                    ?.contains("Trần Minh") == true,
            )
        }
}
