package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RejectVenueUseCaseTest {
    private val adminRepository: AdminRepository = mockk()
    private val sut = RejectVenueUseCase(adminRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            coEvery { adminRepository.rejectVenue("ven-1", "Sai thông tin") } returns Result.Success(Unit)

            val actual = sut("ven-1", "Sai thông tin")

            assertEquals(Result.Success(Unit), actual)
            coVerify { adminRepository.rejectVenue("ven-1", "Sai thông tin") }
        }
}
