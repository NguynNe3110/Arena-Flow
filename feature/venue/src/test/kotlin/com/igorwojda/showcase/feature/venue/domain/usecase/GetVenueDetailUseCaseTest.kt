package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetVenueDetailUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = GetVenueDetailUseCase(venueRepository)

    @Test
    fun `when invoked then returns detail result from repository`() =
        runTest {
            val expected = Result.Failure(AppError.NotFound)
            coEvery { venueRepository.getVenueDetail("ven-404") } returns expected

            val actual = sut("ven-404")

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.getVenueDetail("ven-404") }
        }
}
