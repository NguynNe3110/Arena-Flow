package com.uzuu.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.domain.model.TeamMember
import com.uzuu.team.domain.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InviteTeamMemberUseCaseTest {
    private val teamRepository: TeamRepository = mockk()
    private val sut = InviteTeamMemberUseCase(teamRepository)

    @Test
    fun `invoke calls repository inviteTeamMember`() =
        runTest {
            val member = mockk<TeamMember>()
            val expected = Result.Success(member)
            coEvery { teamRepository.inviteTeamMember("t1", "Lê Hoàng", "Tiền vệ") } returns expected

            val actual = sut("t1", "Lê Hoàng", "Tiền vệ")

            assertEquals(expected, actual)
        }
}
