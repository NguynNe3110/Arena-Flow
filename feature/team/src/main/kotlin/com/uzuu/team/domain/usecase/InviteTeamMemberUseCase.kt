package com.uzuu.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.team.domain.model.TeamMember
import com.uzuu.team.domain.repository.TeamRepository

internal class InviteTeamMemberUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        name: String,
        position: String,
    ): Result<TeamMember> = teamRepository.inviteTeamMember(teamId, name, position)
}
