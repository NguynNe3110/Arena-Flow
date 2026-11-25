package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CloseMercenaryPostUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = CloseMercenaryPostUseCase(mercenaryRepository)

    @Test
    fun `invoke closes post via repository`() =
        runTest {
            coEvery { mercenaryRepository.closeMercenaryPost("rec-1") } returns Result.Success(Unit)

            val actual = sut("rec-1")

            assertEquals(Result.Success(Unit), actual)
        }
}
