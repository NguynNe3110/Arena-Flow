package com.igorwojda.showcase.feature.admin.domain.usecase

import com.igorwojda.showcase.feature.admin.domain.repository.AdminRepository
import com.igorwojda.showcase.feature.base.domain.result.Result

/** Unbans a previously banned user account. */
internal class UnbanUserUseCase(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(userId: String): Result<Unit> = adminRepository.unbanUser(userId)
}
