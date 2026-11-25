package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamDraft
import com.igorwojda.showcase.feature.team.domain.model.TeamStatus
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CreateTeamUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = CreateTeamUseCase(teamRepository)

    @Test
    fun `invoke creates team via repository`() =
        runTest {
            val draft = TeamDraft(teamName = "FC Mới", skillLevels = listOf("Chill"))
            val expected =
                Result.Success(
                    TeamDetail(
                        teamId = "team-new",
                        teamName = "FC Mới",
                        avatarUrl = null,
                        description = null,
                        skillLevels = listOf("Chill"),
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
                    ),
                )
            coEvery { teamRepository.createTeam(draft) } returns expected

            val actual = sut(draft)

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamRepository.createTeam(draft) }
        }
}
