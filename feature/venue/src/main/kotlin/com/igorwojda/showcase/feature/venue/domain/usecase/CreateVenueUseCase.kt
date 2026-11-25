package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.CreateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository

class CreateVenueUseCase(
    private val venueRepository: VenueRepository,
) {
    suspend operator fun invoke(params: CreateVenueParams): Result<VenueDetail> = venueRepository.createVenue(params)
}
