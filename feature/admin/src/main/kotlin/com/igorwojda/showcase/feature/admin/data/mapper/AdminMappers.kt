package com.igorwojda.showcase.feature.admin.data.mapper

import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminPendingVenueDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminReportDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AdminUserSummaryDto
import com.igorwojda.showcase.feature.admin.data.datasource.api.model.AuditLogDto
import com.igorwojda.showcase.feature.admin.domain.model.AdminPage
import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.base.data.retrofit.envelope.PaginatedEnvelope

internal fun AdminReportDto.toDomain(): AdminReport =
    AdminReport(
        reportId = reportId,
        reporterType = reporterType,
        reporterId = reporterId,
        reporterName = reporterName,
        isReporterDeleted = isReporterDeleted,
        reportedId = reportedId,
        reportedName = reportedName,
        isReportedDeleted = isReportedDeleted,
        matchId = matchId,
        type = type,
        reason = reason,
        status = AdminReportStatus.fromApiValue(status),
        createdAt = createdAt,
        canDisputeUntil = canDisputeUntil,
        disputeNote = disputeNote,
        adminDecisionNote = adminDecisionNote,
    )

internal fun AdminPendingVenueDto.toDomain(): AdminPendingVenue =
    AdminPendingVenue(
        venueId = venueId,
        venueName = venueName,
        address = address,
        wardLabel = wardLabel,
        venueType = venueType,
        createdById = createdById,
        createdByName = createdByName,
        isCreatedByDeleted = isCreatedByDeleted,
        createdAt = createdAt,
        contactPhone = contactPhone,
    )

internal fun AdminUserSummaryDto.toDomain(): AdminUserSummary =
    AdminUserSummary(
        userId = userId,
        displayName = displayName,
        realName = realName,
        phone = phone,
        role = role,
        status = AdminUserStatus.fromApiValue(status),
        reputationScore = reputationScore,
        matchCount = matchCount,
        isDeleted = isDeleted,
        banReason = banReason,
    )

internal fun AuditLogDto.toDomain(): AuditLogEntry =
    AuditLogEntry(
        logId = logId,
        actorId = actorId,
        actorName = actorName,
        action = action,
        targetType = targetType,
        targetId = targetId,
        targetLabel = targetLabel,
        detail = detail,
        createdAt = createdAt,
    )

internal fun <T, R> PaginatedEnvelope<T>.toAdminPage(transform: (T) -> R): AdminPage<R> =
    AdminPage(
        items = data.map(transform),
        page = pagination.page,
        pageSize = pagination.pageSize,
        totalPages = pagination.totalPages,
        totalItems = pagination.totalItems,
    )
