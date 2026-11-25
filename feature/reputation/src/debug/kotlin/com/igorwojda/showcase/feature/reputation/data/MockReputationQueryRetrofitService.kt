@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LabeledExpression", "MagicNumber", "StringLiteralDuplication")

package com.igorwojda.showcase.feature.reputation.data

import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.matchStartsAt
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.penaltyFor
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.reliabilityLevelFor
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.toMatchRatingDto
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.toReportDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeDetailDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeMatchDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeSummaryDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.CompletionStatsDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.CounterpartTeamDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.MatchRatingsResponseDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.RatingSummaryDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.TeamReputationDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.VerificationBadgeDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationQueryRetrofitService
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Debug mock implementation of [ReputationQueryRetrofitService] backed by [ArenaInMemoryStore]. */
internal class MockReputationQueryRetrofitService(
    private val store: ArenaInMemoryStore,
) : ReputationQueryRetrofitService {
    override suspend fun getMatchRatings(
        matchId: String,
        teamId: String,
    ): ApiResult<ApiEnvelope<MatchRatingsResponseDto>> =
        store.executeApi("/api/v1/matches/$matchId/ratings") { snapshot ->
            val match =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }

            val givenReview =
                snapshot.reviews.firstOrNull {
                    it.matchId == matchId && it.reviewerType == "TEAM" && it.reviewerId == teamId
                }
            val receivedReview =
                givenReview?.let {
                    snapshot.reviews.firstOrNull {
                        it.matchId == matchId && it.reviewerType == "TEAM" && it.revieweeId == teamId
                    }
                }

            ApiResult.Success(
                ApiEnvelope(
                    MatchRatingsResponseDto(
                        matchId = matchId,
                        teamId = teamId,
                        given = givenReview?.toMatchRatingDto(match),
                        received = receivedReview?.toMatchRatingDto(match),
                    ),
                ),
            )
        }

    override suspend fun getTeamReports(
        teamId: String,
        direction: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<ReportDto>> =
        store.executeApi("/api/v1/teams/$teamId/reports") { snapshot ->
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }

            val filtered =
                snapshot.reports
                    .filter { report ->
                        report.reporterType == "TEAM" &&
                            if (direction.equals("SENT", ignoreCase = true)) {
                                report.reporterId == teamId
                            } else {
                                report.reportedId == teamId
                            }
                    }.sortedByDescending { it.createdAt }
                    .map { it.toReportDto(snapshot, teamId) }

            val safePage = page.coerceAtLeast(1)
            val safeSize = pageSize.coerceIn(1, 100)
            val fromIndex = ((safePage - 1) * safeSize).coerceAtMost(filtered.size)
            val toIndex = (fromIndex + safeSize).coerceAtMost(filtered.size)
            val totalPages = if (filtered.isEmpty()) 1 else ceil(filtered.size / safeSize.toDouble()).toInt()

            ApiResult.Success(
                PaginatedEnvelope(
                    data = filtered.subList(fromIndex, toIndex),
                    pagination =
                        PaginationDto(
                            page = safePage,
                            pageSize = safeSize,
                            totalPages = totalPages,
                            totalItems = filtered.size,
                        ),
                ),
            )
        }

    override suspend fun getTeamReputation(teamId: String): ApiResult<ApiEnvelope<TeamReputationDto>> =
        store.executeApi("/api/v1/teams/$teamId/reputation") { snapshot ->
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")

            val teamReviews = snapshot.reviews.filter { it.revieweeId == teamId && !it.noShow }
            val avgRating =
                if (teamReviews.isNotEmpty()) {
                    ((teamReviews.map { it.stars }.average()) * 10.0).roundToInt() / 10.0
                } else if (team.ratingAvg > 0.0) {
                    team.ratingAvg
                } else {
                    null
                }

            val counted = team.matchCount
            val rate = team.completionRate
            val completed =
                if (rate != null && counted > 0) {
                    ((rate / 100.0) * counted).roundToInt().coerceIn(0, counted)
                } else {
                    counted
                }
            val isNewcomer = counted < 5
            val captainHasPhone = snapshot.users[team.captainId]?.phone?.isNotBlank() == true
            val isVerified = captainHasPhone && completed > 5 && (avgRating ?: 0.0) > 3.0 && team.reputationScore >= 70

            ApiResult.Success(
                ApiEnvelope(
                    TeamReputationDto(
                        teamId = team.id,
                        reliabilityScore = team.reputationScore,
                        reliabilityLevel = reliabilityLevelFor(team.reputationScore),
                        completion =
                            CompletionStatsDto(
                                completedMatches = completed,
                                countedMatches = counted,
                                completionRate = if (isNewcomer) null else team.completionRate ?: 100,
                                isNewcomer = isNewcomer,
                            ),
                        rating =
                            RatingSummaryDto(
                                averageRating = avgRating,
                                ratingCount = if (teamReviews.isNotEmpty()) teamReviews.size else counted,
                            ),
                        verification =
                            VerificationBadgeDto(
                                isVerified = isVerified,
                                verifiedAt = if (isVerified) team.createdAt else null,
                            ),
                    ),
                ),
            )
        }

    override suspend fun getAdminDisputes(
        status: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<AdminDisputeSummaryDto>> =
        store.executeApi("/api/v1/admin/disputes") { snapshot ->
            val disputedReports =
                snapshot.reports.filter { report ->
                    report.reporterType == "TEAM" &&
                        report.type == "NOSHOW" &&
                        report.disputeNote != null &&
                        when (status.uppercase()) {
                            "PENDING" -> report.status == "DISPUTED"
                            "RESOLVED" -> report.status == "CONFIRMED" || report.status == "REJECTED"
                            else -> true
                        }
                }
            val items =
                disputedReports.map { report ->
                    val reporter = snapshot.teams.firstOrNull { it.id == report.reporterId }
                    val reported = snapshot.teams.firstOrNull { it.id == report.reportedId }
                    val match = snapshot.matches.firstOrNull { it.id == report.matchId }
                    AdminDisputeSummaryDto(
                        disputeId = "disp-${report.id}",
                        reportId = report.id,
                        matchId = report.matchId ?: "",
                        matchStartsAt = matchStartsAt(match),
                        reporterTeam = CounterpartTeamDto(report.reporterId, reporter?.name ?: "Đội report", null, reporter?.status),
                        reportedTeam = CounterpartTeamDto(report.reportedId, reported?.name ?: "Đội bị report", null, reported?.status),
                        reportedById = reporter?.captainId ?: "",
                        reportedByName = store.displayUserName(reporter?.captainId, viewerIsAdmin = true),
                        isReportedByDeleted = snapshot.users[reporter?.captainId]?.isDeleted == true,
                        disputedById = reported?.captainId ?: "",
                        disputedByName = store.displayUserName(reported?.captainId, viewerIsAdmin = true),
                        isDisputedByDeleted = snapshot.users[reported?.captainId]?.isDeleted == true,
                        status = if (report.status == "DISPUTED") "PENDING" else "RESOLVED",
                        decision = if (report.status == "CONFIRMED" || report.status == "REJECTED") report.status else null,
                        isResolvedByDeleted = false,
                        reportCreatedAt = report.createdAt,
                        createdAt = report.createdAt,
                    )
                }
            ApiResult.Success(
                PaginatedEnvelope(
                    data = items,
                    pagination = PaginationDto(page = page, pageSize = pageSize, totalPages = 1, totalItems = items.size),
                ),
            )
        }

    override suspend fun getAdminDisputeDetail(disputeId: String): ApiResult<ApiEnvelope<AdminDisputeDetailDto>> =
        store.executeApi("/api/v1/admin/disputes/$disputeId") { snapshot ->
            val reportId = disputeId.removePrefix("disp-")
            val report =
                snapshot.reports.firstOrNull { it.id == reportId }
                    ?: return@executeApi store.apiError(404, "DISPUTE_001", "Không tìm thấy tranh chấp")
            val reporter = snapshot.teams.firstOrNull { it.id == report.reporterId }
            val reported = snapshot.teams.firstOrNull { it.id == report.reportedId }
            val match = snapshot.matches.firstOrNull { it.id == report.matchId }
            val venue = snapshot.venues.firstOrNull { it.id == match?.venueId }
            val ratings =
                snapshot.reviews
                    .filter { it.matchId == report.matchId && it.reviewerType == "TEAM" }
                    .map { it.toMatchRatingDto(match) }

            ApiResult.Success(
                ApiEnvelope(
                    AdminDisputeDetailDto(
                        disputeId = disputeId,
                        reportId = report.id,
                        matchId = report.matchId ?: "",
                        matchStartsAt = matchStartsAt(match),
                        reporterTeam = CounterpartTeamDto(report.reporterId, reporter?.name ?: "Đội report", null, reporter?.status),
                        reportedTeam = CounterpartTeamDto(report.reportedId, reported?.name ?: "Đội bị report", null, reported?.status),
                        reportedById = reporter?.captainId ?: "",
                        reportedByName = store.displayUserName(reporter?.captainId, viewerIsAdmin = true),
                        isReportedByDeleted = snapshot.users[reporter?.captainId]?.isDeleted == true,
                        disputedById = reported?.captainId ?: "",
                        disputedByName = store.displayUserName(reported?.captainId, viewerIsAdmin = true),
                        isDisputedByDeleted = snapshot.users[reported?.captainId]?.isDeleted == true,
                        status = if (report.status == "DISPUTED") "PENDING" else "RESOLVED",
                        decision = if (report.status == "CONFIRMED" || report.status == "REJECTED") report.status else null,
                        isResolvedByDeleted = false,
                        reportCreatedAt = report.createdAt,
                        createdAt = report.createdAt,
                        match =
                            AdminDisputeMatchDto(
                                sportName = "Bóng đá",
                                status = match?.status ?: "COMPLETED",
                                startsAt = matchStartsAt(match),
                                endsAt = matchStartsAt(match),
                                venueName = venue?.name,
                            ),
                        reportReason = report.reason,
                        reliabilityPenalty = penaltyFor(report.type, 0),
                        evidence = report.disputeNote ?: "",
                        resolutionNote = report.adminDecisionNote,
                        reporterTeamReliabilityScore = reporter?.reputationScore ?: 80,
                        reportedTeamReliabilityScore = reported?.reputationScore ?: 80,
                        matchRatings = ratings,
                    ),
                ),
            )
        }
}
