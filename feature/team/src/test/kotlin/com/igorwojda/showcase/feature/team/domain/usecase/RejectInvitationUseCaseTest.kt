package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RejectInvitationUseCaseTest {
    private val teamMemberRepository: TeamMemberRepository = mockk()
    private val sut = RejectInvitationUseCase(teamMemberRepository)

    @Test
    fun `invoke rejects invitation via repository`() =
        runTest {
            val expected = Result.Success(Unit)
            coEvery { teamMemberRepository.rejectInvitation("inv-1") } returns expected

            val actual = sut("inv-1")

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamMemberRepository.rejectInvitation("inv-1") }
        }
}
