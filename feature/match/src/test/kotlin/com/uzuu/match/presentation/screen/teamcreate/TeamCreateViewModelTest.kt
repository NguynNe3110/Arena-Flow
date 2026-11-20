package com.uzuu.match.presentation.screen.teamcreate

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.library.testutils.CoroutinesTestDispatcherExtension
import com.igorwojda.showcase.library.testutils.InstantTaskExecutorExtension
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.usecase.CreateTeamUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class, CoroutinesTestDispatcherExtension::class)
class TeamCreateViewModelTest {
    private val createTeamUseCase: CreateTeamUseCase = mockk()

    @Test
    fun `blank team name produces validation error`() = runTest {
        val sut = TeamCreateViewModel(createTeamUseCase)
        var created = false

        sut.updateTeamName("   ")
        sut.createTeam { created = true }

        assertFalse(created)
        assertNotNull(sut.uiState.value.nameError)
    }

    @Test
    fun `toggleSkill restricts to maximum 3 skill levels`() = runTest {
        val sut = TeamCreateViewModel(createTeamUseCase)

        sut.toggleSkill("Vui vẻ")
        sut.toggleSkill("Bán chuyên")
        // Now has 3: "Trung bình", "Vui vẻ", "Bán chuyên"
        assertEquals(3, sut.uiState.value.selectedSkills.size)

        // Adding 4th should be ignored per max 3 rule
        sut.toggleSkill("Chuyên nghiệp")
        assertEquals(3, sut.uiState.value.selectedSkills.size)
    }

    @Test
    fun `valid draft creates team successfully`() = runTest {
        val team = TeamDetail(id = "team-new", name = "Sài Gòn Utd", shortName = "SGU")
        coEvery { createTeamUseCase(any()) } returns Result.Success(team)

        val sut = TeamCreateViewModel(createTeamUseCase)
        var createdId: String? = null

        sut.updateTeamName("Sài Gòn Utd")
        sut.updateShortName("SGU")
        sut.createTeam { createdId = it }
        advanceUntilIdle()

        assertEquals("team-new", createdId)
        assertTrue(sut.uiState.value.isCreatedSuccessfully)
        assertFalse(sut.uiState.value.isSubmitting)
    }
}
