package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetTeamDetailUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = GetTeamDetailUseCase(teamRepository)

    @Test
    fun `invoke returns repository result`() =
        runTest {
            val expected = Result.Failure(AppError.NotFound)
            coEvery { teamRepository.getTeamDetail("team-1") } returns expected

            val actual = sut("team-1")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamRepository.getTeamDetail("team-1") }
        }
}
