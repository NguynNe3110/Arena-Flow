package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.domain.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMercenaryPostsUseCaseTest {
    private val matchRepository: MatchRepository = mockk()
    private val sut = GetMercenaryPostsUseCase(matchRepository)

    @Test
    fun `invoke calls repository getMercenaryPosts`() = runTest {
        val expected = Result.Success(emptyList<MercenaryPost>())
        coEvery { matchRepository.getMercenaryPosts() } returns expected

        val actual = sut()

        assertEquals(expected, actual)
    }
}
