package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RejectMercenaryApplicationUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = RejectMercenaryApplicationUseCase(mercenaryRepository)

    @Test
    fun `invoke delegates reject to repository`() =
        runTest {
            coEvery { mercenaryRepository.rejectApplication("rec-1", "app-1") } returns Result.Success(Unit)

            val actual = sut("rec-1", "app-1")

            assertEquals(Result.Success(Unit), actual)
        }
}
