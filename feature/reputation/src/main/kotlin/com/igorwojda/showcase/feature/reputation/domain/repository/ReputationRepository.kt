package com.igorwojda.showcase.feature.reputation.domain.repository

import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.reputation.domain.model.DisputeSubmission
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRating
import com.igorwojda.showcase.feature.reputation.domain.model.MatchRatings
import com.igorwojda.showcase.feature.reputation.domain.model.ReportDirection
import com.igorwojda.showcase.feature.reputation.domain.model.ReportType
import com.igorwojda.showcase.feature.reputation.domain.model.SubmitRatingParams
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReport
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReportPage
import com.igorwojda.showcase.feature.reputation.domain.model.TeamReputation

/** Repository contract for post-match ratings, violation reports, disputes, and team reputation. */
interface ReputationRepository {
    suspend fun getMatchRatings(
        matchId: String,
        teamId: String,
    ): Result<MatchRatings>

    suspend fun submitMatchRating(params: SubmitRatingParams): Result<MatchRating>

    suspend fun submitMatchReport(
        matchId: String,
        reportType: ReportType,
        reason: String,
    ): Result<TeamReport>

    suspend fun submitChatReport(
        conversationId: String,
        reason: String,
    ): Result<TeamReport>

    suspend fun getTeamReports(
        teamId: String,
        direction: ReportDirection = ReportDirection.RECEIVED,
        page: Int = 1,
        pageSize: Int = 20,
    ): Result<TeamReportPage>

    suspend fun disputeReport(
        reportId: String,
        evidence: String,
    ): Result<DisputeSubmission>

    suspend fun getTeamReputation(teamId: String): Result<TeamReputation>
}
