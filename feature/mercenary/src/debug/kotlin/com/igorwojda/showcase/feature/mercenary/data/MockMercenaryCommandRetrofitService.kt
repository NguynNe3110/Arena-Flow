@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "LongMethod", "MagicNumber", "StringLiteralDuplication")

package com.igorwojda.showcase.feature.mercenary.data

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.StoreApplication
import com.igorwojda.showcase.feature.base.data.store.StoreConversation
import com.igorwojda.showcase.feature.base.data.store.StoreRecruitment
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.ApplyMercenaryRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.CreateMercenaryPostRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryApplicationDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.MercenaryPostDetailDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.model.UpdateMercenaryPostRequestDto
import com.igorwojda.showcase.feature.mercenary.data.datasource.api.service.MercenaryCommandRetrofitService

/** Debug mock implementation of [MercenaryCommandRetrofitService] backed by [ArenaInMemoryStore]. */
@Suppress("TooManyFunctions")
internal class MockMercenaryCommandRetrofitService(
    private val store: ArenaInMemoryStore,
) : MercenaryCommandRetrofitService {
    override suspend fun createMercenaryPost(request: CreateMercenaryPostRequestDto): ApiResult<ApiEnvelope<MercenaryPostDetailDto>> =
        store.executeApi(path = "/api/v1/mercenary-posts") { snapshot ->
            val currentUserId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val team =
                snapshot.teams.firstOrNull { it.id == request.teamId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "TEAM_001", "Không tìm thấy đội bóng.")
            validateCaptainTeam<ApiEnvelope<MercenaryPostDetailDto>>(
                team.captainId,
                currentUserId,
                team.status,
            )?.let { return@executeApi it }

            val venue = snapshot.venues.firstOrNull { it.id == request.venueId }
            val position =
                PlayerPosition.fromApiLabel(request.neededPositions.firstOrNull())
                    ?: PlayerPosition.MIDFIELDER
            val skill = SkillLevel.fromApiLabel(request.skillLevel) ?: SkillLevel.INTERMEDIATE
            val datePart = request.matchStartAt.substringBefore("T").ifBlank { "2026-11-23" }
            val timePart = request.matchStartAt.substringAfter("T", "19:00:00").take(5)
            val newId = "rec-${snapshot.recruitments.size + 1}"
            val newRec =
                StoreRecruitment(
                    id = newId,
                    teamId = team.id,
                    position = position,
                    quantity = request.slotsNeeded.coerceIn(1, MAX_SLOTS),
                    approvedCount = 0,
                    status = "OPEN",
                    area = venue?.let { store.wardLabel(it.wardId) } ?: store.wardLabel(team.wardId),
                    wardId = venue?.wardId ?: team.wardId,
                    distanceKm = venue?.distanceKm ?: DEFAULT_DISTANCE_KM,
                    matchDate = datePart,
                    timeSlot = "$timePart - 20:30",
                    venueId = venue?.id ?: request.venueId,
                    skillLevel = skill,
                    note = request.notes,
                    createdAt = VietnamDateTime.nowIso(snapshot.mockClockIso),
                )
            store.update { it.copy(recruitments = listOf(newRec) + it.recruitments) }
            ApiResult.Success(ApiEnvelope(MockMercenaryMapper.toDetailDto(store, store.current, newRec)))
        }

    override suspend fun updateMercenaryPost(
        postId: String,
        request: UpdateMercenaryPostRequestDto,
    ): ApiResult<ApiEnvelope<MercenaryPostDetailDto>> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId") { snapshot ->
            val currentUserId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val rec =
                snapshot.recruitments.firstOrNull { it.id == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_001", "Không tìm thấy tin tuyển.")
            val team =
                snapshot.teams.firstOrNull { it.id == rec.teamId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "TEAM_001", "Không tìm thấy đội bóng.")
            validateCaptainTeam<ApiEnvelope<MercenaryPostDetailDto>>(
                team.captainId,
                currentUserId,
                team.status,
            )?.let { return@executeApi it }

            if (request.slotsNeeded < rec.approvedCount) {
                return@executeApi store.apiError(
                    HTTP_UNPROCESSABLE,
                    "MERCENARY_008",
                    "Số lượng không được thấp hơn số người đã duyệt (${rec.approvedCount}).",
                )
            }
            val newStatus = if (request.slotsNeeded == rec.approvedCount) "FILLED" else "OPEN"
            val datePart = request.matchStartAt.substringBefore("T").ifBlank { rec.matchDate }
            val timePart = request.matchStartAt.substringAfter("T", "19:00:00").take(5)
            val updated =
                rec.copy(
                    position =
                        PlayerPosition.fromApiLabel(request.neededPositions.firstOrNull())
                            ?: rec.position,
                    quantity = request.slotsNeeded,
                    status = newStatus,
                    matchDate = datePart,
                    timeSlot = "$timePart - 20:30",
                    venueId = request.venueId.ifBlank { rec.venueId },
                    skillLevel = SkillLevel.fromApiLabel(request.skillLevel) ?: rec.skillLevel,
                    note = request.notes,
                )
            store.update { current ->
                current.copy(recruitments = current.recruitments.map { if (it.id == postId) updated else it })
            }
            ApiResult.Success(ApiEnvelope(MockMercenaryMapper.toDetailDto(store, store.current, updated)))
        }

    override suspend fun closeMercenaryPost(postId: String): ApiResult<Unit> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId/close") { snapshot ->
            val currentUserId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val rec =
                snapshot.recruitments.firstOrNull { it.id == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_001", "Không tìm thấy tin tuyển.")
            val team = snapshot.teams.firstOrNull { it.id == rec.teamId }
            if (team?.captainId != currentUserId) {
                return@executeApi store.apiError(HTTP_FORBIDDEN, "TEAM_002", "Không phải đội trưởng đội tuyển.")
            }
            store.update { current ->
                current.copy(
                    recruitments =
                        current.recruitments.map {
                            if (it.id == postId) it.copy(status = "CLOSED") else it
                        },
                    applications =
                        current.applications.map {
                            if (it.recruitmentId == postId && it.status == "PENDING") {
                                it.copy(status = "REJECTED")
                            } else {
                                it
                            }
                        },
                )
            }
            ApiResult.Success(Unit)
        }

    override suspend fun applyToMercenaryPost(
        postId: String,
        request: ApplyMercenaryRequestDto,
    ): ApiResult<ApiEnvelope<MercenaryApplicationDto>> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId/apply") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val rec =
                snapshot.recruitments.firstOrNull { it.id == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_001", "Không tìm thấy tin tuyển.")
            if (rec.status != "OPEN") {
                return@executeApi store.apiError(HTTP_UNPROCESSABLE, "MERCENARY_006", "Tin tuyển đã đủ người hoặc đã đóng.")
            }
            val isMember = snapshot.teamMembers.any { it.teamId == rec.teamId && it.userId == userId && it.status == "ACTIVE" }
            if (isMember) {
                return@executeApi store.apiError(HTTP_UNPROCESSABLE, "MERCENARY_010", "Thành viên đội không thể ứng tuyển.")
            }
            val alreadyApplied =
                snapshot.applications.any {
                    it.recruitmentId == postId && it.applicantId == userId && it.status in setOf("PENDING", "APPROVED")
                }
            if (alreadyApplied) {
                return@executeApi store.apiError(HTTP_CONFLICT, "MERCENARY_009", "Bạn đã ứng tuyển tin này rồi.")
            }
            val user = snapshot.users[userId]
            if ((user?.reputationScore ?: 80) < MIN_APPLY_REPUTATION) {
                return@executeApi store.apiError(
                    HTTP_UNPROCESSABLE,
                    "MERCENARY_019",
                    "Điểm uy tín cá nhân quá thấp để ứng tuyển.",
                )
            }

            val appId = "app-${snapshot.applications.size + 1}"
            val convId = "conv-merc-$appId"
            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newApp =
                StoreApplication(
                    id = appId,
                    recruitmentId = postId,
                    applicantId = userId,
                    conversationId = convId,
                    status = "PENDING",
                    message = request.note,
                    createdAt = now,
                )
            val team = snapshot.teams.firstOrNull { it.id == rec.teamId }
            val newConv =
                StoreConversation(
                    id = convId,
                    contextType = "MERCENARY_APPLICATION",
                    contextId = appId,
                    title = "Tuyển ${rec.position.label} · ${team?.name ?: "Đội bóng"}",
                    participantIds = listOfNotNull(team?.captainId, userId),
                    canSend = true,
                    messages = emptyList(),
                )
            store.update { current ->
                current.copy(
                    applications = listOf(newApp) + current.applications,
                    conversations = current.conversations + newConv,
                )
            }
            ApiResult.Success(ApiEnvelope(MockMercenaryMapper.toApplicationDto(store, store.current, newApp, rec)))
        }

    override suspend fun acceptApplication(
        postId: String,
        appId: String,
    ): ApiResult<ApiEnvelope<MercenaryPostDetailDto>> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId/applications/$appId/accept") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val rec =
                snapshot.recruitments.firstOrNull { it.id == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_001", "Không tìm thấy tin tuyển.")
            val team = snapshot.teams.firstOrNull { it.id == rec.teamId }
            if (team?.captainId != userId) {
                return@executeApi store.apiError(HTTP_FORBIDDEN, "TEAM_002", "Không phải đội trưởng.")
            }
            val app =
                snapshot.applications.firstOrNull { it.id == appId && it.recruitmentId == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_002", "Không tìm thấy đơn ứng tuyển.")
            if (app.status != "PENDING") {
                return@executeApi store.apiError(HTTP_CONFLICT, "MERCENARY_012", "Trạng thái đơn đã thay đổi.")
            }
            if (rec.approvedCount >= rec.quantity) {
                return@executeApi store.apiError(HTTP_UNPROCESSABLE, "MERCENARY_006", "Tin tuyển đã đủ số lượng.")
            }

            val newApproved = rec.approvedCount + 1
            val newStatus = if (newApproved >= rec.quantity) "FILLED" else "OPEN"
            val updatedRec = rec.copy(approvedCount = newApproved, status = newStatus)
            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            store.update { current ->
                current.copy(
                    recruitments = current.recruitments.map { if (it.id == postId) updatedRec else it },
                    applications =
                        current.applications.map {
                            if (it.id == appId) it.copy(status = "APPROVED", approvedAt = now) else it
                        },
                )
            }
            ApiResult.Success(ApiEnvelope(MockMercenaryMapper.toDetailDto(store, store.current, updatedRec)))
        }

    override suspend fun rejectApplication(
        postId: String,
        appId: String,
    ): ApiResult<Unit> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId/applications/$appId/reject") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val rec =
                snapshot.recruitments.firstOrNull { it.id == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_001", "Không tìm thấy tin tuyển.")
            val team = snapshot.teams.firstOrNull { it.id == rec.teamId }
            if (team?.captainId != userId) {
                return@executeApi store.apiError(HTTP_FORBIDDEN, "TEAM_002", "Không phải đội trưởng.")
            }
            store.update { current ->
                current.copy(
                    applications =
                        current.applications.map {
                            if (it.id == appId && it.recruitmentId == postId) it.copy(status = "REJECTED") else it
                        },
                )
            }
            ApiResult.Success(Unit)
        }

    override suspend fun withdrawApplication(
        postId: String,
        appId: String,
    ): ApiResult<Unit> =
        store.executeApi(path = "/api/v1/mercenary-posts/$postId/applications/$appId/withdraw") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(HTTP_UNAUTHORIZED, "AUTH_001", "Vui lòng đăng nhập.")
            val app =
                snapshot.applications.firstOrNull { it.id == appId && it.recruitmentId == postId }
                    ?: return@executeApi store.apiError(HTTP_NOT_FOUND, "MERCENARY_002", "Không tìm thấy đơn ứng tuyển.")
            if (app.applicantId != userId) {
                return@executeApi store.apiError(HTTP_FORBIDDEN, "MERCENARY_014", "Bạn không có quyền rút đơn này.")
            }
            val wasApproved = app.status == "APPROVED"
            store.update { current ->
                val updatedRecs =
                    if (wasApproved) {
                        current.recruitments.map { rec ->
                            if (rec.id == postId) {
                                val dec = (rec.approvedCount - 1).coerceAtLeast(0)
                                val nextStatus = if (rec.status == "FILLED") "OPEN" else rec.status
                                rec.copy(approvedCount = dec, status = nextStatus)
                            } else {
                                rec
                            }
                        }
                    } else {
                        current.recruitments
                    }
                val updatedUsers =
                    if (wasApproved) {
                        current.users.mapValues { (id, user) ->
                            if (id == userId) {
                                user.copy(reputationScore = (user.reputationScore - WITHDRAW_PENALTY).coerceAtLeast(0))
                            } else {
                                user
                            }
                        }
                    } else {
                        current.users
                    }
                current.copy(
                    recruitments = updatedRecs,
                    users = updatedUsers,
                    applications =
                        current.applications.map {
                            if (it.id == appId) it.copy(status = "WITHDRAWN") else it
                        },
                )
            }
            ApiResult.Success(Unit)
        }

    private fun <T> validateCaptainTeam(
        captainId: String,
        currentUserId: String,
        teamStatus: String,
    ): ApiResult<T>? =
        when {
            teamStatus == "DISBANDED" -> store.apiError(HTTP_UNPROCESSABLE, "TEAM_016", "Đội đã giải tán.")
            teamStatus == "INACTIVE" -> store.apiError(HTTP_UNPROCESSABLE, "TEAM_015", "Đội đang tạm ngưng.")
            captainId != currentUserId -> store.apiError(HTTP_FORBIDDEN, "TEAM_002", "Không phải đội trưởng.")
            else -> null
        }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
        const val HTTP_UNPROCESSABLE = 422
        const val MAX_SLOTS = 10
        const val DEFAULT_DISTANCE_KM = 2.0
        const val MIN_APPLY_REPUTATION = 30
        const val WITHDRAW_PENALTY = 3
    }
}
