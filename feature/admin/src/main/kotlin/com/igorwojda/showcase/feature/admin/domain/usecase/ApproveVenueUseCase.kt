package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Approves a pending venue so it becomes active. */
internal class ApproveVenueUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(venueId: String): Result<Unit> = adminRepository.approveVenue(venueId)
}
