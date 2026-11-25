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
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.StoreMatch
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CancelMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CancelMatchResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CostAgreementRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CostAgreementResponseDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.CreateMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.ForfeitMatchRequestDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchCostItemDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.MatchDetailDto
import com.igorwojda.showcase.feature.match.data.datasource.api.model.UpdateMatchRequestDto
import java.util.UUID

internal class MockMatchCommandRetrofitService(
    private val store: ArenaInMemoryStore,
) : MatchCommandRetrofitService {
    override suspend fun createMatch(body: CreateMatchRequestDto): ApiResult<ApiEnvelope<MatchDetailDto>> =
        store.executeApi("/api/v1/matches") { snapshot ->
            val team =
                snapshot.teams.firstOrNull { it.id == body.teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }
            if (team.status == "INACTIVE") {
                return@executeApi store.apiError(422, "TEAM_015", "Đội đang tạm ngưng hoạt động")
            }
            if (team.captainId != snapshot.currentUserId) {
                return@executeApi store.apiError(403, "TEAM_002", "Chỉ đội trưởng được thực hiện")
            }
            if (team.reputationScore < MIN_REPUTATION_SCORE) {
                return@executeApi store.apiError(422, "TEAM_017", "Điểm uy tín dưới 40, tạm khóa tạo kèo")
            }
            val activeCount =
                snapshot.matches.count {
                    it.hostTeamId == team.id &&
                        it.effectiveStatus(snapshot) in listOf("OPEN", "CONFIRMED")
                }
            if (activeCount >= MAX_ACTIVE_MATCHES) {
                return@executeApi store.apiError(422, "MATCH_017", "Đội đã đạt giới hạn kèo đang hoạt động")
            }
            val sport =
                snapshot.sports.firstOrNull { it.id == body.sportId }
                    ?: return@executeApi store.apiError(404, "SPORT_001", "Không tìm thấy bộ môn")
            if (!sport.active) {
                return@executeApi store.apiError(422, "SPORT_002", "Bộ môn hiện không được hỗ trợ")
            }

            val isHasVenue = body.type != "NEED_VENUE"
            if (isHasVenue) {
                val venue =
                    snapshot.venues.firstOrNull { it.id == body.venueId }
                        ?: return@executeApi store.apiError(404, "VENUE_001", "Không tìm thấy địa điểm")
                if (venue.status != "ACTIVE") {
                    return@executeApi store.apiError(422, "VENUE_002", "Địa điểm đã ngừng hoạt động")
                }
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val startIso = body.startsAt ?: body.timeOptions?.firstOrNull()?.startsAt ?: "${nowIso.take(10)}T19:00:00"
            val endIso = body.endsAt ?: body.timeOptions?.firstOrNull()?.endsAt ?: "${nowIso.take(10)}T20:30:00"
            val matchId = "match-${UUID.randomUUID().toString().take(8)}"
            val newMatch =
                StoreMatch(
                    id = matchId,
                    kind = body.type,
                    hostTeamId = body.teamId,
                    format = PitchFormat.fromApiName(body.format) ?: PitchFormat.SEVEN_A_SIDE,
                    skillLevel = SkillLevel.fromApiLabel(body.skillLevels.firstOrNull()) ?: SkillLevel.INTERMEDIATE,
                    venueId = body.venueId,
                    proposedVenues = body.venueOptionIds.orEmpty(),
                    proposedTimeSlots =
                        body.timeOptions.orEmpty().map {
                            "${it.startsAt.drop(
                                11,
                            ).take(5)} - ${it.endsAt.drop(11).take(5)}"
                        },
                    matchDate = startIso.take(10),
                    startTime = if (isHasVenue) startIso.drop(11).take(5) else null,
                    endTime = if (isHasVenue) endIso.drop(11).take(5) else null,
                    expiresAt = body.expiresAt,
                    status = "OPEN",
                    totalCost = body.venueAmount ?: 0,
                    costSharing = body.costSplit ?: "FIFTY_FIFTY",
                    depositAmount = body.depositAmount ?: 0,
                    depositHolder = body.depositHolder,
                    notes = body.matchNature.orEmpty(),
                    createdAt = nowIso,
                )
            store.update { it.copy(matches = listOf(newMatch) + it.matches) }
            ApiResult.Success(ApiEnvelope(newMatch.toDetailDto(store.current)))
        }

    override suspend fun updateMatch(
        matchId: String,
        body: UpdateMatchRequestDto,
    ): ApiResult<ApiEnvelope<MatchDetailDto>> =
        store.executeApi("/api/v1/matches/$matchId") { snapshot ->
            val existing =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            if (existing.effectiveStatus(snapshot) != "OPEN") {
                return@executeApi store.apiError(409, "MATCH_005", "Kèo không còn ở trạng thái cho phép sửa")
            }
            val updated =
                existing.copy(
                    format = PitchFormat.fromApiName(body.format) ?: existing.format,
                    skillLevel = SkillLevel.fromApiLabel(body.skillLevels.firstOrNull()) ?: existing.skillLevel,
                    venueId = body.venueId ?: existing.venueId,
                    matchDate = body.startsAt?.take(10) ?: existing.matchDate,
                    startTime = body.startsAt?.drop(11)?.take(5) ?: existing.startTime,
                    endTime = body.endsAt?.drop(11)?.take(5) ?: existing.endTime,
                    expiresAt = body.expiresAt ?: existing.expiresAt,
                    totalCost = body.venueAmount ?: existing.totalCost,
                    costSharing = body.costSplit ?: existing.costSharing,
                    notes = body.matchNature ?: existing.notes,
                )
            store.update { snap ->
                snap.copy(matches = snap.matches.map { if (it.id == matchId) updated else it })
            }
            ApiResult.Success(ApiEnvelope(updated.toDetailDto(store.current)))
        }

    override suspend fun deleteMatch(matchId: String): ApiResult<Unit> =
        store.executeApi("/api/v1/matches/$matchId") { snapshot ->
            val existing =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            store.update { snap -> snap.copy(matches = snap.matches.filterNot { it.id == existing.id }) }
            ApiResult.Success(Unit)
        }

    override suspend fun updateCostAgreement(
        matchId: String,
        body: CostAgreementRequestDto,
    ): ApiResult<ApiEnvelope<CostAgreementResponseDto>> =
        store.executeApi("/api/v1/matches/$matchId/cost-agreement") { snapshot ->
            val existing =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            if (existing.kind == "NEED_VENUE") {
                return@executeApi store.apiError(422, "MATCH_016", "Thao tác không áp dụng cho loại kèo này")
            }
            val currentStatus = existing.effectiveStatus(snapshot)
            if (currentStatus == "EXPIRED") return@executeApi store.apiError(409, "MATCH_004", "Kèo đã hết hạn")
            if (currentStatus == "CONFIRMED") return@executeApi store.apiError(409, "MATCH_003", "Kèo đã được ghép")
            if (currentStatus !=
                "OPEN"
            ) {
                return@executeApi store.apiError(409, "MATCH_005", "Kèo không còn ở trạng thái cho phép thao tác này")
            }

            val updated =
                existing.copy(
                    totalCost = body.venueAmount ?: 0,
                    costSharing = body.costSplit ?: existing.costSharing,
                )
            store.update { snap ->
                snap.copy(matches = snap.matches.map { if (it.id == matchId) updated else it })
            }
            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            ApiResult.Success(
                ApiEnvelope(
                    CostAgreementResponseDto(
                        matchId = matchId,
                        costType = body.costType,
                        costSplit = body.costSplit,
                        venueAmount = body.venueAmount,
                        drinksAmount = body.drinksAmount,
                        customItems =
                            body.customItems.mapIndexed { idx, item ->
                                MatchCostItemDto(itemId = "item-$idx", itemName = item.itemName, amount = item.amount)
                            },
                        updatedAt = nowIso,
                    ),
                ),
            )
        }

    override suspend fun cancelMatch(
        matchId: String,
        body: CancelMatchRequestDto,
    ): ApiResult<ApiEnvelope<CancelMatchResponseDto>> =
        store.executeApi("/api/v1/matches/$matchId/cancel") { snapshot ->
            val existing =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            val currentStatus = existing.effectiveStatus(snapshot)
            if (currentStatus == "EXPIRED") return@executeApi store.apiError(409, "MATCH_004", "Kèo đã hết hạn")
            if (currentStatus in listOf("CANCELLED", "ARCHIVED", "COMPLETED")) {
                return@executeApi store.apiError(409, "MATCH_005", "Kèo không còn ở trạng thái cho phép thao tác này")
            }
            if (currentStatus != body.expectedStatus) {
                return@executeApi store.apiError(409, "MATCH_015", "Trạng thái đã thay đổi, vui lòng tải lại")
            }
            val isParticipant = body.teamId == existing.hostTeamId || (currentStatus == "CONFIRMED" && body.teamId == existing.guestTeamId)
            if (!isParticipant) {
                return@executeApi store.apiError(403, "MATCH_009", "Đội không có quyền với kèo này")
            }
            if (currentStatus == "OPEN" && existing.kind == "HAS_VENUE") {
                val hasPending = snapshot.matchRequests.any { it.matchId == matchId && it.status == "PENDING" }
                if (hasPending) {
                    return@executeApi store.apiError(422, "MATCH_010", "Vui lòng từ chối tất cả yêu cầu trước khi hủy")
                }
            }
            if (currentStatus == "CONFIRMED") {
                if (body.reason.isNullOrBlank()) {
                    return@executeApi store.apiError(422, "MATCH_011", "Vui lòng nhập lý do hủy")
                }
                if (isMatchStarted(existing.effectiveStartsAt(), snapshot.mockClockIso)) {
                    return@executeApi store.apiError(422, "MATCH_012", "Trận đã bắt đầu, không thể hủy")
                }
            }

            val nowIso = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val penalty = if (currentStatus == "CONFIRMED") computeReliabilityPenalty(existing.effectiveStartsAt(), nowIso) else 0
            store.update { snap ->
                snap.copy(
                    matches = snap.matches.map { if (it.id == matchId) it.copy(status = "CANCELLED") else it },
                    teams =
                        snap.teams.map { team ->
                            if (team.id == body.teamId && penalty < 0) {
                                team.copy(reputationScore = (team.reputationScore + penalty).coerceAtLeast(0))
                            } else {
                                team
                            }
                        },
                    pairings =
                        snap.pairings.map { p ->
                            if (p.matchId == matchId && p.status == "NEGOTIATING") p.copy(status = "SUPERSEDED") else p
                        },
                )
            }
            ApiResult.Success(
                ApiEnvelope(
                    CancelMatchResponseDto(
                        matchId = matchId,
                        status = "CANCELLED",
                        cancelledByTeamId = body.teamId,
                        cancelledAt = nowIso,
                        cancelReason = body.reason,
                        reliabilityPenalty = penalty,
                    ),
                ),
            )
        }

    override suspend fun forfeitMatch(
        matchId: String,
        body: ForfeitMatchRequestDto,
    ): ApiResult<Unit> =
        store.executeApi("/api/v1/matches/$matchId/forfeit") { snapshot ->
            val existing =
                snapshot.matches.firstOrNull { it.id == matchId }
                    ?: return@executeApi store.apiError(404, "MATCH_001", "Không tìm thấy kèo")
            if (existing.effectiveStatus(snapshot) != "CONFIRMED") {
                return@executeApi store.apiError(409, "MATCH_005", "Kèo không còn ở trạng thái cho phép thao tác này")
            }
            store.update { snap ->
                snap.copy(
                    matches = snap.matches.map { if (it.id == matchId) it.copy(status = "CANCELLED") else it },
                    teams =
                        snap.teams.map { team ->
                            if (team.id == body.teamId) {
                                team.copy(reputationScore = (team.reputationScore - 10).coerceAtLeast(0))
                            } else {
                                team
                            }
                        },
                )
            }
            ApiResult.Success(Unit)
        }
}
