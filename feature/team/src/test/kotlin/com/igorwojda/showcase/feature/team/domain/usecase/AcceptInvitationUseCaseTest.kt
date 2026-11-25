package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AcceptInvitationUseCaseTest {
    private val teamMemberRepository: TeamMemberRepository = mockk()
    private val sut = AcceptInvitationUseCase(teamMemberRepository)

    @Test
    fun `invoke accepts invitation via repository`() =
        runTest {
            val expected =
                Result.Success(
                    TeamMember(
                        memberId = "inv-1",
                        teamId = "team-1",
                        userId = "u1",
                        displayName = "Trần Văn B",
                        avatarUrl = null,
                        role = TeamRole.MEMBER,
                        status = "ACTIVE",
                        joinedAt = "2026-11-18T10:00:00",
                    ),
                )
            coEvery { teamMemberRepository.acceptInvitation("inv-1") } returns expected

            val actual = sut("inv-1")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamMemberRepository.acceptInvitation("inv-1") }
        }
}
