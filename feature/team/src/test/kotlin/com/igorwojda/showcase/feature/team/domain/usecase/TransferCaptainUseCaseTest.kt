package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.CaptainTransfer
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TransferCaptainUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = TransferCaptainUseCase(teamRepository)

    @Test
    fun `invoke transfers captaincy via repository`() =
        runTest {
            val expected =
                Result.Success(
                    CaptainTransfer(
                        teamId = "team-1",
                        fromUserId = "u1",
                        toUserId = "u2",
                        reason = "Bận",
                        transferredAt = "2026-11-18T10:00:00",
                    ),
                )
            coEvery { teamRepository.transferCaptain("team-1", "u2", "Bận") } returns expected

            val actual = sut("team-1", "u2", "Bận")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamRepository.transferCaptain("team-1", "u2", "Bận") }
        }
}
