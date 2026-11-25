package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Loads the list of pending venues awaiting admin approval. */
internal class GetPendingVenuesUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(): Result<List<AdminPendingVenue>> = adminRepository.getPendingVenues()
}
