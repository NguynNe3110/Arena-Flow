package com.uzuu.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMyTeamUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = GetMyTeamUseCase(teamRepository)

    @Test
    fun `invoke calls repository getMyTeam`() =
        runTest {
            val team = mockk<TeamDetail>()
            val expected = Result.Success(team)
            coEvery { teamRepository.getMyTeam() } returns expected

            val actual = sut()

            assertEquals(expected, actual)
        }
}
