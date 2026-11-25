@file:Suppress(
    "CognitiveComplexMethod",
    "CyclomaticComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "MagicNumber",
    "ReturnCount",
    "StringLiteralDuplication",
)

package com.igorwojda.showcase.feature.match.data.datasource.api.service

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.StoreConversation
import com.igorwojda.showcase.feature.base.data.store.StoreMatchRequest
import com.igorwojda.showcase.feature.match.data.datasource.api.model.AcceptMatchRequestResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.RejectMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.SendMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.WithdrawMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.WithdrawMatchRequestResponseDto
import java.util.UUID

internal class MockMatchRequestRetrofitService(
    private val store: ArenaInMemoryStore,
) : MatchRequestRetrofitService {
    override suspend fun sendMatchRequest(
        matchId: String,
        body: SendMatchRequestDto,
    ): ApiResult<ApiEnvelope<MatchRequestDto>> =
        store.executeApi("/api/v1/matches/$matchId/requests") { snapshot ->
            val match =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            if (match.kind == "NEED_VENUE") {
                return@executeApi store.apiError(422, "MATCH_016", "Thao tác không áp dụng cho loại kèo này")
            }
            val currentStatus = match.effectiveStatus(snapshot)
            if (currentStatus == "CONFIRMED") return@executeApi store.apiError(409, "MATCH_003", "Kèo đã được ghép")
            if (currentStatus == "EXPIRED") return@executeApi store.apiError(409, "MATCH_004", "Kèo đã hết hạn")
            if (currentStatus !=
                "OPEN"
            ) {
                return@executeApi store.apiError(409, "MATCH_005", "Kèo không còn ở trạng thái cho phép thao tác này")
            }

            val team =
                snapshot.teams.firstOrNull { it.id == body.teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            if (team.status == "INACTIVE") return@executeApi store.apiError(422, "TEAM_015", "Đội đang tạm ngưng hoạt động")
            if (team.reputationScore < MIN_REPUTATION_SCORE) {
                return@executeApi store.apiError(422, "TEAM_017", "Điểm uy tín dưới 40, không thể xin kèo")
            }
            if (match.hostTeamId == body.teamId) {
                return@executeApi store.apiError(422, "MATCH_007", "Không thể gửi yêu cầu cho kèo của đội mình")
            }
            val duplicate =
                snapshot.matchRequests.any {
                    it.matchId == matchId && it.requesterTeamId == body.teamId &&
                        it.status in listOf("PENDING", "ACCEPTED", "REJECTED", "SUPERSEDED")
                }
            if (duplicate) {
                return@executeApi store.apiError(409, "MATCH_006", "Đội đã gửi yêu cầu cho kèo này")
            }

            val hostTeam = snapshot.teams.firstOrNull { it.id == match.hostTeamId }
            val existingConv =
                snapshot.conversations.firstOrNull { conv ->
                    conv.contextType == "TEAM_MATCH" &&
                        snapshot.matchRequests.any {
                            it.matchId == matchId && it.requesterTeamId == body.teamId && it.conversationId == conv.id
                        }
                }
            val convId = existingConv?.id ?: "conv-${UUID.randomUUID().toString().take(8)}"
            val requestId = "req-${UUID.randomUUID().toString().take(8)}"
            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newReq =
                StoreMatchRequest(
                    id = requestId,
                    matchId = matchId,
                    requesterTeamId = body.teamId,
                    conversationId = convId,
                    status = "PENDING",
                    message = body.message.orEmpty(),
                    createdAt = nowIso,
                )
            val newConv =
                if (existingConv == null) {
                    StoreConversation(
                        id = convId,
                        contextType = "TEAM_MATCH",
                        contextId = requestId,
                        title = "${hostTeam?.name ?: "Chủ kèo"} × ${team.name}",
                        participantIds = listOfNotNull(hostTeam?.captainId, team.captainId).distinct(),
                        canSend = true,
                        messages = emptyList(),
                    )
                } else {
                    null
                }
            store.update { snap ->
                snap.copy(
                    matchRequests = snap.matchRequests + newReq,
                    conversations = if (newConv != null) snap.conversations + newConv else snap.conversations,
                )
            }
            ApiResult.Success(ApiEnvelope(newReq.toDto(store.current)))
        }

    override suspend fun getMatchRequests(
        matchId: String,
        status: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MatchRequestDto>> =
        store.executeApi("/api/v1/matches/$matchId/requests") { snapshot ->
            val match =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            if (match.kind == "NEED_VENUE") {
                return@executeApi store.apiError(422, "MATCH_016", "Thao tác không áp dụng cho loại kèo này")
            }
            val items =
                snapshot.matchRequests
                    .filter { it.matchId == matchId }
                    .map { it.toDto(snapshot) }
                    .filter { status.isNullOrBlank() || status == "ALL" || it.status.equals(status, ignoreCase = true) }
            ApiResult.Success(
                PaginatedEnvelope(
                    data = items,
                    pagination = PaginationDto(page = 1, pageSize = 20, totalPages = 1, totalItems = items.size),
                ),
            )
        }

    override suspend fun getTeamMatchRequests(
        teamId: String,
        status: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MatchRequestDto>> =
        store.executeApi("/api/v1/teams/$teamId/match-requests") { snapshot ->
            val items =
                snapshot.matchRequests
                    .filter { it.requesterTeamId == teamId }
                    .map { it.toDto(snapshot) }
                    .filter { status.isNullOrBlank() || status == "ALL" || it.status.equals(status, ignoreCase = true) }
            ApiResult.Success(
                PaginatedEnvelope(
                    data = items,
                    pagination = PaginationDto(page = 1, pageSize = 20, totalPages = 1, totalItems = items.size),
                ),
            )
        }

    override suspend fun acceptMatchRequest(requestId: String): ApiResult<ApiEnvelope<AcceptMatchRequestResponseDto>> =
        store.executeApi("/api/v1/match-requests/$requestId/accept") { snapshot ->
            val req =
                snapshot.matchRequests.firstOrNull { it.id == requestId }
                    ?: return@executeApi store.apiError(404, "MATCH_002", "Không tìm thấy yêu cầu ghép kèo")
            val match =
                snapshot.matches.firstOrNull { it.id == req.matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            val matchStatus = match.effectiveStatus(snapshot)
            if (matchStatus == "EXPIRED") return@executeApi store.apiError(409, "MATCH_004", "Kèo đã hết hạn")
            if (matchStatus == "CONFIRMED") return@executeApi store.apiError(409, "MATCH_003", "Kèo đã được ghép")
            if (matchStatus !=
                "OPEN"
            ) {
                return@executeApi store.apiError(409, "MATCH_005", "Kèo không còn ở trạng thái cho phép thao tác này")
            }
            if (req.status != "PENDING") return@executeApi store.apiError(409, "MATCH_008", "Yêu cầu đã được xử lý")

            val requesterTeam = snapshot.teams.firstOrNull { it.id == req.requesterTeamId }
            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            store.update { snap ->
                snap.copy(
                    matches =
                        snap.matches.map {
                            if (it.id == match.id) it.copy(status = "CONFIRMED", guestTeamId = req.requesterTeamId) else it
                        },
                    matchRequests =
                        snap.matchRequests.map {
                            when {
                                it.id == requestId -> it.copy(status = "ACCEPTED")
                                it.matchId == match.id && it.status == "PENDING" -> it.copy(status = "SUPERSEDED")
                                else -> it
                            }
                        },
                )
            }
            ApiResult.Success(
                ApiEnvelope(
                    AcceptMatchRequestResponseDto(
                        matchId = match.id,
                        matchStatus = "CONFIRMED",
                        requestId = requestId,
                        requestStatus = "ACCEPTED",
                        opponentTeamId = req.requesterTeamId,
                        opponentTeamName = requesterTeam?.name ?: "Đội khách",
                        respondedAt = nowIso,
                    ),
                ),
            )
        }

    override suspend fun rejectMatchRequest(
        requestId: String,
        body: RejectMatchRequestDto?,
    ): ApiResult<Unit> =
        store.executeApi("/api/v1/match-requests/$requestId/reject") { snapshot ->
            val req =
                snapshot.matchRequests.firstOrNull { it.id == requestId }
                    ?: return@executeApi store.apiError(404, "MATCH_002", "Không tìm thấy yêu cầu ghép kèo")
            if (req.status != "PENDING") return@executeApi store.apiError(409, "MATCH_008", "Yêu cầu đã được xử lý")
            store.update { snap ->
                snap.copy(
                    matchRequests =
                        snap.matchRequests.map {
                            if (it.id == requestId) it.copy(status = "REJECTED", message = body?.reason ?: it.message) else it
                        },
                )
            }
            ApiResult.Success(Unit)
        }

    override suspend fun withdrawMatchRequest(
        requestId: String,
        body: WithdrawMatchRequestDto,
    ): ApiResult<ApiEnvelope<WithdrawMatchRequestResponseDto>> =
        store.executeApi("/api/v1/match-requests/$requestId/withdraw") { snapshot ->
            val req =
                snapshot.matchRequests.firstOrNull { it.id == requestId }
                    ?: return@executeApi store.apiError(404, "MATCH_002", "Không tìm thấy yêu cầu ghép kèo")
            val match =
                snapshot.matches.firstOrNull { it.id == req.matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            if (req.status !in listOf("PENDING", "ACCEPTED")) {
                return@executeApi store.apiError(409, "MATCH_008", "Yêu cầu đã được xử lý")
            }
            if (req.status != body.expectedStatus) {
                return@executeApi store.apiError(409, "MATCH_015", "Trạng thái đã thay đổi, vui lòng tải lại")
            }
            if (req.status == "ACCEPTED") {
                if (body.reason.isNullOrBlank()) {
                    return@executeApi store.apiError(422, "MATCH_011", "Vui lòng nhập lý do hủy")
                }
                if (isMatchStarted(match.effectiveStartsAt(), snapshot.mockClockIso)) {
                    return@executeApi store.apiError(422, "MATCH_012", "Trận đã bắt đầu, không thể hủy")
                }
            }
            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val penalty = if (req.status == "ACCEPTED") computeReliabilityPenalty(match.effectiveStartsAt(), nowIso) else 0
            val newMatchStatus = if (req.status == "ACCEPTED") "CANCELLED" else match.status
            store.update { snap ->
                snap.copy(
                    matchRequests = snap.matchRequests.map { if (it.id == requestId) it.copy(status = "WITHDRAWN") else it },
                    matches = snap.matches.map { if (it.id == match.id) it.copy(status = newMatchStatus) else it },
                    teams =
                        snap.teams.map { team ->
                            if (team.id == req.requesterTeamId && penalty < 0) {
                                team.copy(reputationScore = (team.reputationScore + penalty).coerceAtLeast(0))
                            } else {
                                team
                            }
                        },
                )
            }
            ApiResult.Success(
                ApiEnvelope(
                    WithdrawMatchRequestResponseDto(
                        requestId = requestId,
                        matchId = match.id,
                        status = "WITHDRAWN",
                        withdrawnAt = nowIso,
                        matchStatus = newMatchStatus,
                        reliabilityPenalty = penalty,
                    ),
                ),
            )
        }
}
