package com.uzuu.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.match.domain.model.TeamDetail
import com.uzuu.match.domain.repository.MatchRepository

internal class GetMyTeamUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(): Result<TeamDetail> = matchRepository.getMyTeam()
}
