package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.model.TeamRole
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetMyTeamsUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = GetMyTeamsUseCase(teamRepository)

    @Test
    fun `invoke delegates to repository and returns result`() =
        runTest {
            val expected =
                Result.Success(
                    listOf(
                        MyTeam(
                            teamId = "team-1",
                            teamName = "FC Sao Mai",
                            avatarUrl = null,
                            skillLevels = listOf("Nhiệt tình"),
                            status = TeamStatus.ACTIVE,
                            captainName = "Nguyễn Văn A",
                            memberCount = 5,
                            role = TeamRole.CAPTAIN,
                            joinedAt = "2026-11-01T10:00:00",
                        ),
                    ),
                )
            coEvery { teamRepository.getMyTeams(role = "CAPTAIN", page = 1, pageSize = 20) } returns expected

            val actual = sut(role = "CAPTAIN")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamRepository.getMyTeams(role = "CAPTAIN", page = 1, pageSize = 20) }
        }
}
