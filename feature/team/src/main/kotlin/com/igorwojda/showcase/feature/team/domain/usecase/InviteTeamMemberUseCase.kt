package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamInvitation
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class InviteTeamMemberUseCase(
    private val teamMemberRepository: TeamMemberRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        userId: String,
    ): Result<TeamInvitation> = teamMemberRepository.inviteMember(teamId = teamId, userId = userId)
}
