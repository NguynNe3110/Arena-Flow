package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetVenueWardsUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = GetVenueWardsUseCase(venueRepository)

    @Test
    fun `when invoked then returns ward options from repository`() =
        runTest {
            val expected =
                Result.Success(
                    listOf(VenueWardOption("ward-ak", "Phường An Khánh", "prov-hcm", "TP. Hồ Chí Minh")),
                )
            coEvery { venueRepository.getWards() } returns expected

            val actual = sut()

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.getWards() }
        }
}
