package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMercenaryPostDetailUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = GetMercenaryPostDetailUseCase(mercenaryRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            val expected = Result.Failure(AppError.NotFound)
            coEvery { mercenaryRepository.getMercenaryPostDetail("rec-1") } returns expected

            val actual = sut("rec-1")

            assertEquals(expected, actual)
        }
}
