@file:Suppress(
    "CognitiveComplexMethod",
    "CyclomaticComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "MagicNumber",
    "ReturnCount",
    "StringLiteralDuplication",
    "TooManyFunctions",
)

package com.igorwojda.showcase.feature.team.data.datasource.api.service

import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.StoreInvitation
import com.igorwojda.showcase.feature.base.data.store.StoreTeamMember
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationCandidateDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationCandidateListDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InvitationTeamSummaryDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.InviteMemberRequestDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.MyInvitationDto
import com.igorwojda.showcase.feature.team.data.datasource.api.model.TeamMemberDto
import java.text.Normalizer
import java.util.UUID

internal class MockTeamMemberRetrofitService(
    private val store: ArenaInMemoryStore,
) : TeamMemberRetrofitService {
    override suspend fun getMyInvitations(
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<MyInvitationDto>> =
        store.executeApi("/api/v1/profile/invitations") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val pending =
                snapshot.invitations
                    .filter { it.invitedUserId == userId && it.status == "PENDING" }
                    .mapNotNull { inv ->
                        val team = snapshot.teams.firstOrNull { it.id == inv.teamId } ?: return@mapNotNull null
                        if (team.status == "DISBANDED") return@mapNotNull null
                        MyInvitationDto(
                            invitationId = inv.id,
                            invitedAt = inv.createdAt,
                            team =
                                InvitationTeamSummaryDto(
                                    teamId = team.id,
                                    teamName = team.name,
                                    avatarUrl = null,
                                    skillLevels = team.skillLevels.map { it.label },
                                    status = team.status,
                                    captainName = store.displayUserName(team.captainId),
                                    memberCount =
                                        snapshot.teamMembers.count {
                                            it.teamId == team.id && it.status == "ACTIVE"
                                        },
                                ),
                        )
                    }.sortedWith(compareByDescending<MyInvitationDto> { it.invitedAt }.thenByDescending { it.invitationId })
            val safePage = page.coerceAtLeast(1)
            val safeSize = pageSize.coerceIn(1, 100)
            val totalItems = pending.size
            val totalPages = if (totalItems == 0) 0 else (totalItems + safeSize - 1) / safeSize
            val from = ((safePage - 1) * safeSize).coerceAtMost(totalItems)
            val to = (from + safeSize).coerceAtMost(totalItems)
            ApiResult.Success(
                PaginatedEnvelope(
                    data = pending.subList(from, to),
                    pagination = PaginationDto(safePage, safeSize, totalPages, totalItems),
                ),
            )
        }

    override suspend fun acceptInvitation(invitationId: String): ApiResult<ApiEnvelope<TeamMemberDto>> =
        store.executeApi("/api/v1/invitations/$invitationId/accept") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val inv =
                snapshot.invitations.firstOrNull { it.id == invitationId }
                    ?: return@executeApi store.apiError(404, "TEAM_004", "Không tìm thấy lời mời")
            if (inv.invitedUserId != userId) {
                return@executeApi store.apiError(403, "TEAM_005", "Chỉ người được mời được xử lý lời mời")
            }
            val team =
                snapshot.teams.firstOrNull { it.id == inv.teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }
            if (team.status == "INACTIVE") {
                return@executeApi store.apiError(422, "TEAM_015", "Đội đang tạm ngưng hoạt động")
            }
            if (inv.status != "PENDING") {
                return@executeApi store.apiError(409, "TEAM_006", "Lời mời đã được xử lý")
            }
            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newMember =
                StoreTeamMember(
                    id = inv.id,
                    teamId = inv.teamId,
                    userId = userId,
                    role = "MEMBER",
                    joinedAt = now,
                    status = "ACTIVE",
                )
            store.update { current ->
                val alreadyActive =
                    current.teamMembers.any {
                        it.teamId == inv.teamId && it.userId == userId && it.status == "ACTIVE"
                    }
                current.copy(
                    invitations =
                        current.invitations.map {
                            if (it.id == invitationId) it.copy(status = "ACCEPTED") else it
                        },
                    teamMembers = if (alreadyActive) current.teamMembers else current.teamMembers + newMember,
                )
            }
            ApiResult.Success(
                ApiEnvelope(
                    TeamMemberDto(
                        memberId = newMember.id,
                        teamId = newMember.teamId,
                        userId = newMember.userId,
                        displayName = store.displayUserName(newMember.userId),
                        avatarUrl = null,
                        role = newMember.role,
                        status = newMember.status,
                        joinedAt = newMember.joinedAt,
                    ),
                ),
            )
        }

    override suspend fun rejectInvitation(invitationId: String): ApiResult<Unit> =
        store.executeApi("/api/v1/invitations/$invitationId/reject") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val inv =
                snapshot.invitations.firstOrNull { it.id == invitationId }
                    ?: return@executeApi store.apiError(404, "TEAM_004", "Không tìm thấy lời mời")
            if (inv.invitedUserId != userId) {
                return@executeApi store.apiError(403, "TEAM_005", "Chỉ người được mời được xử lý lời mời")
            }
            val team = snapshot.teams.firstOrNull { it.id == inv.teamId }
            if (team?.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }
            if (team?.status == "INACTIVE") {
                return@executeApi store.apiError(422, "TEAM_015", "Đội đang tạm ngưng hoạt động")
            }
            if (inv.status != "PENDING") {
                return@executeApi store.apiError(409, "TEAM_006", "Lời mời đã được xử lý")
            }
            store.update { current ->
                current.copy(
                    invitations =
                        current.invitations.map {
                            if (it.id == invitationId) it.copy(status = "REJECTED") else it
                        },
                )
            }
            ApiResult.Success(Unit)
        }

    override suspend fun getTeamMembers(
        teamId: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<TeamMemberDto>> =
        store.executeApi("/api/v1/teams/$teamId/members") { snapshot ->
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            val activeMembers =
                if (team.status == "DISBANDED") {
                    emptyList()
                } else {
                    snapshot.teamMembers
                        .filter { it.teamId == teamId && it.status == "ACTIVE" }
                        .sortedWith(
                            compareBy<StoreTeamMember> { if (it.role == "CAPTAIN") 0 else 1 }
                                .thenBy { it.joinedAt }
                                .thenBy { it.userId },
                        ).map { m ->
                            TeamMemberDto(
                                memberId = m.id,
                                teamId = m.teamId,
                                userId = m.userId,
                                displayName = store.displayUserName(m.userId),
                                avatarUrl = null,
                                role = m.role,
                                status = m.status,
                                joinedAt = m.joinedAt,
                                leftAt = m.leftAt,
                            )
                        }
                }
            val safePage = page.coerceAtLeast(1)
            val safeSize = pageSize.coerceIn(1, 100)
            val totalItems = activeMembers.size
            val totalPages = if (totalItems == 0) 0 else (totalItems + safeSize - 1) / safeSize
            val from = ((safePage - 1) * safeSize).coerceAtMost(totalItems)
            val to = (from + safeSize).coerceAtMost(totalItems)
            ApiResult.Success(
                PaginatedEnvelope(
                    data = activeMembers.subList(from, to),
                    pagination = PaginationDto(safePage, safeSize, totalPages, totalItems),
                ),
            )
        }

    override suspend fun searchInvitationCandidates(
        teamId: String,
        query: String,
    ): ApiResult<ApiEnvelope<InvitationCandidateListDto>> =
        store.executeApi("/api/v1/teams/$teamId/invitation-candidates") { snapshot ->
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

            val trimmed = query.trim()
            if (trimmed.isEmpty() || trimmed.length > 100) {
                return@executeApi store.apiError(400, "VALIDATION_ERROR", "Dữ liệu không hợp lệ")
            }
            val isPhoneFormat = trimmed.all { it.isDigit() || it in setOf('+', ' ', '.', '-') }
            val candidates =
                snapshot.users.values.filter { u ->
                    !u.isDeleted && u.id != userId && u.role != AccountRole.ADMIN
                }

            val matched =
                if (isPhoneFormat) {
                    val digits = trimmed.replace(Regex("[\\s.-]"), "")
                    val normalizedPhone =
                        when {
                            digits.startsWith("+84") -> "0" + digits.removePrefix("+84")
                            digits.startsWith("84") -> "0" + digits.removePrefix("84")
                            else -> digits
                        }
                    if (!Regex("^0\\d{9}$").matches(normalizedPhone)) {
                        return@executeApi store.apiError(400, "USER_005", "Số điện thoại không hợp lệ")
                    }
                    candidates.filter { it.phone == normalizedPhone }
                } else {
                    val normalizedQ = normalizeText(trimmed)
                    val queryTokens = normalizedQ.split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
                    val alnumCount = queryTokens.sumOf { it.length }
                    if (alnumCount < 2) {
                        return@executeApi store.apiError(400, "USER_006", "Nhập ít nhất 2 ký tự")
                    }
                    candidates
                        .filter { u ->
                            val nameTokens = normalizeText(u.displayName).split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
                            queryTokens.all { qt -> nameTokens.any { nt -> nt.startsWith(qt) } }
                        }.sortedWith(
                            compareBy<com.igorwojda.showcase.feature.base.data.store.StoreUser> {
                                if (normalizeText(it.displayName).startsWith(normalizedQ)) 0 else 1
                            }.thenBy { it.displayName }.thenBy { it.id },
                        )
                }

            val activeIds =
                snapshot.teamMembers
                    .filter { it.teamId == teamId && it.status == "ACTIVE" }
                    .map { it.userId }
                    .toSet()
            val pendingIds =
                snapshot.invitations
                    .filter { it.teamId == teamId && it.status == "PENDING" }
                    .map { it.invitedUserId }
                    .toSet()

            val limited =
                matched.take(20).map { u ->
                    val status =
                        when {
                            activeIds.contains(u.id) -> "ACTIVE"
                            pendingIds.contains(u.id) -> "PENDING"
                            else -> null
                        }
                    InvitationCandidateDto(
                        userId = u.id,
                        displayName = u.displayName.ifBlank { u.phone },
                        avatarUrl = null,
                        membershipStatus = status,
                    )
                }
            ApiResult.Success(ApiEnvelope(InvitationCandidateListDto(items = limited, hasMore = matched.size > 20)))
        }

    override suspend fun inviteMember(
        teamId: String,
        body: InviteMemberRequestDto,
    ): ApiResult<ApiEnvelope<InvitationDto>> =
        store.executeApi("/api/v1/teams/$teamId/invitations") { snapshot ->
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
            val targetUser = snapshot.users[body.userId]
            if (targetUser == null || targetUser.isDeleted) {
                return@executeApi store.apiError(404, "USER_001", "Không tìm thấy người dùng")
            }
            val isAlreadyActive =
                snapshot.teamMembers.any {
                    it.teamId == teamId && it.userId == body.userId && it.status == "ACTIVE"
                }
            val hasPendingInvite =
                snapshot.invitations.any {
                    it.teamId == teamId && it.invitedUserId == body.userId && it.status == "PENDING"
                }
            if (isAlreadyActive || hasPendingInvite) {
                return@executeApi store.apiError(409, "TEAM_003", "Người này đã là thành viên hoặc đã được mời")
            }
            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            val newInv =
                StoreInvitation(
                    id = "inv-${UUID.randomUUID().toString().take(8)}",
                    teamId = teamId,
                    invitedUserId = body.userId,
                    invitedBy = userId,
                    status = "PENDING",
                    message = "Mời bạn gia nhập ${team.name}",
                    createdAt = now,
                )
            store.update { current ->
                current.copy(invitations = listOf(newInv) + current.invitations)
            }
            ApiResult.Success(
                ApiEnvelope(
                    InvitationDto(
                        invitationId = newInv.id,
                        teamId = teamId,
                        teamName = team.name,
                        userId = body.userId,
                        status = "PENDING",
                        invitedAt = now,
                    ),
                ),
            )
        }

    override suspend fun leaveTeam(teamId: String): ApiResult<Unit> =
        store.executeApi("/api/v1/teams/$teamId/members/leave") { snapshot ->
            val userId =
                snapshot.currentUserId
                    ?: return@executeApi store.apiError(401, "AUTH_001", "Token không hợp lệ hoặc đã hết hạn")
            val team =
                snapshot.teams.firstOrNull { it.id == teamId }
                    ?: return@executeApi store.apiError(404, "TEAM_001", "Không tìm thấy đội")
            if (team.status == "DISBANDED") {
                return@executeApi store.apiError(422, "TEAM_016", "Đội đã giải tán")
            }
            val membership =
                snapshot.teamMembers.firstOrNull {
                    it.teamId == teamId && it.userId == userId && it.status == "ACTIVE"
                } ?: return@executeApi store.apiError(422, "TEAM_010", "Bạn không phải thành viên của đội")
            if (membership.role == "CAPTAIN" || team.captainId == userId) {
                return@executeApi store.apiError(422, "TEAM_009", "Đội trưởng phải chuyển quyền trước khi rời đội")
            }
            val now = VietnamDateTime.nowIso(snapshot.mockClockIso)
            store.update { current ->
                current.copy(
                    teamMembers =
                        current.teamMembers.map { m ->
                            if (m.id == membership.id) m.copy(status = "LEFT", leftAt = now) else m
                        },
                )
            }
            ApiResult.Success(Unit)
        }

    private fun normalizeText(input: String): String {
        val normalized = Normalizer.normalize(input.lowercase().replace('đ', 'd'), Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }
}
