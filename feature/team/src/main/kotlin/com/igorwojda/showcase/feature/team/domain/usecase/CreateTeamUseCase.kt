package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamDetail
import com.igorwojda.showcase.feature.team.domain.model.TeamDraft
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository

internal class CreateTeamUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(draft: TeamDraft): Result<TeamDetail> = teamRepository.createTeam(draft)
}
