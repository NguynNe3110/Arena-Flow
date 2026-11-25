package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetCaptainHistoryUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = GetCaptainHistoryUseCase(teamRepository)

    @Test
    fun `invoke fetches captain history from repository`() =
        runTest {
            val expected =
                Result.Success(
                    listOf(
                        CaptainHistoryEntry(
                            historyId = "ch-1",
                            teamId = "team-1",
                            fromUserId = null,
                            fromUserName = null,
                            toUserId = "u1",
                            toUserName = "User 1",
                            reason = null,
                            transferredAt = "2026-11-18T10:00:00",
                        ),
                    ),
                )
            coEvery { teamRepository.getCaptainHistory("team-1", page = 1, pageSize = 20) } returns expected

            val actual = sut("team-1")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamRepository.getCaptainHistory("team-1", page = 1, pageSize = 20) }
        }
}
