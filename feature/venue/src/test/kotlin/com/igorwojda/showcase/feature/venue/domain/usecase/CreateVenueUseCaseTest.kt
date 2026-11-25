package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.CreateVenueParams
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class CreateVenueUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = CreateVenueUseCase(venueRepository)

    @Test
    fun `when invoked then delegates createVenue to repository`() =
        runTest {
            val params =
                CreateVenueParams(
                    venueName = "Sân Hoa Lư",
                    address = "2 Đinh Tiên Hoàng",
                    latitude = 10.788,
                    longitude = 106.699,
                    wardId = "ward-q1",
                    venueType = PitchFormat.SEVEN_A_SIDE,
                )
            val expected = Result.Failure(AppError.Network)
            coEvery { venueRepository.createVenue(params) } returns expected

            val actual = sut(params)

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.createVenue(params) }
        }
}
