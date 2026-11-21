package com.uzuu.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.domain.model.MercenaryPost
import com.uzuu.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMercenaryPostsUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = GetMercenaryPostsUseCase(mercenaryRepository)

    @Test
    fun `invoke calls repository getMercenaryPosts`() =
        runTest {
            val expected = Result.Success(emptyList<MercenaryPost>())
            coEvery { mercenaryRepository.getMercenaryPosts() } returns expected

            val actual = sut()

            assertEquals(expected, actual)
        }
}
