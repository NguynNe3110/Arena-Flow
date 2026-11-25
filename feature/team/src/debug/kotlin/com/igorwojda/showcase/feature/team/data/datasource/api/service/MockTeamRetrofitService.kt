@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "LongMethod", "MagicNumber", "StringLiteralDuplication", "TooManyFunctions")

package com.igorwojda.showcase.feature.team.data.datasource.api.service

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.FieldValidationErrorDto
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreCaptainHistory
import com.igorwojda.showcase.feature.base.data.store.StoreTeam
import com.igorwojda.showcase.feature.base.data.store.StoreTeamMember
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CaptainHistoryDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CaptainTransferDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.CreateTeamRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.DisbandTeamRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.MyTeamDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamSummaryDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TransferCaptainRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.UpdateTeamRequestDto
import java.util.UUID

internal class MockTeamRetrofitService(
    private val store: ArenaInMemoryStore,
) : TeamRetrofitService {
    override suspend fun createTeam(body: CreateTeamRequestDto): ApiResult<ApiEnvelope<TeamDto>> =
        store.executeApi("/api/v1/teams") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val user = snapshot.users[userId]
            if (user == null || user.isDeleted) {
                return@executeApi store.apiError(404, "USER_001", "Không tìm thấy người dùng")
            }
            val cleanName = body.teamName.trim()
            if (cleanName.isEmpty() || cleanName.length > 100) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "VALIDATION_ERROR",
                    message = "Dữ liệu không hợp lệ",
                    validationErrors = listOf(FieldValidationErrorDto("teamName", "Tên đội phải từ 1 đến 100 ký tự")),
                )
            }
            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newTeamId = "team-${UUID.randomUUID().toString().take(8)}"
            val short =
                cleanName
                    .split(Regex("\\s+"))
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .joinToString("")
                    .take(3)
                    .ifBlank { "FC" }
            val skills =
                body.skillLevels
                    .distinct()
                    .take(3)
                    .mapNotNull { SkillLevel.fromApiLabel(it) }
            val newTeam =
                StoreTeam(
                    id = newTeamId,
                    name = cleanName,
                    shortName = short,
                    description = body.description?.trim().orEmpty(),
                    captainId = userId,
                    wardId = user.wardId ?: "ward-ak",
                    status = "ACTIVE",
                    skillLevels = skills,
                    reputationScore = 100,
                    ratingAvg = 5.0,
                    completionRate = null,
                    matchCount = 0,
                    createdAt = now,
                )
            store.update { current ->
                current.copy(
                    teams = listOf(newTeam) + current.teams,
                    teamMembers =
                        current.teamMembers +
                            StoreTeamMember(
                                id = "tm-${UUID.randomUUID().toString().take(8)}",
                                teamId = newTeamId,
                                userId = userId,
                                role = "CAPTAIN",
                                joinedAt = now,
                                status = "ACTIVE",
                            ),
                    captainHistory =
                        listOf(
                            StoreCaptainHistory(
                                id = "ch-${UUID.randomUUID().toString().take(8)}",
                                teamId = newTeamId,
                                previousCaptainId = null,
                                newCaptainId = userId,
                                changedAt = now,
                                note = "",
                            ),
                        ) + current.captainHistory,
                    actingTeamId = newTeamId,
                )
            }
            ApiResult.Success(ApiEnvelope(newTeam.toDto(store.current)))
        }

    override suspend fun getTeams(
        status: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<TeamSummaryDto>> =
        store.executeApi("/api/v1/teams") { snapshot ->
            val filtered =
                snapshot.teams
                    .filter { status == "ALL" || it.status.equals(status, ignoreCase = true) }
                    .sortedWith(compareByDescending<StoreTeam> { it.createdAt }.thenByDescending { it.id })
            val paged = paginate(filtered, page, pageSize)
            ApiResult.Success(
                PaginatedEnvelope(
                    data = paged.items.map { it.toSummaryDto(snapshot) },
                    pagination = paged.pagination,
                ),
            )
        }

    override suspend fun getTeamDetail(teamId: String): ApiResult<ApiEnvelope<TeamDto>> =
        store.executeApi("/api/v1/teams/$teamId") { snapshot ->
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            ApiResult.Success(ApiEnvelope(team.toDto(snapshot)))
        }

    override suspend fun updateTeam(
        teamId: String,
        body: UpdateTeamRequestDto,
    ): ApiResult<ApiEnvelope<TeamDto>> =
        store.executeApi("/api/v1/teams/$teamId") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }
            if (team.status == "INACTIVE") {
                return@executeApi store.apiError(422, "TEAM_015", "Đội đang tạm ngưng hoạt động")
            }
            if (team.captainId != userId) {
                return@executeApi store.apiError(403, "TEAM_002", "Chỉ đội trưởng được thực hiện")
            }
            val updatedName = body.teamName?.trim() ?: team.name
            if (updatedName.isEmpty() || updatedName.length > 100) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "VALIDATION_ERROR",
                    message = "Dữ liệu không hợp lệ",
                    validationErrors = listOf(FieldValidationErrorDto("teamName", "Tên đội phải từ 1 đến 100 ký tự")),
                )
            }
            val updatedSkills =
                body.skillLevels
                    ?.distinct()
                    ?.take(3)
                    ?.mapNotNull { SkillLevel.fromApiLabel(it) }
                    ?: team.skillLevels
            val updatedTeam =
                team.copy(
                    name = updatedName,
                    description = body.description?.trim() ?: team.description,
                    skillLevels = updatedSkills,
                )
            store.update { current ->
                current.copy(teams = current.teams.map { if (it.id == teamId) updatedTeam else it })
            }
            ApiResult.Success(ApiEnvelope(updatedTeam.toDto(store.current)))
        }

    override suspend fun disbandTeam(
        teamId: String,
        body: DisbandTeamRequestDto,
    ): ApiResult<ApiEnvelope<TeamDto>> =
        store.executeApi("/api/v1/teams/$teamId/disband") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }
            if (team.captainId != userId) {
                return@executeApi store.apiError(403, "TEAM_002", "Chỉ đội trưởng được thực hiện")
            }
            val rawReason = body.reason
            if (rawReason != null && rawReason.length > 100) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "VALIDATION_ERROR",
                    message = "Dữ liệu không hợp lệ",
                    validationErrors = listOf(FieldValidationErrorDto("reason", "Lý do tối đa 100 ký tự")),
                )
            }
            val finalReason = rawReason?.trim()?.ifBlank { null } ?: "Đội trưởng giải tán đội"
            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val disbandedTeam =
                team.copy(
                    status = "DISBANDED",
                    disbandReason = finalReason,
                    disbandedAt = now,
                )
            store.update { current ->
                current.copy(
                    teams = current.teams.map { if (it.id == teamId) disbandedTeam else it },
                    teamMembers =
                        current.teamMembers.map { m ->
                            if (m.teamId == teamId && m.status == "ACTIVE") m.copy(status = "LEFT", leftAt = now) else m
                        },
                    invitations =
                        current.invitations.map { inv ->
                            if (inv.teamId == teamId && inv.status == "PENDING") inv.copy(status = "REJECTED") else inv
                        },
                )
            }
            val dto =
                disbandedTeam.toDto(store.current).copy(
                    captainId = null,
                    captainName = null,
                    memberCount = 0,
                    disbandedById = userId,
                    disbandedByName = store.displayUserName(userId),
                )
            ApiResult.Success(ApiEnvelope(dto))
        }

    override suspend fun getMyTeams(
        role: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MyTeamDto>> =
        store.executeApi("/api/v1/profile/teams") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val activeMemberships =
                snapshot.teamMembers
                    .filter { it.userId == userId && it.status == "ACTIVE" }
                    .associateBy { it.teamId }
            val myTeams =
                snapshot.teams
                    .filter { it.status != "DISBANDED" && activeMemberships.containsKey(it.id) }
                    .mapNotNull { team ->
                        val membership = activeMemberships[team.id] ?: return@mapNotNull null
                        if (role != null && !membership.role.equals(role, ignoreCase = true)) {
                            return@mapNotNull null
                        }
                        MyTeamDto(
                            teamId = team.id,
                            teamName = team.name,
                            avatarUrl = null,
                            skillLevels = team.skillLevels.map { it.label },
                            status = team.status,
                            captainName = store.displayUserName(team.captainId),
                            memberCount = snapshot.teamMembers.count { it.teamId == team.id && it.status == "ACTIVE" },
                            role = membership.role,
                            joinedAt = membership.joinedAt,
                        )
                    }.sortedWith(
                        compareBy<MyTeamDto> { if (it.role == "CAPTAIN") 0 else 1 }
                            .thenByDescending { it.joinedAt }
                            .thenByDescending { it.teamId },
                    )
            val paged = paginate(myTeams, page, pageSize)
            ApiResult.Success(PaginatedEnvelope(data = paged.items, pagination = paged.pagination))
        }

    override suspend fun transferCaptain(
        teamId: String,
        body: TransferCaptainRequestDto,
    ): ApiResult<ApiEnvelope<CaptainTransferDto>> =
        store.executeApi("/api/v1/teams/$teamId/captain/transfer") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }
            if (team.status == "INACTIVE") {
                return@executeApi store.apiError(422, "TEAM_015", "Đội đang tạm ngưng hoạt động")
            }
            if (team.captainId != userId) {
                return@executeApi store.apiError(403, "TEAM_002", "Chỉ đội trưởng được thực hiện")
            }
            if (body.newCaptainId == userId) {
                return@executeApi store.apiError(422, "TEAM_008", "Không thể chuyển quyền cho chính mình")
            }
            val targetMember =
                snapshot.teamMembers.firstOrNull {
                    it.teamId == teamId && it.userId == body.newCaptainId && it.status == "ACTIVE"
                } ?: return@executeApi store.apiError(422, "TEAM_007", "Chỉ chuyển quyền cho thành viên đang hoạt động")

            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val cleanReason = body.reason?.trim()?.ifBlank { null }
            store.update { current ->
                current.copy(
                    teams = current.teams.map { if (it.id == teamId) it.copy(captainId = targetMember.userId) else it },
                    teamMembers =
                        current.teamMembers.map { m ->
                            if (m.teamId == teamId && m.status == "ACTIVE") {
                                m.copy(role = if (m.userId == targetMember.userId) "CAPTAIN" else "MEMBER")
                            } else {
                                m
                            }
                        },
                    captainHistory =
                        listOf(
                            StoreCaptainHistory(
                                id = "ch-${UUID.randomUUID().toString().take(8)}",
                                teamId = teamId,
                                previousCaptainId = userId,
                                newCaptainId = targetMember.userId,
                                changedAt = now,
                                note = cleanReason.orEmpty(),
                            ),
                        ) + current.captainHistory,
                )
            }
            ApiResult.Success(
                ApiEnvelope(
                    CaptainTransferDto(
                        teamId = teamId,
                        fromUserId = userId,
                        toUserId = targetMember.userId,
                        reason = cleanReason,
                        transferredAt = now,
                    ),
                ),
            )
        }

    override suspend fun getCaptainHistory(
        teamId: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<CaptainHistoryDto>> =
        store.executeApi("/api/v1/teams/$teamId/captain/history") { snapshot ->
            if (snapshot.teams.none { it.id == teamId }) {
                return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            }
            val entries =
                snapshot.captainHistory
                    .filter { it.teamId == teamId }
                    .sortedWith(compareByDescending<StoreCaptainHistory> { it.changedAt }.thenByDescending { it.id })
                    .map { ch ->
                        CaptainHistoryDto(
                            historyId = ch.id,
                            teamId = ch.teamId,
                            fromUserId = ch.previousCaptainId,
                            fromUserName = ch.previousCaptainId?.let { store.displayUserName(it) },
                            toUserId = ch.newCaptainId,
                            toUserName = store.displayUserName(ch.newCaptainId),
                            reason = ch.note.ifBlank { null },
                            transferredAt = ch.changedAt,
                        )
                    }
            val paged = paginate(entries, page, pageSize)
            ApiResult.Success(PaginatedEnvelope(data = paged.items, pagination = paged.pagination))
        }

    private fun StoreTeam.toDto(snapshot: ArenaStoreSnapshot): TeamDto {
        val isDisbanded = status == "DISBANDED"
        val activeMemberCount =
            if (isDisbanded) 0 else snapshot.teamMembers.count { it.teamId == id && it.status == "ACTIVE" }
        return TeamDto(
            teamId = id,
            teamName = name,
            avatarUrl = null,
            description = description.ifBlank { null },
            skillLevels = skillLevels.map { it.label },
            status = status,
            inactiveReason = inactiveReason,
            captainId = if (isDisbanded) null else captainId,
            captainName = if (isDisbanded) null else store.displayUserName(captainId),
            memberCount = activeMemberCount,
            createdBy = captainId,
            createdAt = createdAt,
            updatedAt = createdAt,
            disbandedAt = disbandedAt,
            disbandedById = if (isDisbanded) captainId else null,
            disbandedByName = if (isDisbanded) store.displayUserName(captainId) else null,
            disbandReason = disbandReason,
        )
    }

    private fun StoreTeam.toSummaryDto(snapshot: ArenaStoreSnapshot): TeamSummaryDto {
        val isDisbanded = status == "DISBANDED"
        val activeMemberCount =
            if (isDisbanded) 0 else snapshot.teamMembers.count { it.teamId == id && it.status == "ACTIVE" }
        return TeamSummaryDto(
            teamId = id,
            teamName = name,
            avatarUrl = null,
            skillLevels = skillLevels.map { it.label },
            status = status,
            captainName = if (isDisbanded) null else store.displayUserName(captainId),
            memberCount = activeMemberCount,
            createdAt = createdAt,
            disbandedByName = if (isDisbanded) store.displayUserName(captainId) else null,
        )
    }

    private data class PagedSlice<T>(
        val items: List<T>,
        val pagination: PaginationDto,
    )

    private fun <T> paginate(
        all: List<T>,
        page: Int,
        pageSize: Int,
    ): PagedSlice<T> {
        val safePage = page.coerceAtLeast(1)
        val safeSize = pageSize.coerceIn(1, 100)
        val totalItems = all.size
        val totalPages = if (totalItems == 0) 0 else (totalItems + safeSize - 1) / safeSize
        val from = ((safePage - 1) * safeSize).coerceAtMost(totalItems)
        val to = (from + safeSize).coerceAtMost(totalItems)
        return PagedSlice(
            items = all.subList(from, to),
            pagination = PaginationDto(page = safePage, pageSize = safeSize, totalPages = totalPages, totalItems = totalItems),
        )
    }
}
