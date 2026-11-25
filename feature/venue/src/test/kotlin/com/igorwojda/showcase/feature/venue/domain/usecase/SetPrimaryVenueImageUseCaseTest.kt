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

class SetPrimaryVenueImageUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = SetPrimaryVenueImageUseCase(venueRepository)

    @Test
    fun `when invoked then delegates setPrimaryImage to repository`() =
        runTest {
            val expected = Result.Success(emptyList<VenueImage>())
            coEvery { venueRepository.setPrimaryImage("ven-1", "img-2") } returns expected

            val actual = sut("ven-1", "img-2")

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.setPrimaryImage("ven-1", "img-2") }
        }
}
