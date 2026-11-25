@file:Suppress(
    "CognitiveComplexMethod",
    "ComplexCondition",
    "CyclomaticComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "MagicNumber",
    "StringLiteralDuplication",
)

package com.igorwojda.showcase.feature.reputation.data

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.StoreReport
import com.igorwojda.showcase.feature.base.data.store.StoreReview
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.penaltyFor
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.toMatchRatingDto
import com.igorwojda.showcase.feature.reputation.data.MockReputationStoreHelper.toReportDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeDetailDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.DisputeCreatedDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.DisputeReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.MatchRatingDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ReportDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.ResolveDisputeRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitChatReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitMatchRatingRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.model.SubmitMatchReportRequestDto
import com.igorwojda.showcase.feature.reputation.data.datasource.api.service.ReputationCommandRetrofitService
import java.util.UUID

/** Debug mock implementation of [ReputationCommandRetrofitService] backed by [ArenaInMemoryStore]. */
internal class MockReputationCommandRetrofitService(
    private val store: ArenaInMemoryStore,
) : ReputationCommandRetrofitService {
    override suspend fun submitMatchRating(
        matchId: String,
        request: SubmitMatchRatingRequestDto,
    ): ApiResult<ApiEnvelope<MatchRatingDto>> =
        store.executeApi("/api/v1/matches/$matchId/ratings") { snapshot ->
            val match =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            val actingTeamId =
                snapshot.actingTeamId
                    ?: return@executeApi store.apiError(403, "TEAM_002", "Chỉ đội trưởng được thực hiện")

            val alreadyRated =
                snapshot.reviews.any {
                    it.matchId == matchId && it.reviewerType == "TEAM" && it.reviewerId == actingTeamId
                }
            if (alreadyRated) {
                return@executeApi store.apiError(409, "RATING_002", "Bạn đã đánh giá trận này")
            }
            if (!request.isNoShow && (request.score == null || request.score !in 1..5)) {
                return@executeApi store.apiError(400, "VALIDATION_ERROR", "Vui lòng chọn từ 1 đến 5 sao")
            }

            val rateeTeamId =
                if (match.hostTeamId == actingTeamId) {
                    match.guestTeamId ?: actingTeamId
                } else {
                    match.hostTeamId
                }
            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val scoreGiven =
                if (!request.isNoShow && request.raterTeamScore != null && request.rateeTeamScore != null) {
                    "${request.raterTeamScore}-${request.rateeTeamScore}"
                } else {
                    null
                }
            val newReview =
                StoreReview(
                    id = "rev-${UUID.randomUUID().toString().take(8)}",
                    matchId = matchId,
                    reviewerId = actingTeamId,
                    revieweeId = rateeTeamId,
                    reviewerType = "TEAM",
                    stars = request.score ?: 0,
                    comment = request.comment?.trim().orEmpty(),
                    noShow = request.isNoShow,
                    scoreGiven = scoreGiven,
                    createdAt = nowIso,
                )

            val isFirstRating = snapshot.reviews.none { it.matchId == matchId && it.reviewerType == "TEAM" }
            store.update { current ->
                val updatedTeams =
                    current.teams.map { team ->
                        var delta = 0
                        if (isFirstRating && (team.id == actingTeamId || (!request.isNoShow && team.id == rateeTeamId))) {
                            delta += 2
                        }
                        if (!request.isNoShow && request.score == 5 && team.id == rateeTeamId) {
                            delta += 1
                        }
                        if (delta > 0) {
                            team.copy(reputationScore = (team.reputationScore + delta).coerceIn(0, 100))
                        } else {
                            team
                        }
                    }
                val updatedMatches =
                    current.matches.map { item ->
                        if (item.id == matchId) item.copy(status = "COMPLETED") else item
                    }
                current.copy(
                    teams = updatedTeams,
                    matches = updatedMatches,
                    reviews = current.reviews + newReview,
                )
            }

            ApiResult.Success(ApiEnvelope(newReview.toMatchRatingDto(match, matchStatus = "COMPLETED")))
        }

    override suspend fun submitMatchReport(
        matchId: String,
        request: SubmitMatchReportRequestDto,
    ): ApiResult<ApiEnvelope<ReportDto>> =
        store.executeApi("/api/v1/matches/$matchId/reports") { snapshot ->
            val match =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            val actingTeamId =
                snapshot.actingTeamId
                    ?: return@executeApi store.apiError(403, "TEAM_002", "Chỉ đội trưởng được thực hiện")
            val trimmedReason = request.reason.trim()
            if (trimmedReason.isEmpty()) {
                return@executeApi store.apiError(400, "VALIDATION_ERROR", "Lý do report không được để trống")
            }
            val reportedTeamId =
                if (match.hostTeamId == actingTeamId) {
                    match.guestTeamId ?: actingTeamId
                } else {
                    match.hostTeamId
                }
            if (reportedTeamId == actingTeamId) {
                return@executeApi store.apiError(422, "REPORT_005", "Bạn là đội trưởng của cả hai đội, không thể report trận này")
            }
            val duplicate =
                snapshot.reports.any {
                    it.matchId == matchId &&
                        it.reporterType == "TEAM" &&
                        it.reporterId == actingTeamId &&
                        it.type == request.reportType
                }
            if (duplicate) {
                return@executeApi store.apiError(409, "REPORT_004", "Bạn đã report đội này với loại này trong trận")
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val priorCount =
                snapshot.reports.count {
                    it.reporterType == "TEAM" && it.reportedId == reportedTeamId && it.type == request.reportType
                }
            val penalty = penaltyFor(request.reportType, priorCount)
            val newReport =
                StoreReport(
                    id = "rep-${UUID.randomUUID().toString().take(8)}",
                    reporterType = "TEAM",
                    reporterId = actingTeamId,
                    reportedId = reportedTeamId,
                    matchId = matchId,
                    type = request.reportType,
                    reason = trimmedReason,
                    status = "AUTO_ACCEPTED",
                    createdAt = nowIso,
                    canDisputeUntil = if (request.reportType == "NOSHOW") "2026-11-19T23:59:59" else null,
                )

            store.update { current ->
                val updatedTeams =
                    current.teams.map { team ->
                        if (team.id == reportedTeamId) {
                            team.copy(reputationScore = (team.reputationScore - penalty).coerceIn(0, 100))
                        } else {
                            team
                        }
                    }
                current.copy(
                    teams = updatedTeams,
                    reports = current.reports + newReport,
                )
            }

            ApiResult.Success(ApiEnvelope(newReport.toReportDto(store.current, actingTeamId)))
        }

    override suspend fun disputeReport(
        reportId: String,
        request: DisputeReportRequestDto,
    ): ApiResult<ApiEnvelope<DisputeCreatedDto>> =
        store.executeApi("/api/v1/reports/$reportId/dispute") { snapshot ->
            val report =
                snapshot.reports.firstOrNull { it.id == reportId }
                    ?: return@executeApi store.apiError(404, "REPORT_001", "Không tìm thấy report")
            if (report.type != "NOSHOW") {
                return@executeApi store.apiError(422, "REPORT_006", "Chỉ tranh chấp được report bùng")
            }
            if (report.status != "AUTO_ACCEPTED") {
                return@executeApi store.apiError(409, "REPORT_008", "Report đã được tranh chấp hoặc đã có quyết định")
            }
            val evidence = request.evidence.trim()
            if (evidence.isEmpty()) {
                return@executeApi store.apiError(400, "VALIDATION_ERROR", "Bằng chứng không được để trống")
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            store.update { current ->
                current.copy(
                    reports =
                        current.reports.map { item ->
                            if (item.id == reportId) {
                                item.copy(status = "DISPUTED", disputeNote = evidence)
                            } else {
                                item
                            }
                        },
                )
            }

            ApiResult.Success(
                ApiEnvelope(
                    DisputeCreatedDto(
                        disputeId = "disp-$reportId",
                        reportId = reportId,
                        status = "PENDING",
                        evidence = evidence,
                        reportStatus = "DISPUTED",
                        createdAt = nowIso,
                    ),
                ),
            )
        }

    override suspend fun resolveAdminDispute(
        disputeId: String,
        request: ResolveDisputeRequestDto,
    ): ApiResult<ApiEnvelope<AdminDisputeDetailDto>> =
        store.executeApi("/api/v1/admin/disputes/$disputeId/resolve") { snapshot ->
            val reportId = disputeId.removePrefix("disp-")
            val report =
                snapshot.reports.firstOrNull { it.id == reportId }
                    ?: return@executeApi store.apiError(404, "DISPUTE_001", "Không tìm thấy tranh chấp")
            if (report.status != "DISPUTED") {
                return@executeApi store.apiError(409, "DISPUTE_002", "Tranh chấp đã được xử lý")
            }

            val penalty = penaltyFor(report.type, 0)
            store.update { current ->
                val updatedTeams =
                    if (request.decision == "REJECTED") {
                        current.teams.map { team ->
                            if (team.id == report.reportedId) {
                                team.copy(reputationScore = (team.reputationScore + penalty).coerceIn(0, 100))
                            } else {
                                team
                            }
                        }
                    } else {
                        current.teams
                    }
                current.copy(
                    teams = updatedTeams,
                    reports =
                        current.reports.map { item ->
                            if (item.id == reportId) {
                                item.copy(status = request.decision, adminDecisionNote = request.note.trim())
                            } else {
                                item
                            }
                        },
                )
            }
            val updated = store.current
            val updatedReport = updated.reports.first { it.id == reportId }
            val reporter = updated.teams.firstOrNull { it.id == updatedReport.reporterId }
            val reported = updated.teams.firstOrNull { it.id == updatedReport.reportedId }
            val match = updated.matches.firstOrNull { it.id == updatedReport.matchId }
            val venue = updated.venues.firstOrNull { it.id == match?.venueId }
            val nowIso = VietnamDateTime.nowIso(updated.mockClockIso)
            ApiResult.Success(
                ApiEnvelope(
                    com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeDetailDto(
                        disputeId = disputeId,
                        reportId = updatedReport.id,
                        matchId = updatedReport.matchId ?: "",
                        matchStartsAt = MockReputationStoreHelper.matchStartsAt(match),
                        reporterTeam =
                            com.igorwojda.showcase.feature.reputation.data.datasource.api.model.CounterpartTeamDto(
                                updatedReport.reporterId,
                                reporter?.name ?: "Đội report",
                                null,
                                reporter?.status,
                            ),
                        reportedTeam =
                            com.igorwojda.showcase.feature.reputation.data.datasource.api.model.CounterpartTeamDto(
                                updatedReport.reportedId,
                                reported?.name ?: "Đội bị report",
                                null,
                                reported?.status,
                            ),
                        reportedById = reporter?.captainId ?: "",
                        reportedByName = store.displayUserName(reporter?.captainId, viewerIsAdmin = true),
                        isReportedByDeleted = updated.users[reporter?.captainId]?.isDeleted == true,
                        disputedById = reported?.captainId ?: "",
                        disputedByName = store.displayUserName(reported?.captainId, viewerIsAdmin = true),
                        isDisputedByDeleted = updated.users[reported?.captainId]?.isDeleted == true,
                        status = "RESOLVED",
                        decision = request.decision,
                        resolvedById = updated.currentUserId,
                        resolvedByName = store.displayUserName(updated.currentUserId, viewerIsAdmin = true),
                        isResolvedByDeleted = false,
                        resolvedAt = nowIso,
                        reportCreatedAt = updatedReport.createdAt,
                        createdAt = updatedReport.createdAt,
                        match =
                            com.igorwojda.showcase.feature.reputation.data.datasource.api.model.AdminDisputeMatchDto(
                                sportName = "Bóng đá",
                                status = match?.status ?: "COMPLETED",
                                startsAt = MockReputationStoreHelper.matchStartsAt(match),
                                endsAt = MockReputationStoreHelper.matchStartsAt(match),
                                venueName = venue?.name,
                            ),
                        reportReason = updatedReport.reason,
                        reliabilityPenalty = penalty,
                        evidence = updatedReport.disputeNote ?: "",
                        resolutionNote = updatedReport.adminDecisionNote,
                        reporterTeamReliabilityScore = reporter?.reputationScore ?: 80,
                        reportedTeamReliabilityScore = reported?.reputationScore ?: 80,
                        matchRatings = emptyList(),
                    ),
                ),
            )
        }

    override suspend fun submitChatReport(
        conversationId: String,
        request: SubmitChatReportRequestDto,
    ): ApiResult<ApiEnvelope<ReportDto>> =
        store.executeApi("/api/v1/conversations/$conversationId/reports") { snapshot ->
            val conversation =
                snapshot.conversations.firstOrNull { it.id == conversationId }
                    ?: return@executeApi store.apiError(404, "CHAT_002", "Không tìm thấy cuộc trò chuyện")
            val actingTeamId =
                snapshot.actingTeamId
                    ?: return@executeApi store.apiError(403, "TEAM_002", "Chỉ đội trưởng được thực hiện")
            if (conversation.messages.isEmpty()) {
                return@executeApi store.apiError(422, "REPORT_010", "Chỉ report qua chat được khi hai đội đã nhắn tin")
            }
            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newReport =
                StoreReport(
                    id = "rep-${UUID.randomUUID().toString().take(8)}",
                    reporterType = "TEAM",
                    reporterId = actingTeamId,
                    reportedId = conversation.participantIds.firstOrNull { it != actingTeamId } ?: "team-beta",
                    matchId = conversation.contextId,
                    type = "BAD_COMMUNICATION",
                    reason = request.reason.trim(),
                    status = "AUTO_ACCEPTED",
                    createdAt = nowIso,
                )
            store.update { current ->
                current.copy(reports = current.reports + newReport)
            }
            ApiResult.Success(ApiEnvelope(newReport.toReportDto(store.current, actingTeamId)))
        }
}
