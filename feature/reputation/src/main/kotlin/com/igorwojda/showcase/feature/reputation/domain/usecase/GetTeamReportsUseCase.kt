package com.igorwojda.showcase.feature.reputation.domain.usecase

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReportPage
import com.igorwojda.showcase.feature.reputation.domain.repository.ReputationRepository

private const val DEFAULT_PAGE_SIZE = 20

/** Loads paginated reports sent or received by [teamId]. */
class GetTeamReportsUseCase(
    private val reputationRepository: ReputationRepository,
) {
    suspend operator fun invoke(
        teamId: String,
        direction: ReportDirection = ReportDirection.RECEIVED,
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<TeamReportPage> =
        reputationRepository.getTeamReports(
            teamId = teamId,
            direction = direction,
            page = page,
            pageSize = pageSize,
        )
}
