package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplyMercenaryUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = ApplyMercenaryUseCase(matchRepository)

    @Test
    fun `invoke calls repository applyMercenary`() =
        runTest {
            val post = mockk<MercenaryPost>()
            val expected = Result.Success(post)
            coEvery { matchRepository.applyMercenary("p1", "msg") } returns expected

            val actual = sut("p1", "msg")

            assertEquals(expected, actual)
        }
}
