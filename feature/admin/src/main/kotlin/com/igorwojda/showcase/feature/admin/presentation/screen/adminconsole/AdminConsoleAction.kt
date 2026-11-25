package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import com.igorwojda.showcase.feature.admin.domain.model.AdminPendingVenue
import com.igorwojda.showcase.feature.admin.domain.model.AdminReport
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserSummary
import com.igorwojda.showcase.feature.admin.domain.model.AuditLogEntry
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction

internal sealed interface AdminConsoleAction : BaseAction<AdminConsoleUiState> {
    data object Loading : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(isLoading = true, error = null)
    }

    data object Submitting : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(isSubmitting = true, error = null)
    }

    data class SelectTab(
        val tab: AdminConsoleTab,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(selectedTab = tab, error = null)
    }

    data class SetReportFilter(
        val status: AdminReportStatus?,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(reportFilter = status)
    }

    data class ReportsLoaded(
        val reports: List<AdminReport>,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState =
            state.copy(isLoading = false, reports = reports, error = null)
    }

    data class PendingVenuesLoaded(
        val venues: List<AdminPendingVenue>,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState =
            state.copy(isLoading = false, pendingVenues = venues, error = null)
    }

    data class SetUserQuery(
        val query: String,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(userQuery = query)
    }

    data class SetUserFilter(
        val status: AdminUserStatus?,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(userFilter = status)
    }

    data class UsersLoaded(
        val users: List<AdminUserSummary>,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(isLoading = false, users = users, error = null)
    }

    data class AuditLogsLoaded(
        val logs: List<AuditLogEntry>,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(isLoading = false, auditLogs = logs, error = null)
    }

    data class OpenDialog(
        val dialog: AdminConsoleDialog?,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(activeDialog = dialog)
    }

    data class MutationSuccess(
        val message: String,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState =
            state.copy(isSubmitting = false, activeDialog = null, feedbackMessage = message)
    }

    data class Failed(
        val error: AppError,
    ) : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState =
            state.copy(isLoading = false, isSubmitting = false, error = error)
    }

    data object DismissFeedback : AdminConsoleAction {
        override fun reduce(state: AdminConsoleUiState): AdminConsoleUiState = state.copy(feedbackMessage = null)
    }
}
