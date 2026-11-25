package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdjustUserReputationUseCaseTest {
    private val adminRepository: AdminRepository = mockk()
    private val sut = AdjustUserReputationUseCase(adminRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            coEvery { adminRepository.adjustReputation("u1", -10, "Phạt") } returns Result.Success(Unit)

            val actual = sut("u1", -10, "Phạt")

            assertEquals(Result.Success(Unit), actual)
            coVerify { adminRepository.adjustReputation("u1", -10, "Phạt") }
        }
}
