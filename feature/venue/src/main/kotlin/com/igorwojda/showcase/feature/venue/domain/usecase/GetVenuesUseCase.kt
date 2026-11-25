@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository

class GetVenuesUseCase(
    private val venueRepository: VenueRepository,
) {
    suspend operator fun invoke(
        query: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        radiusKm: Double? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<VenueSummary>> =
        venueRepository.getVenues(
            query = query,
            lat = lat,
            lng = lng,
            radiusKm = radiusKm,
            page = page,
            pageSize = pageSize,
        )
}
