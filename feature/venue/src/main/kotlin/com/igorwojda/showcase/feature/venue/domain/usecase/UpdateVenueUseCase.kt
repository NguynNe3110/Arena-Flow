package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.UpdateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository

class UpdateVenueUseCase(
    private val venueRepository: VenueRepository,
) {
    suspend operator fun invoke(
        venueId: String,
        params: UpdateVenueParams,
    ): Result<VenueDetail> = venueRepository.updateVenue(venueId, params)
}
