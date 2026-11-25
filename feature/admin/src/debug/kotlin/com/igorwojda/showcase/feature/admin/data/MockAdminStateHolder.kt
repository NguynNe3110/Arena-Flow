@file:Suppress("LongParameterList", "MagicNumber", "ReturnCount")

package com.igorwojda.showcase.feature.admin.data

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminPendingVenueDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AuditLogDto
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginationDto
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreUser
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.ceil

/** Holds debug-only mutable admin state synced with [ArenaInMemoryStore]. */
internal class MockAdminStateHolder(
    private val store: ArenaInMemoryStore,
) {
    val bannedUsers = MutableStateFlow<Map<String, String>>(emptyMap())
    val pendingVenues =
        MutableStateFlow(
            listOf(
                AdminPendingVenueDto(
                    venueId = "ven-pending-1",
                    venueName = "Sân Bóng Đá K34 Tân Bình",
                    address = "24 Cộng Hòa, Phường 4, Tân Bình, TP.HCM",
                    wardLabel = "Phường 4 · Tân Bình · TP. Hồ Chí Minh",
                    venueType = "SEVEN_A_SIDE",
                    createdById = "user-cap-beta",
                    createdByName = "Phạm Văn Hùng",
                    isCreatedByDeleted = false,
                    createdAt = "2026-11-18T09:15:00",
                    contactPhone = "0904000004",
                ),
                AdminPendingVenueDto(
                    venueId = "ven-pending-2",
                    venueName = "Sân Mini Thủ Đức Arena",
                    address = "112 Võ Văn Ngân, Bình Thọ, TP. Thủ Đức",
                    wardLabel = "Phường Bình Thọ · TP. Thủ Đức · TP. Hồ Chí Minh",
                    venueType = "FIVE_A_SIDE",
                    createdById = "user-player",
                    createdByName = "Trần Văn Minh",
                    isCreatedByDeleted = false,
                    createdAt = "2026-11-18T14:20:00",
                    contactPhone = null,
                ),
            ),
        )
    val auditLogs =
        MutableStateFlow(
            listOf(
                AuditLogDto(
                    logId = "audit-1",
                    actorId = "user-admin",
                    actorName = "Admin Hệ Thống",
                    action = "RESOLVE_REPORT",
                    targetType = "REPORT",
                    targetId = "rep-noshow-expired",
                    targetLabel = "Báo cáo #rep-noshow-expired",
                    detail = "Xác nhận vi phạm bùng kèo (quá hạn tranh chấp 24h).",
                    createdAt = "2026-11-17T15:05:00",
                ),
                AuditLogDto(
                    logId = "audit-2",
                    actorId = "user-admin",
                    actorName = "Admin Hệ Thống",
                    action = "MARK_VENUE_INACTIVE",
                    targetType = "VENUE",
                    targetId = "ven-inactive-1",
                    targetLabel = "Sân Cũ Đóng Cửa",
                    detail = "Đánh dấu ngừng hoạt động theo phản ánh sân đã đóng cửa.",
                    createdAt = "2026-11-17T18:30:00",
                ),
            ),
        )

    fun <T> requireAdmin(
        snapshot: ArenaStoreSnapshot,
        path: String,
    ): Pair<StoreUser?, ApiResult<T>?> {
        val uid =
            snapshot.currentUserId
                ?: return null to
                    store.apiError(
                        httpStatus = 401,
                        code = "AUTH_001",
                        message = "Phiên đăng nhập hết hạn.",
                        path = path,
                    )
        val user = snapshot.users[uid]
        if (user == null || user.isDeleted) {
            return null to
                store.apiError(
                    httpStatus = 401,
                    code = "AUTH_001",
                    message = "Tài khoản không tồn tại.",
                    path = path,
                )
        }
        if (user.role != AccountRole.ADMIN) {
            return null to
                store.apiError(
                    httpStatus = 403,
                    code = "AUTH_004",
                    message = "Bạn không có quyền truy cập chức năng quản trị.",
                    path = path,
                )
        }
        return user to null
    }

    fun appendAuditLog(
        actor: StoreUser,
        action: String,
        targetType: String,
        targetId: String,
        targetLabel: String,
        detail: String,
    ) {
        val entry =
            AuditLogDto(
                logId = "audit-${auditLogs.value.size + 1}",
                actorId = actor.id,
                actorName = actor.displayName.ifBlank { actor.phone },
                action = action,
                targetType = targetType,
                targetId = targetId,
                targetLabel = targetLabel,
                detail = detail,
                createdAt = store.current.mockClockIso,
            )
        auditLogs.value = listOf(entry) + auditLogs.value
    }

    fun <T> paginate(
        items: List<T>,
        page: Int,
        pageSize: Int,
    ): PaginatedEnvelope<T> {
        val safePage = page.coerceAtLeast(1)
        val safeSize = pageSize.coerceIn(1, 100)
        val totalItems = items.size
        val totalPages = if (totalItems == 0) 1 else ceil(totalItems.toDouble() / safeSize).toInt()
        val fromIndex = ((safePage - 1) * safeSize).coerceAtMost(totalItems)
        val toIndex = (fromIndex + safeSize).coerceAtMost(totalItems)
        return PaginatedEnvelope(
            data = items.subList(fromIndex, toIndex),
            pagination =
                PaginationDto(
                    page = safePage,
                    pageSize = safeSize,
                    totalPages = totalPages,
                    totalItems = totalItems,
                ),
        )
    }
}
