package com.uzuu.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.domain.model.TeamDetail
import com.uzuu.team.domain.model.TeamDraft
import com.uzuu.team.domain.repository.TeamRepository

internal class CreateTeamUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(draft: TeamDraft): Result<TeamDetail> = teamRepository.createTeam(draft)
}
