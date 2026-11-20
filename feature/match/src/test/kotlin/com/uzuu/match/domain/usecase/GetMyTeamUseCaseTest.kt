package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMyTeamUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetMyTeamUseCase(matchRepository)

    @Test
    fun `invoke calls repository getMyTeam`() =
        runTest {
            val team = mockk<TeamDetail>()
            val expected = Result.Success(team)
            coEvery { matchRepository.getMyTeam() } returns expected

            val actual = sut()

            assertEquals(expected, actual)
        }
}
