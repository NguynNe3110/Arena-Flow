package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.Province
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetProvincesUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val sut = GetProvincesUseCase(profileRepository)

    @Test
    fun `when invoked then returns provinces from repository`() =
        runTest {
            val expected = Result.Success(listOf(Province("p-1", "TP. Hồ Chí Minh")))
            coEvery { profileRepository.getProvinces() } returns expected

            val actual = sut()

            actual shouldBeEqualTo expected
        }
}
