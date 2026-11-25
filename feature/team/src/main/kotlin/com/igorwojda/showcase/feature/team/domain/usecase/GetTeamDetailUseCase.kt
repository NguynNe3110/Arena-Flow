package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository

internal class GetTeamDetailUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(teamId: String): Result<TeamDetail> = teamRepository.getTeamDetail(teamId)
}
