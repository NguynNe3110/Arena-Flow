package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.TeamMember
import com.uzuu.match.domain.repository.MatchRepository

internal class InviteTeamMemberUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(teamId: String, name: String, position: String): Result<TeamMember> =
        matchRepository.inviteTeamMember(teamId, name, position)
}
