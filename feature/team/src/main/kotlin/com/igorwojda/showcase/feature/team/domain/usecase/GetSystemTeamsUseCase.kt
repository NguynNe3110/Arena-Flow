package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamSummary
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository

internal class GetSystemTeamsUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(
        status: String = "ACTIVE",
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<TeamSummary>> = teamRepository.getTeams(status = status, page = page, pageSize = pageSize)
}
