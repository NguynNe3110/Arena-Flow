package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.InvitationCandidatePage
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class SearchInvitationCandidatesUseCase(
    private val teamMemberRepository: TeamMemberRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        query: String,
    ): Result<InvitationCandidatePage> = teamMemberRepository.searchInvitationCandidates(teamId = teamId, query = query)
}
