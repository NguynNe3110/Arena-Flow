package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository

class DeleteAccountUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): Result<Unit> = profileRepository.deleteAccount()
}
