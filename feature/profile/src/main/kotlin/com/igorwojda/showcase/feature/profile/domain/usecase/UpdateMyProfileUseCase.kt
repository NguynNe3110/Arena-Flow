package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.UpdateProfileParams
import com.igorwojda.showcase.feature.profile.domain.model.UserProfile
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository

class UpdateMyProfileUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(params: UpdateProfileParams): Result<UserProfile> = profileRepository.updateMyProfile(params)
}
