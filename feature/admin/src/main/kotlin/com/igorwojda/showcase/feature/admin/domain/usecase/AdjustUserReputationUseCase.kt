package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Adjusts a user's reputation score by a signed delta with an audit reason. */
internal class AdjustUserReputationUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(
        userId: String,
        delta: Int,
        reason: String,
    ): Result<Unit> = adminRepository.adjustReputation(userId = userId, delta = delta, reason = reason)
}
