package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class DeleteAccountUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val sut = DeleteAccountUseCase(profileRepository)

    @Test
    fun `when invoked and repository succeeds then returns success`() =
        runTest {
            coEvery { profileRepository.deleteAccount() } returns Result.Success(Unit)

            val actual = sut()

            actual shouldBeEqualTo Result.Success(Unit)
        }

    @Test
    fun `when invoked and captaincy blocks delete then returns domain error`() =
        runTest {
            val expected =
                Result.Failure(
                    AppError.DomainError(
                        httpStatus = 422,
                        code = "USER_003",
                        message = "Còn là đội trưởng",
                    ),
                )
            coEvery { profileRepository.deleteAccount() } returns expected

            val actual = sut()

            actual shouldBeEqualTo expected
        }
}
