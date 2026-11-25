package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository

class DeleteVenueImageUseCase(
    private val venueRepository: VenueRepository,
) {
    suspend operator fun invoke(
        venueId: String,
        imageId: String,
    ): Result<List<VenueImage>> = venueRepository.deleteVenueImage(venueId, imageId)
}
