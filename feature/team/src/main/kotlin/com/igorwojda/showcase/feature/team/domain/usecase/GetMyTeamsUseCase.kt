package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.MyTeam
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository

internal class GetMyTeamsUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(
        role: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<MyTeam>> = teamRepository.getMyTeams(role = role, page = page, pageSize = pageSize)
}
