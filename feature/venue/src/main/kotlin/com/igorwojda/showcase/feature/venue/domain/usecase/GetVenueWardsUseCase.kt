package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository

class GetVenueWardsUseCase(
    private val venueRepository: VenueRepository,
) {
    suspend operator fun invoke(): Result<List<VenueWardOption>> = venueRepository.getWards()
}
