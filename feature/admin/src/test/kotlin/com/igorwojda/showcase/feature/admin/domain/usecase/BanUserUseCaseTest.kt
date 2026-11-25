package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BanUserUseCaseTest {
    private val adminRepository: AdminRepository = mockk()
    private val sut = BanUserUseCase(adminRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            coEvery { adminRepository.banUser("u1", "Vi phạm") } returns Result.Success(Unit)

            val actual = sut("u1", "Vi phạm")

            assertEquals(Result.Success(Unit), actual)
            coVerify { adminRepository.banUser("u1", "Vi phạm") }
        }
}
