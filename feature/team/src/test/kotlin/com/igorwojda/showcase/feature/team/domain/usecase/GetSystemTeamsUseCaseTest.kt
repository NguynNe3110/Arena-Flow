package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetSystemTeamsUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = GetSystemTeamsUseCase(teamRepository)

    @Test
    fun `invoke delegates to repository and returns teams`() =
        runTest {
            val expected =
                Result.Success(
                    listOf(
                        TeamSummary(
                            teamId = "team-1",
                            teamName = "FC Sao Mai",
                            avatarUrl = null,
                            skillLevels = listOf("Chill"),
                            status = TeamStatus.ACTIVE,
                            captainName = "Nguyễn Văn A",
                            memberCount = 4,
                            createdAt = "2026-11-01T10:00:00",
                            disbandedByName = null,
                        ),
                    ),
                )
            coEvery { teamRepository.getTeams(status = "ACTIVE", page = 1, pageSize = 20) } returns expected

            val actual = sut(status = "ACTIVE")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamRepository.getTeams(status = "ACTIVE", page = 1, pageSize = 20) }
        }
}
