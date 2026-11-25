package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision

/** Events dispatched from [AdminConsoleScreen] to [AdminConsoleViewModel]. */
sealed interface AdminConsoleEvent {
    data class SelectTab(
        val tab: AdminConsoleTab,
    ) : AdminConsoleEvent

    data object Refresh : AdminConsoleEvent

    data class FilterReportsByStatus(
        val status: AdminReportStatus?,
    ) : AdminConsoleEvent

    data class OpenResolveReportDialog(
        val report: AdminReport,
    ) : AdminConsoleEvent

    data class SubmitReportDecision(
        val reportId: String,
        val decision: ReportDecision,
        val note: String?,
    ) : AdminConsoleEvent

    data class ApproveVenue(
        val venueId: String,
    ) : AdminConsoleEvent

    data class OpenRejectVenueDialog(
        val venue: AdminPendingVenue,
    ) : AdminConsoleEvent

    data class SubmitRejectVenue(
        val venueId: String,
        val reason: String,
    ) : AdminConsoleEvent

    data class SearchUsers(
        val query: String,
    ) : AdminConsoleEvent

    data class FilterUsersByStatus(
        val status: AdminUserStatus?,
    ) : AdminConsoleEvent

    data class OpenBanUserDialog(
        val user: AdminUserSummary,
    ) : AdminConsoleEvent

    data class SubmitBanUser(
        val userId: String,
        val reason: String,
    ) : AdminConsoleEvent

    data class UnbanUser(
        val userId: String,
    ) : AdminConsoleEvent

    data class OpenAdjustReputationDialog(
        val user: AdminUserSummary,
    ) : AdminConsoleEvent

    data class SubmitAdjustReputation(
        val userId: String,
        val delta: Int,
        val reason: String,
    ) : AdminConsoleEvent

    data object DismissDialog : AdminConsoleEvent

    data object DismissFeedback : AdminConsoleEvent
}
