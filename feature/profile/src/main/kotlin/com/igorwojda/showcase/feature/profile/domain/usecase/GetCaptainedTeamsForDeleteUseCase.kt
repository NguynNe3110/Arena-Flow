package com.igorwojda.showcase.feature.profile.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.profile.domain.model.CaptainedTeam
import com.igorwojda.showcase.feature.profile.domain.repository.ProfileRepository

class GetCaptainedTeamsForDeleteUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): Result<List<CaptainedTeam>> = profileRepository.getCaptainedTeams()
}
