package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class RejectInvitationUseCase(
    private val teamMemberRepository: TeamMemberRepository,
) {
    suspend operator fun invoke(invitationId: String): Result<Unit> = teamMemberRepository.rejectInvitation(invitationId)
}
