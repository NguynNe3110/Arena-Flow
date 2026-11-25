package com.igorwojda.showcase.feature.team.presentation.screen.teammanage

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
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
internal class TeamManageViewModelTest {
    @JvmField
    @RegisterExtension
    val coroutinesTestExtension = CoroutinesTestDispatcherExtension()

    private val getTeamDetailUseCase: GetTeamDetailUseCase = mockk()
    private val sut = TeamManageViewModel(getTeamDetailUseCase)

    @Test
    fun `load updates state with team detail`() =
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
