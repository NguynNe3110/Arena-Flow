package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetMatchesUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetMatchesUseCase(matchRepository)

    @Test
    fun `when invoked then returns match page`() =
        runTest {
            val expected = Result.Success(MatchPage(items = emptyList()))
            val filter = MatchFilter()
            coEvery { matchRepository.getMatches(filter) } returns expected

            val actual = sut(filter)

            actual shouldBeEqualTo expected
        }
}
