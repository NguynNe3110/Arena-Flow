package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPage
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryPostFilter
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMercenaryPostsUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = GetMercenaryPostsUseCase(mercenaryRepository)

    @Test
    fun `invoke returns repository page`() =
        runTest {
            val expected = Result.Success(MercenaryPage(emptyList(), 1, 20, 1, 0))
            coEvery { mercenaryRepository.getMercenaryPosts(any()) } returns expected

            val actual = sut(MercenaryPostFilter())

            assertEquals(expected, actual)
        }
}
