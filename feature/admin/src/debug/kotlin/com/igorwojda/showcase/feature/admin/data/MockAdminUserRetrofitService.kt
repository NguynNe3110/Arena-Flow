@file:Suppress("LabeledExpression", "MagicNumber", "StringLiteralDuplication")

package com.igorwojda.showcase.feature.admin.data

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdjustReputationRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminUserSummaryDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AuditLogDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.BanUserRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminUserRetrofitService
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore

internal class MockAdminUserRetrofitService(
    private val store: ArenaInMemoryStore,
    private val adminState: MockAdminStateHolder,
) : AdminUserRetrofitService {
    override suspend fun getUsers(
        query: String?,
        status: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<AdminUserSummaryDto>> =
        store.executeApi(USERS_PATH) { snapshot ->
            val (_, error) = adminState.requireAdmin<PaginatedEnvelope<AdminUserSummaryDto>>(snapshot, USERS_PATH)
            if (error != null) return@executeApi error

            val bannedMap = adminState.bannedUsers.value
            val normalizedQuery = query?.trim()?.lowercase().orEmpty()
            val users =
                snapshot.users.values
                    .map { user ->
                        val banReason = bannedMap[user.id]
                        val userStatus =
                            when {
                                user.isDeleted -> "DELETED"
                                banReason != null -> "BANNED"
                                else -> "ACTIVE"
                            }
                        AdminUserSummaryDto(
                            userId = user.id,
                            displayName = user.displayName.ifBlank { user.phone },
                            realName = user.realName,
                            phone = user.phone,
                            role = user.role.name,
                            status = userStatus,
                            reputationScore = user.reputationScore,
                            matchCount = user.matchCount,
                            isDeleted = user.isDeleted,
                            banReason = banReason,
                        )
                    }.filter { dto ->
                        val matchesQuery =
                            normalizedQuery.isEmpty() ||
                                dto.displayName.lowercase().contains(normalizedQuery) ||
                                dto.phone.contains(normalizedQuery) ||
                                (dto.realName?.lowercase()?.contains(normalizedQuery) == true)
                        val matchesStatus = status.isNullOrBlank() || dto.status.equals(status, ignoreCase = true)
                        matchesQuery && matchesStatus
                    }

            ApiResult.Success(adminState.paginate(users, page, pageSize))
        }

    override suspend fun banUser(
        userId: String,
        request: BanUserRequestDto,
    ): ApiResult<Unit> =
        store.executeApi("$USERS_PATH/$userId/ban") { snapshot ->
            val (admin, error) = adminState.requireAdmin<Unit>(snapshot, USERS_PATH)
            if (error != null) return@executeApi error
            val actor = requireNotNull(admin)

            val target =
                snapshot.users[userId]
                    ?: return@executeApi store.apiError(
                        httpStatus = 404,
                        code = "USER_001",
                        message = "Không tìm thấy người dùng.",
                        path = "$USERS_PATH/$userId/ban",
                    )

            val reason = request.reason.trim().ifEmpty { "Vi phạm quy định cộng đồng" }
            adminState.bannedUsers.value = adminState.bannedUsers.value + (userId to reason)
            adminState.appendAuditLog(
                actor = actor,
                action = "BAN_USER",
                targetType = "USER",
                targetId = userId,
                targetLabel = target.displayName.ifBlank { target.phone },
                detail = "Khóa tài khoản: $reason",
            )
            ApiResult.Success(Unit)
        }

    override suspend fun unbanUser(userId: String): ApiResult<Unit> =
        store.executeApi("$USERS_PATH/$userId/unban") { snapshot ->
            val (admin, error) = adminState.requireAdmin<Unit>(snapshot, USERS_PATH)
            if (error != null) return@executeApi error
            val actor = requireNotNull(admin)

            val target =
                snapshot.users[userId]
                    ?: return@executeApi store.apiError(
                        httpStatus = 404,
                        code = "USER_001",
                        message = "Không tìm thấy người dùng.",
                        path = "$USERS_PATH/$userId/unban",
                    )

            adminState.bannedUsers.value = adminState.bannedUsers.value - userId
            adminState.appendAuditLog(
                actor = actor,
                action = "UNBAN_USER",
                targetType = "USER",
                targetId = userId,
                targetLabel = target.displayName.ifBlank { target.phone },
                detail = "Mở khóa tài khoản người dùng.",
            )
            ApiResult.Success(Unit)
        }

    override suspend fun adjustReputation(
        userId: String,
        request: AdjustReputationRequestDto,
    ): ApiResult<Unit> =
        store.executeApi("$USERS_PATH/$userId/adjust-reputation") { snapshot ->
            val (admin, error) = adminState.requireAdmin<Unit>(snapshot, USERS_PATH)
            if (error != null) return@executeApi error
            val actor = requireNotNull(admin)

            val target =
                snapshot.users[userId]
                    ?: return@executeApi store.apiError(
                        httpStatus = 404,
                        code = "USER_001",
                        message = "Không tìm thấy người dùng.",
                        path = "$USERS_PATH/$userId/adjust-reputation",
                    )

            val newScore = (target.reputationScore + request.delta).coerceIn(0, 100)
            store.update { current ->
                current.copy(users = current.users + (userId to target.copy(reputationScore = newScore)))
            }
            adminState.appendAuditLog(
                actor = actor,
                action = "ADJUST_REPUTATION",
                targetType = "USER",
                targetId = userId,
                targetLabel = target.displayName.ifBlank { target.phone },
                detail =
                    "Điều chỉnh uy tín ${if (request.delta >= 0) "+${request.delta}" else "${request.delta}"}" +
                        " (${target.reputationScore} → $newScore): ${request.reason}",
            )
            ApiResult.Success(Unit)
        }

    override suspend fun getAuditLogs(
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<AuditLogDto>> =
        store.executeApi(AUDIT_LOGS_PATH) { snapshot ->
            val (_, error) = adminState.requireAdmin<PaginatedEnvelope<AuditLogDto>>(snapshot, AUDIT_LOGS_PATH)
            if (error != null) return@executeApi error

            ApiResult.Success(adminState.paginate(adminState.auditLogs.value, page, pageSize))
        }

    private companion object {
        const val USERS_PATH = "/api/v1/admin/users"
        const val AUDIT_LOGS_PATH = "/api/v1/admin/audit-logs"
    }
}
