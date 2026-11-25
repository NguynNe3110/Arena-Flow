package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class AcceptInvitationUseCase(
    private val teamMemberRepository: TeamMemberRepository,
) {
    suspend operator fun invoke(invitationId: String): Result<TeamMember> = teamMemberRepository.acceptInvitation(invitationId)
}
