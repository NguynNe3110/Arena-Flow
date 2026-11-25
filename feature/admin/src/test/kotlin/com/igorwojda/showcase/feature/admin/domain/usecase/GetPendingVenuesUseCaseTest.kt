package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetPendingVenuesUseCaseTest {
    private val adminRepository: AdminRepository = mockk()
    private val sut = GetPendingVenuesUseCase(adminRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            val expected = Result.Success(emptyList<AdminPendingVenue>())
            coEvery { adminRepository.getPendingVenues() } returns expected

            val actual = sut()

            assertEquals(expected, actual)
            coVerify { adminRepository.getPendingVenues() }
        }
}
