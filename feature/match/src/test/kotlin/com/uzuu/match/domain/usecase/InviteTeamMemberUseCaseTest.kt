package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.TeamMember
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InviteTeamMemberUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = InviteTeamMemberUseCase(matchRepository)

    @Test
    fun `invoke calls repository inviteTeamMember`() =
        runTest {
            val member = mockk<TeamMember>()
            val expected = Result.Success(member)
            coEvery { matchRepository.inviteTeamMember("t1", "Lê Hoàng", "Tiền vệ") } returns expected

            val actual = sut("t1", "Lê Hoàng", "Tiền vệ")

            assertEquals(expected, actual)
        }
}
