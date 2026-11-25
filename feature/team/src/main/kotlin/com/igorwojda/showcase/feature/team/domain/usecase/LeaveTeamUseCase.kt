package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class LeaveTeamUseCase(
    private val teamMemberRepository: TeamMemberRepository,
) {
    suspend operator fun invoke(teamId: String): Result<Unit> = teamMemberRepository.leaveTeam(teamId)
}
