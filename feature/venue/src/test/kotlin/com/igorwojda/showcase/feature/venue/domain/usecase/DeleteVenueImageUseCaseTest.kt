package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class DeleteVenueImageUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = DeleteVenueImageUseCase(venueRepository)

    @Test
    fun `when invoked then delegates deleteVenueImage to repository`() =
        runTest {
            val expected = Result.Success(emptyList<VenueImage>())
            coEvery { venueRepository.deleteVenueImage("ven-1", "img-1") } returns expected

            val actual = sut("ven-1", "img-1")

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.deleteVenueImage("ven-1", "img-1") }
        }
}
