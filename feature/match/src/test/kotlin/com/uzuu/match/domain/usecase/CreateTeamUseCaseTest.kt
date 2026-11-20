package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.model.TeamDraft
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateTeamUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = CreateTeamUseCase(matchRepository)

    @Test
    fun `invoke calls repository createTeam`() = runTest {
        val draft = TeamDraft(name = "Bình Minh FC", ward = "Hải Châu")
        val team = mockk<TeamDetail>()
        val expected = Result.Success(team)
        coEvery { matchRepository.createTeam(draft) } returns expected

        val actual = sut(draft)

        assertEquals(expected, actual)
    }
}
