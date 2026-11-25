package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.MyInvitation
import com.igorwojda.showcase.feature.team.domain.repository.TeamMemberRepository

internal class GetMyInvitationsUseCase(
    private val teamMemberRepository: TeamMemberRepository,
) {
    suspend operator fun invoke(
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<MyInvitation>> = teamMemberRepository.getMyInvitations(page = page, pageSize = pageSize)
}
