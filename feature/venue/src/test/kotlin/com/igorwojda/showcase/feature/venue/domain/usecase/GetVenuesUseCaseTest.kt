package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueStatus
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetVenuesUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = GetVenuesUseCase(venueRepository)

    @Test
    fun `when invoked then delegates query and returns venues`() =
        runTest {
            val expected =
                Result.Success(
                    listOf(
                        VenueSummary(
                            venueId = "ven-1",
                            venueName = "Sân An Phú",
                            address = "An Khánh",
                            latitude = 10.79,
                            longitude = 106.74,
                            venueType = PitchFormat.SEVEN_A_SIDE,
                            status = VenueStatus.ACTIVE,
                            primaryImageUrl = null,
                            distanceKm = 1.2,
                            lastSelectedAt = null,
                        ),
                    ),
                )
            coEvery { venueRepository.getVenues("An Phú", null, null, null, 1, 20) } returns expected

            val actual = sut(query = "An Phú")

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.getVenues("An Phú", null, null, null, 1, 20) }
        }
}
