package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState

/** Tabs in the ArenaFlow Admin Console (S14). */
enum class AdminConsoleTab(
    val label: String,
) {
    REPORTS("Báo cáo vi phạm"),
    VENUES("Sân chờ duyệt"),
    USERS("Người dùng"),
    AUDIT_LOGS("Nhật ký"),
}

/** Active modal dialog state in the Admin Console. */
sealed interface AdminConsoleDialog {
    data class ResolveReport(
        val report: AdminReport,
    ) : AdminConsoleDialog

    data class RejectVenue(
        val venue: AdminPendingVenue,
    ) : AdminConsoleDialog

    data class BanUser(
        val user: AdminUserSummary,
    ) : AdminConsoleDialog

    data class AdjustReputation(
        val user: AdminUserSummary,
    ) : AdminConsoleDialog
}

/** Immutable UI state for [AdminConsoleScreen]. */
data class AdminConsoleUiState(
    val selectedTab: AdminConsoleTab = AdminConsoleTab.REPORTS,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AppError? = null,
    val feedbackMessage: String? = null,
    val reportFilter: AdminReportStatus? = null,
    val reports: List<AdminReport> = emptyList(),
    val pendingVenues: List<AdminPendingVenue> = emptyList(),
    val userQuery: String = "",
    val userFilter: AdminUserStatus? = null,
    val users: List<AdminUserSummary> = emptyList(),
    val auditLogs: List<AuditLogEntry> = emptyList(),
    val activeDialog: AdminConsoleDialog? = null,
) : BaseState
