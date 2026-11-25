package com.igorwojda.showcase.feature.admin.data.datasource.api.model

import kotlinx.serialization.Serializable

/** DTO representing a violation or dispute report in the admin queue. */
@Serializable
data class AdminReportDto(
    val reportId: String,
    val reporterType: String,
    val reporterId: String,
    val reporterName: String,
    val isReporterDeleted: Boolean = false,
    val reportedId: String,
    val reportedName: String,
    val isReportedDeleted: Boolean = false,
    val matchId: String? = null,
    val type: String,
    val reason: String,
    val status: String,
    val createdAt: String,
    val canDisputeUntil: String? = null,
    val disputeNote: String? = null,
    val adminDecisionNote: String? = null,
)

/** Request DTO for resolving or dismissing an admin report. */
@Serializable
data class ResolveReportRequestDto(
    val decision: String,
    val note: String? = null,
)

/** DTO representing a pending venue awaiting admin moderation. */
@Serializable
data class AdminPendingVenueDto(
    val venueId: String,
    val venueName: String,
    val address: String,
    val wardLabel: String,
    val venueType: String,
    val createdById: String,
    val createdByName: String,
    val isCreatedByDeleted: Boolean = false,
    val createdAt: String,
    val contactPhone: String? = null,
)

/** Request DTO for rejecting a pending venue. */
@Serializable
data class RejectVenueRequestDto(
    val reason: String,
)

/** DTO representing a user summary in the admin user management tab. */
@Serializable
data class AdminUserSummaryDto(
    val userId: String,
    val displayName: String,
    val realName: String? = null,
    val phone: String,
    val role: String,
    val status: String,
    val reputationScore: Int,
    val matchCount: Int,
    val isDeleted: Boolean = false,
    val banReason: String? = null,
)

/** Request DTO for banning a user. */
@Serializable
data class BanUserRequestDto(
    val reason: String,
)

/** Request DTO for adjusting a user's reputation score. */
@Serializable
data class AdjustReputationRequestDto(
    val delta: Int,
    val reason: String,
)

/** DTO representing an entry in the admin audit log. */
@Serializable
data class AuditLogDto(
    val logId: String,
    val actorId: String,
    val actorName: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val targetLabel: String,
    val detail: String,
    val createdAt: String,
)
