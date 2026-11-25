package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamInvitation
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InviteTeamMemberUseCaseTest {
    private val teamMemberRepository: TeamMemberRepository = mockk()
    private val sut = InviteTeamMemberUseCase(teamMemberRepository)

    @Test
    fun `invoke invites user to team`() =
        runTest {
            val expected =
                Result.Success(
                    TeamInvitation(
                        invitationId = "inv-1",
                        teamId = "team-1",
                        teamName = "FC Sao Mai",
                        userId = "u2",
                        status = "PENDING",
                        invitedAt = "2026-11-18T10:00:00",
                    ),
                )
            coEvery { teamMemberRepository.inviteMember("team-1", "u2") } returns expected

            val actual = sut("team-1", "u2")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamMemberRepository.inviteMember("team-1", "u2") }
        }
}
