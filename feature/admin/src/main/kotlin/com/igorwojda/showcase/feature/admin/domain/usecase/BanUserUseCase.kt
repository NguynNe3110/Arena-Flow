package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Bans a user account with a moderation reason. */
internal class BanUserUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(
        userId: String,
        reason: String,
    ): Result<Unit> = adminRepository.banUser(userId = userId, reason = reason)
}
