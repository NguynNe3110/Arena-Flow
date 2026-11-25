package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository

class UploadVenueImageUseCase(
    private val venueRepository: VenueRepository,
) {
    suspend operator fun invoke(
        venueId: String,
        imageLabel: String,
        isPrimary: Boolean = false,
    ): Result<VenueImage> = venueRepository.uploadVenueImage(venueId, imageLabel, isPrimary)
}
