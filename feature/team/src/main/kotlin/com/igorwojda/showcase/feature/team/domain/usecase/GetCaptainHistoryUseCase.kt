package com.igorwojda.showcase.feature.team.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.team.domain.model.CaptainHistoryEntry
import com.igorwojda.showcase.feature.team.domain.repository.TeamRepository

internal class GetCaptainHistoryUseCase(
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<List<CaptainHistoryEntry>> = teamRepository.getCaptainHistory(teamId = teamId, page = page, pageSize = pageSize)
}
