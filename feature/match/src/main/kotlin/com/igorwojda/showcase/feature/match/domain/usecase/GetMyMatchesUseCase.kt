package com.igorwojda.showcase.feature.match.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.match.domain.model.MatchPage
import com.igorwojda.showcase.feature.match.domain.repository.MatchRepository

/** Fetches matches created by or paired with the acting team. */
class GetMyMatchesUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        teamId: String? = null,
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<MatchPage> = matchRepository.getMyMatches(teamId = teamId, status = status, page = page, pageSize = pageSize)
}
