package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.Ward
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetWardsByProvinceUseCaseTest {
    private val profileRepository: ProfileRepository = mockk()
    private val sut = GetWardsByProvinceUseCase(profileRepository)

    @Test
    fun `when invoked then returns wards for province from repository`() =
        runTest {
            val expected = Result.Success(listOf(Ward("w-1", "Phường Bến Nghé")))
            coEvery { profileRepository.getWardsByProvince("p-1") } returns expected

            val actual = sut("p-1")

            actual shouldBeEqualTo expected
        }
}
