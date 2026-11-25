package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Rejects a pending venue with a moderation reason. */
internal class RejectVenueUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(
        venueId: String,
        reason: String,
    ): Result<Unit> = adminRepository.rejectVenue(venueId = venueId, reason = reason)
}
