package com.igorwojda.showcase.feature.venue.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.venue.domain.model.VenueReport
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.domain.repository.VenueRepository

class ReportVenueUseCase(
    private val venueRepository: VenueRepository,
) {
    suspend operator fun invoke(
        venueId: String,
        reason: VenueReportReason,
        detail: String?,
    ): Result<VenueReport> = venueRepository.reportVenue(venueId, reason, detail)
}
