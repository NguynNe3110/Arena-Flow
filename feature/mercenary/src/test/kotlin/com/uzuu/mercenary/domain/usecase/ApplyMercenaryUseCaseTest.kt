package com.uzuu.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.mercenary.domain.model.MercenaryPost
import com.uzuu.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApplyMercenaryUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = ApplyMercenaryUseCase(mercenaryRepository)

    @Test
    fun `invoke calls repository applyMercenary`() =
        runTest {
            val post = mockk<MercenaryPost>()
            val expected = Result.Success(post)
            coEvery { mercenaryRepository.applyMercenary("p1", "msg") } returns expected

            val actual = sut("p1", "msg")

            assertEquals(expected, actual)
        }
}
