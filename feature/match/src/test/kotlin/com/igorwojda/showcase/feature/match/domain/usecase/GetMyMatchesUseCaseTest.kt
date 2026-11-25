package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetMyMatchesUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetMyMatchesUseCase(matchRepository)

    @Test
    fun `when invoked then returns my matches`() =
        runTest {
            val expected = Result.Success(MatchPage(items = emptyList()))
            coEvery { matchRepository.getMyMatches("team-alpha", null, 1, 20) } returns expected

            val actual = sut(teamId = "team-alpha")

            actual shouldBeEqualTo expected
        }
}
