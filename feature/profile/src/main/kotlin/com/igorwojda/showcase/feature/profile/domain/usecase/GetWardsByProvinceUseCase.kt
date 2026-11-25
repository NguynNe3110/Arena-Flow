package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.Ward
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository

class GetWardsByProvinceUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(provinceId: String): Result<List<Ward>> = profileRepository.getWardsByProvince(provinceId)
}
