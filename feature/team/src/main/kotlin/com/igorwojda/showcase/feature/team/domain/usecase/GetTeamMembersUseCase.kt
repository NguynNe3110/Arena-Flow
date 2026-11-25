package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.TeamMember
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class GetTeamMembersUseCase(
    private val teamMemberRepository: TeamMemberRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<TeamMember>> = teamMemberRepository.getTeamMembers(teamId = teamId, page = page, pageSize = pageSize)
}
