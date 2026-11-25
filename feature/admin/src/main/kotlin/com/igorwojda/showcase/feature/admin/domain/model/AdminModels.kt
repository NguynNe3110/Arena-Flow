package com.igorwojda.showcase.feature.admin.domain.model

/** Report lifecycle status visible in the admin moderation queue. */
enum class AdminReportStatus(
    val apiValue: String,
    val label: String,
) {
    PENDING("PENDING", "Chờ xử lý"),
    AUTO_ACCEPTED("AUTO_ACCEPTED", "Tự động chấp nhận"),
    DISPUTED("DISPUTED", "Đang tranh chấp"),
    CONFIRMED("CONFIRMED", "Đã xác nhận"),
    REJECTED("REJECTED", "Đã bác bỏ"),
    ;

    companion object {
        fun fromApiValue(value: String): AdminReportStatus = entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: PENDING
    }
}

/** Decision options when an admin resolves a report. */
enum class ReportDecision(
    val apiValue: String,
    val label: String,
) {
    CONFIRM("CONFIRMED", "Xác nhận vi phạm"),
    DISMISS("REJECTED", "Bác bỏ báo cáo"),
}

/** Domain model for an admin moderation report. */
data class AdminReport(
    val reportId: String,
    val reporterType: String,
    val reporterId: String,
    val reporterName: String,
    val isReporterDeleted: Boolean,
    val reportedId: String,
    val reportedName: String,
    val isReportedDeleted: Boolean,
    val matchId: String?,
    val type: String,
    val reason: String,
    val status: AdminReportStatus,
    val createdAt: String,
    val canDisputeUntil: String?,
    val disputeNote: String?,
    val adminDecisionNote: String?,
)

/** Domain model for a venue awaiting admin approval or rejection. */
data class AdminPendingVenue(
    val venueId: String,
    val venueName: String,
    val address: String,
    val wardLabel: String,
    val venueType: String,
    val createdById: String,
    val createdByName: String,
    val isCreatedByDeleted: Boolean,
    val createdAt: String,
    val contactPhone: String?,
)

/** User account moderation status in the admin user management tab. */
enum class AdminUserStatus(
    val apiValue: String,
    val label: String,
) {
    ACTIVE("ACTIVE", "Hoạt động"),
    BANNED("BANNED", "Bị khóa"),
    DELETED("DELETED", "Đã xóa"),
    ;

    companion object {
        fun fromApiValue(value: String): AdminUserStatus = entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}

/** Domain model for an admin user summary row. */
data class AdminUserSummary(
    val userId: String,
    val displayName: String,
    val realName: String?,
    val phone: String,
    val role: String,
    val status: AdminUserStatus,
    val reputationScore: Int,
    val matchCount: Int,
    val isDeleted: Boolean,
    val banReason: String?,
)

/** Domain model for an admin audit log timeline entry. */
data class AuditLogEntry(
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

/** Paginated result wrapper for admin lists. */
data class AdminPage<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
)
