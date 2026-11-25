package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.UpdateVenueParams
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class UpdateVenueUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = UpdateVenueUseCase(venueRepository)

    @Test
    fun `when invoked then delegates updateVenue to repository`() =
        runTest {
            val params = UpdateVenueParams(venueName = "Sân mới")
            val expected = Result.Failure(AppError.Unauthorized)
            coEvery { venueRepository.updateVenue("ven-1", params) } returns expected

            val actual = sut("ven-1", params)

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.updateVenue("ven-1", params) }
        }
}
