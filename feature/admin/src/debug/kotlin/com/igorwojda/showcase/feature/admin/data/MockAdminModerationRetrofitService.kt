@file:Suppress("LabeledExpression", "MagicNumber", "ReturnCount", "StringLiteralDuplication")

package com.igorwojda.showcase.feature.admin.data

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminPendingVenueDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminReportDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.RejectVenueRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.ResolveReportRequestDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.service.AdminModerationRetrofitService
import com.igorwojda.showcase.feature.base.data.retrofit.ApiResult
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.ApiEnvelope
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope
import com.igorwojda.showcase.feature.base.data.store.ArenaInMemoryStore
import com.igorwojda.showcase.feature.base.data.store.ArenaStoreSnapshot
import com.igorwojda.showcase.feature.base.data.store.StoreReport
import com.igorwojda.showcase.feature.base.data.store.StoreVenue
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat

internal class MockAdminModerationRetrofitService(
    private val store: ArenaInMemoryStore,
    private val adminState: MockAdminStateHolder,
) : AdminModerationRetrofitService {
    override suspend fun getReports(
        status: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<PaginatedEnvelope<AdminReportDto>> =
        store.executeApi(REPORTS_PATH) { snapshot ->
            val (_, error) = adminState.requireAdmin<PaginatedEnvelope<AdminReportDto>>(snapshot, REPORTS_PATH)
            if (error != null) return@executeApi error

            val filtered =
                snapshot.reports
                    .filter { report -> status.isNullOrBlank() || report.status.equals(status, ignoreCase = true) }
                    .sortedByDescending { it.createdAt }
                    .map { it.toAdminDto(snapshot) }

            ApiResult.Success(adminState.paginate(filtered, page, pageSize))
        }

    override suspend fun resolveReport(
        reportId: String,
        request: ResolveReportRequestDto,
    ): ApiResult<ApiEnvelope<AdminReportDto>> =
        store.executeApi("$REPORTS_PATH/$reportId/resolve") { snapshot ->
            val (admin, error) = adminState.requireAdmin<ApiEnvelope<AdminReportDto>>(snapshot, REPORTS_PATH)
            if (error != null) return@executeApi error
            val actor = requireNotNull(admin)

            val existing =
                snapshot.reports.firstOrNull { it.id == reportId }
                    ?: return@executeApi store.apiError(
                        httpStatus = 404,
                        code = "REPORT_001",
                        message = "Không tìm thấy báo cáo.",
                        path = "$REPORTS_PATH/$reportId/resolve",
                    )

            val newStatus = if (request.decision.equals("REJECTED", ignoreCase = true)) "REJECTED" else "CONFIRMED"
            val updated =
                existing.copy(
                    status = newStatus,
                    adminDecisionNote = request.note?.trim()?.takeIf { it.isNotEmpty() },
                )

            store.update { current ->
                current.copy(reports = current.reports.map { if (it.id == reportId) updated else it })
            }
            adminState.appendAuditLog(
                actor = actor,
                action = "RESOLVE_REPORT",
                targetType = "REPORT",
                targetId = reportId,
                targetLabel = "Báo cáo #$reportId",
                detail = "Quyết định: $newStatus${request.note?.let { " — $it" }.orEmpty()}",
            )
            ApiResult.Success(ApiEnvelope(updated.toAdminDto(store.current)))
        }

    override suspend fun getPendingVenues(): ApiResult<ApiEnvelope<List<AdminPendingVenueDto>>> =
        store.executeApi(VENUES_PENDING_PATH) { snapshot ->
            val (_, error) = adminState.requireAdmin<ApiEnvelope<List<AdminPendingVenueDto>>>(snapshot, VENUES_PENDING_PATH)
            if (error != null) return@executeApi error

            ApiResult.Success(ApiEnvelope(adminState.pendingVenues.value))
        }

    override suspend fun approveVenue(venueId: String): ApiResult<Unit> =
        store.executeApi("$VENUES_PATH/$venueId/approve") { snapshot ->
            val (admin, error) = adminState.requireAdmin<Unit>(snapshot, VENUES_PATH)
            if (error != null) return@executeApi error
            val actor = requireNotNull(admin)

            val pending =
                adminState.pendingVenues.value.firstOrNull { it.venueId == venueId }
                    ?: return@executeApi store.apiError(
                        httpStatus = 404,
                        code = "VENUE_001",
                        message = "Không tìm thấy sân chờ duyệt.",
                        path = "$VENUES_PATH/$venueId/approve",
                    )

            adminState.pendingVenues.value = adminState.pendingVenues.value.filterNot { it.venueId == venueId }
            val newVenue =
                StoreVenue(
                    id = pending.venueId,
                    name = pending.venueName,
                    address = pending.address,
                    wardId = snapshot.wards.firstOrNull()?.id ?: "ward-ak",
                    lat = DEFAULT_LAT,
                    lng = DEFAULT_LNG,
                    distanceKm = 2.0,
                    type = PitchFormat.entries.firstOrNull { it.name == pending.venueType } ?: PitchFormat.SEVEN_A_SIDE,
                    contactPhone = pending.contactPhone,
                    status = "ACTIVE",
                    createdBy = pending.createdById,
                )
            store.update { current -> current.copy(venues = current.venues + newVenue) }
            adminState.appendAuditLog(
                actor = actor,
                action = "APPROVE_VENUE",
                targetType = "VENUE",
                targetId = venueId,
                targetLabel = pending.venueName,
                detail = "Phê duyệt sân mới và kích hoạt hiển thị.",
            )
            ApiResult.Success(Unit)
        }

    override suspend fun rejectVenue(
        venueId: String,
        request: RejectVenueRequestDto,
    ): ApiResult<Unit> =
        store.executeApi("$VENUES_PATH/$venueId/reject") { snapshot ->
            val (admin, error) = adminState.requireAdmin<Unit>(snapshot, VENUES_PATH)
            if (error != null) return@executeApi error
            val actor = requireNotNull(admin)

            if (request.reason.isBlank()) {
                return@executeApi store.apiError(
                    httpStatus = 400,
                    code = "VALIDATION_ERROR",
                    message = "Lý do từ chối không được để trống.",
                    path = "$VENUES_PATH/$venueId/reject",
                )
            }

            val pending =
                adminState.pendingVenues.value.firstOrNull { it.venueId == venueId }
                    ?: return@executeApi store.apiError(
                        httpStatus = 404,
                        code = "VENUE_001",
                        message = "Không tìm thấy sân chờ duyệt.",
                        path = "$VENUES_PATH/$venueId/reject",
                    )

            adminState.pendingVenues.value = adminState.pendingVenues.value.filterNot { it.venueId == venueId }
            adminState.appendAuditLog(
                actor = actor,
                action = "REJECT_VENUE",
                targetType = "VENUE",
                targetId = venueId,
                targetLabel = pending.venueName,
                detail = "Từ chối sân: ${request.reason.trim()}",
            )
            ApiResult.Success(Unit)
        }

    private fun StoreReport.toAdminDto(snapshot: ArenaStoreSnapshot): AdminReportDto {
        val reporterInfo = resolveEntityName(snapshot, reporterId)
        val reportedInfo = resolveEntityName(snapshot, reportedId)
        return AdminReportDto(
            reportId = id,
            reporterType = reporterType,
            reporterId = reporterId,
            reporterName = reporterInfo.first,
            isReporterDeleted = reporterInfo.second,
            reportedId = reportedId,
            reportedName = reportedInfo.first,
            isReportedDeleted = reportedInfo.second,
            matchId = matchId,
            type = type,
            reason = reason,
            status = status,
            createdAt = createdAt,
            canDisputeUntil = canDisputeUntil,
            disputeNote = disputeNote,
            adminDecisionNote = adminDecisionNote,
        )
    }

    private fun resolveEntityName(
        snapshot: ArenaStoreSnapshot,
        entityId: String,
    ): Pair<String, Boolean> {
        snapshot.users[entityId]?.let { user ->
            val name = user.realName ?: user.displayName.ifBlank { user.phone }
            return name to user.isDeleted
        }
        snapshot.teams.firstOrNull { it.id == entityId }?.let { team ->
            return team.name to false
        }
        snapshot.venues.firstOrNull { it.id == entityId }?.let { venue ->
            return venue.name to false
        }
        return entityId to false
    }

    private companion object {
        const val REPORTS_PATH = "/api/v1/admin/reports"
        const val VENUES_PENDING_PATH = "/api/v1/admin/venues/pending"
        const val VENUES_PATH = "/api/v1/admin/venues"
        const val DEFAULT_LAT = 10.7881
        const val DEFAULT_LNG = 106.6991
    }
}
