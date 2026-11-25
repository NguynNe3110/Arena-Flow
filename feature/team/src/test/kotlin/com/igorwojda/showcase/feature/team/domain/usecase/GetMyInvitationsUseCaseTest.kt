package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetMyInvitationsUseCaseTest {
    private val teamMemberRepository: TeamMemberRepository = mockk()
    private val sut = GetMyInvitationsUseCase(teamMemberRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            val expected = Result.Success(emptyList<MyInvitation>())
            coEvery { teamMemberRepository.getMyInvitations(page = 1, pageSize = 20) } returns expected

            val actual = sut()

            assertEquals(expected, actual)
            coVerify(exactly = 1) { teamMemberRepository.getMyInvitations(page = 1, pageSize = 20) }
        }
}
