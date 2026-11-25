package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.Province
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository

class GetProvincesUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): Result<List<Province>> = profileRepository.getProvinces()
}
