package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetTeamMembersUseCaseTest {
    private val teamMemberRepository: TeamMemberRepository = mockk()
    private val sut = GetTeamMembersUseCase(teamMemberRepository)

    @Test
    fun `invoke fetches active team members`() =
        runTest {
            val expected = Result.Success(emptyList<TeamMember>())
            coEvery { teamMemberRepository.getTeamMembers("team-1", page = 1, pageSize = 20) } returns expected

            val actual = sut("team-1")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamMemberRepository.getTeamMembers("team-1", page = 1, pageSize = 20) }
        }
}
