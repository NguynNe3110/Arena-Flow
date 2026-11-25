package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetAdminUsersUseCaseTest {
    private val adminRepository: AdminRepository = mockk()
    private val sut = GetAdminUsersUseCase(adminRepository)

    @Test
    fun `invoke delegates to repository`() =
        runTest {
            val expected = Result.Success(AdminPage<AdminUserSummary>(emptyList(), 1, 20, 1, 0))
            coEvery { adminRepository.getUsers("Minh", "ACTIVE", 1, 20) } returns expected

            val actual = sut(query = "Minh", status = "ACTIVE", page = 1, pageSize = 20)

            assertEquals(expected, actual)
            coVerify { adminRepository.getUsers("Minh", "ACTIVE", 1, 20) }
        }
}
