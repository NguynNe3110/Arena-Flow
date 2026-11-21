package com.uzuu.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.repository.TeamRepository

internal class GetMyTeamUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(): Result<TeamDetail> = teamRepository.getMyTeam()
}
