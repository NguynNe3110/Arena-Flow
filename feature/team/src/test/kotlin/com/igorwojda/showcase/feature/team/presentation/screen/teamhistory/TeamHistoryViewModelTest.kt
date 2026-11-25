package com.igorwojda.showcase.feature.team.presentation.screen.teamhistory

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.usecase.GetCaptainHistoryUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamMembersUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class TeamHistoryViewModelTest {
    @JvmField
    @RegisterExtension
    val coroutinesTestExtension = CoroutinesTestDispatcherExtension()

    private val getCaptainHistoryUseCase: GetCaptainHistoryUseCase = mockk()
    private val getTeamDetailUseCase: GetTeamDetailUseCase = mockk()
    private val getTeamMembersUseCase: GetTeamMembersUseCase = mockk()

    private val sut =
        TeamHistoryViewModel(
            getCaptainHistoryUseCase = getCaptainHistoryUseCase,
            getTeamDetailUseCase = getTeamDetailUseCase,
            getTeamMembersUseCase = getTeamMembersUseCase,
        )

    @Test
    fun `selectTab switches between captain and member history`() =
        runTest {
            val detail =
                TeamDetail(
                    teamId = "team-1",
                    teamName = "FC Sao Mai",
                    avatarUrl = null,
                    description = null,
                    skillLevels = emptyList(),
                    status = TeamStatus.ACTIVE,
                    inactiveReason = null,
                    captainId = "u1",
                    captainName = "Nguyễn Văn A",
                    memberCount = 2,
                    createdBy = "u1",
                    createdAt = "2026-11-01T10:00:00",
                    updatedAt = "2026-11-01T10:00:00",
                    disbandedAt = null,
                    disbandedById = null,
                    disbandedByName = null,
                    disbandReason = null,
                )
            coEvery { getTeamDetailUseCase("team-1") } returns Result.Success(detail)
            coEvery { getCaptainHistoryUseCase("team-1") } returns Result.Success(emptyList())
            coEvery { getTeamMembersUseCase("team-1") } returns Result.Success(emptyList())

            sut.load("team-1")
            advanceUntilIdle()
            sut.selectTab(TeamHistoryTab.MEMBERS)

            assertEquals(TeamHistoryTab.MEMBERS, sut.uiStateFlow.value.selectedTab)
        }
}
