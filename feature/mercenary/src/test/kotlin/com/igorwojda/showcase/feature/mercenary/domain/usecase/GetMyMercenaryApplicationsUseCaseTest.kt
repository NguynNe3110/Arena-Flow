package com.igorwojda.showcase.feature.mercenary.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.mercenary.domain.model.MercenaryApplication
import com.igorwojda.showcase.feature.mercenary.domain.repository.MercenaryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetMyMercenaryApplicationsUseCaseTest {
    private val mercenaryRepository: MercenaryRepository = mockk()
    private val sut = GetMyMercenaryApplicationsUseCase(mercenaryRepository)

    @Test
    fun `invoke returns player applications`() =
        runTest {
            val expected = Result.Success(emptyList<MercenaryApplication>())
            coEvery { mercenaryRepository.getMyApplications() } returns expected

            val actual = sut()

            assertEquals(expected, actual)
        }
}
