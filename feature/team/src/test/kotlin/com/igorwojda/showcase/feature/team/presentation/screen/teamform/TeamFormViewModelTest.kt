package com.igorwojda.showcase.feature.team.presentation.screen.teamform

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.usecase.CreateTeamUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.GetTeamDetailUseCase
import com.igorwojda.showcase.feature.team.domain.usecase.UpdateTeamUseCase
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class TeamFormViewModelTest {
    @JvmField
    @RegisterExtension
    val coroutinesTestExtension = CoroutinesTestDispatcherExtension()

    private val createTeamUseCase: CreateTeamUseCase = mockk()
    private val getTeamDetailUseCase: GetTeamDetailUseCase = mockk()
    private val updateTeamUseCase: UpdateTeamUseCase = mockk()

    private val sut =
        TeamFormViewModel(
            createTeamUseCase = createTeamUseCase,
            getTeamDetailUseCase = getTeamDetailUseCase,
            updateTeamUseCase = updateTeamUseCase,
        )

    @Test
    fun `submit with blank name sets validation error`() =
        runTest {
            sut.onNameChange("   ")
            var createdId: String? = null

            sut.submit { createdId = it }

            assertEquals(null, createdId)
            assertNotNull(sut.uiStateFlow.value.nameError)
        }

    @Test
    fun `submit creates team when valid`() =
        runTest {
            val detail =
                TeamDetail(
                    teamId = "team-new",
                    teamName = "FC Phượng Hoàng",
                    avatarUrl = null,
                    description = "Giao lưu",
                    skillLevels = listOf("Nhiệt tình", "Rèn luyện"),
                    status = TeamStatus.ACTIVE,
                    inactiveReason = null,
                    captainId = "u1",
                    captainName = "User 1",
                    memberCount = 1,
                    createdBy = "u1",
                    createdAt = "2026-11-18T10:00:00",
                    updatedAt = "2026-11-18T10:00:00",
                    disbandedAt = null,
                    disbandedById = null,
                    disbandedByName = null,
                    disbandReason = null,
                )
            coEvery { createTeamUseCase(any()) } returns Result.Success(detail)

            sut.onNameChange("FC Phượng Hoàng")
            sut.onDescriptionChange("Giao lưu")
            var createdId: String? = null
            sut.submit { createdId = it }
            advanceUntilIdle()

            assertEquals("team-new", createdId)
            coVerify(exactly = 1) { createTeamUseCase(any()) }
        }
}
