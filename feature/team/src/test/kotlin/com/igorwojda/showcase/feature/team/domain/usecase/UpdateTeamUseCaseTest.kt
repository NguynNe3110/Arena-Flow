package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDraft
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UpdateTeamUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = UpdateTeamUseCase(teamRepository)

    @Test
    fun `invoke updates team via repository`() =
        runTest {
            val draft = TeamDraft(teamName = "FC Updated", skillLevels = listOf("Thi đấu"))
            val expected = Result.Failure(AppError.Unauthorized)
            coEvery { teamRepository.updateTeam("team-1", draft) } returns expected

            val actual = sut("team-1", draft)

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamRepository.updateTeam("team-1", draft) }
        }
}
