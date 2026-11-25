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

class UploadVenueImageUseCaseTest {
    private val venueRepository: VenueRepository = mockk()
    private val sut = UploadVenueImageUseCase(venueRepository)

    @Test
    fun `when invoked then delegates uploadVenueImage to repository`() =
        runTest {
            val expected =
                Result.Success(
                    VenueImage(
                        imageId = "img-1",
                        venueId = "ven-1",
                        imageUrl = "local://img-1",
                        isPrimary = true,
                        displayOrder = 1,
                        uploadedById = "user-1",
                        createdAt = "2026-11-18T10:00:00",
                    ),
                )
            coEvery { venueRepository.uploadVenueImage("ven-1", "Mặt sân #1", true) } returns expected

            val actual = sut("ven-1", "Mặt sân #1", true)

            actual shouldBeEqualTo expected
            coVerify(exactly = 1) { venueRepository.uploadVenueImage("ven-1", "Mặt sân #1", true) }
        }
}
