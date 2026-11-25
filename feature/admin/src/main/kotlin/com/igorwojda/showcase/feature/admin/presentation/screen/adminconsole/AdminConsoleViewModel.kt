@file:Suppress("LongParameterList")

package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.admin.domain.model.AdminReportStatus
import com.igorwojda.showcase.feature.admin.domain.model.AdminUserStatus
import com.igorwojda.showcase.feature.admin.domain.usecase.AdjustUserReputationUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.ApproveVenueUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.BanUserUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAdminReportsUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAdminUsersUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetAuditLogsUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.GetPendingVenuesUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.RejectVenueUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.ResolveAdminReportUseCase
import com.igorwojda.showcase.feature.admin.domain.usecase.UnbanUserUseCase
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class AdminConsoleViewModel(
    private val adjustUserReputationUseCase: AdjustUserReputationUseCase,
    private val approveVenueUseCase: ApproveVenueUseCase,
    private val banUserUseCase: BanUserUseCase,
    private val getAdminReportsUseCase: GetAdminReportsUseCase,
    private val getAdminUsersUseCase: GetAdminUsersUseCase,
    private val getAuditLogsUseCase: GetAuditLogsUseCase,
    private val getPendingVenuesUseCase: GetPendingVenuesUseCase,
    private val rejectVenueUseCase: RejectVenueUseCase,
    private val resolveAdminReportUseCase: ResolveAdminReportUseCase,
    private val unbanUserUseCase: UnbanUserUseCase,
) : BaseViewModel<AdminConsoleUiState, AdminConsoleAction>(AdminConsoleUiState()) {
    private var loadJob: Job? = null

    init {
        loadActiveTab(AdminConsoleTab.REPORTS)
    }

    /** Dispatches UI events from [AdminConsoleScreen]. */
    fun onEvent(event: AdminConsoleEvent) {
        when (event) {
            is AdminConsoleEvent.SelectTab -> {
                sendAction(AdminConsoleAction.SelectTab(event.tab))
                loadActiveTab(event.tab)
            }

            AdminConsoleEvent.Refresh -> {
                loadActiveTab(uiStateFlow.value.selectedTab)
            }

            is AdminConsoleEvent.FilterReportsByStatus -> {
                sendAction(AdminConsoleAction.SetReportFilter(event.status))
                loadReports(event.status)
            }

            is AdminConsoleEvent.SearchUsers -> {
                sendAction(AdminConsoleAction.SetUserQuery(event.query))
                loadUsers(event.query, uiStateFlow.value.userFilter)
            }

            is AdminConsoleEvent.FilterUsersByStatus -> {
                sendAction(AdminConsoleAction.SetUserFilter(event.status))
                loadUsers(uiStateFlow.value.userQuery, event.status)
            }

            AdminConsoleEvent.DismissFeedback -> {
                sendAction(AdminConsoleAction.DismissFeedback)
            }

            else -> {
                handleDialogOrMutation(event)
            }
        }
    }

    private fun handleDialogOrMutation(event: AdminConsoleEvent) {
        when (event) {
            is AdminConsoleEvent.OpenResolveReportDialog -> {
                sendAction(AdminConsoleAction.OpenDialog(AdminConsoleDialog.ResolveReport(event.report)))
            }

            is AdminConsoleEvent.OpenRejectVenueDialog -> {
                sendAction(AdminConsoleAction.OpenDialog(AdminConsoleDialog.RejectVenue(event.venue)))
            }

            is AdminConsoleEvent.OpenBanUserDialog -> {
                sendAction(AdminConsoleAction.OpenDialog(AdminConsoleDialog.BanUser(event.user)))
            }

            is AdminConsoleEvent.OpenAdjustReputationDialog -> {
                sendAction(AdminConsoleAction.OpenDialog(AdminConsoleDialog.AdjustReputation(event.user)))
            }

            AdminConsoleEvent.DismissDialog -> {
                sendAction(AdminConsoleAction.OpenDialog(null))
            }

            else -> {
                handleMutationEvent(event)
            }
        }
    }

    private fun handleMutationEvent(event: AdminConsoleEvent) {
        when (event) {
            is AdminConsoleEvent.SubmitReportDecision -> {
                runMutation(
                    successMessage = "Đã xử lý báo cáo vi phạm.",
                    block = { resolveAdminReportUseCase(event.reportId, event.decision, event.note) },
                    onSuccessReload = { loadReports(uiStateFlow.value.reportFilter) },
                )
            }

            is AdminConsoleEvent.ApproveVenue -> {
                runMutation(
                    successMessage = "Đã phê duyệt sân mới.",
                    block = { approveVenueUseCase(event.venueId) },
                    onSuccessReload = { loadPendingVenues() },
                )
            }

            is AdminConsoleEvent.SubmitRejectVenue -> {
                runMutation(
                    successMessage = "Đã từ chối sân.",
                    block = { rejectVenueUseCase(event.venueId, event.reason) },
                    onSuccessReload = { loadPendingVenues() },
                )
            }

            is AdminConsoleEvent.SubmitBanUser -> {
                runMutation(
                    successMessage = "Đã khóa tài khoản người dùng.",
                    block = { banUserUseCase(event.userId, event.reason) },
                    onSuccessReload = { loadUsers(uiStateFlow.value.userQuery, uiStateFlow.value.userFilter) },
                )
            }

            is AdminConsoleEvent.UnbanUser -> {
                runMutation(
                    successMessage = "Đã mở khóa tài khoản.",
                    block = { unbanUserUseCase(event.userId) },
                    onSuccessReload = { loadUsers(uiStateFlow.value.userQuery, uiStateFlow.value.userFilter) },
                )
            }

            is AdminConsoleEvent.SubmitAdjustReputation -> {
                runMutation(
                    successMessage = "Đã cập nhật điểm uy tín.",
                    block = { adjustUserReputationUseCase(event.userId, event.delta, event.reason) },
                    onSuccessReload = { loadUsers(uiStateFlow.value.userQuery, uiStateFlow.value.userFilter) },
                )
            }

            else -> {
                // No-op for unhandled event variants.
            }
        }
    }

    private fun loadActiveTab(tab: AdminConsoleTab) {
        when (tab) {
            AdminConsoleTab.REPORTS -> loadReports(uiStateFlow.value.reportFilter)
            AdminConsoleTab.VENUES -> loadPendingVenues()
            AdminConsoleTab.USERS -> loadUsers(uiStateFlow.value.userQuery, uiStateFlow.value.userFilter)
            AdminConsoleTab.AUDIT_LOGS -> loadAuditLogs()
        }
    }

    private fun loadReports(status: AdminReportStatus?) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                sendAction(AdminConsoleAction.Loading)
                when (val result = getAdminReportsUseCase(status = status?.apiValue)) {
                    is Result.Success -> sendAction(AdminConsoleAction.ReportsLoaded(result.value.items))
                    is Result.Failure -> sendAction(AdminConsoleAction.Failed(result.error))
                }
            }
    }

    private fun loadPendingVenues() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                sendAction(AdminConsoleAction.Loading)
                when (val result = getPendingVenuesUseCase()) {
                    is Result.Success -> sendAction(AdminConsoleAction.PendingVenuesLoaded(result.value))
                    is Result.Failure -> sendAction(AdminConsoleAction.Failed(result.error))
                }
            }
    }

    private fun loadUsers(
        query: String,
        status: AdminUserStatus?,
    ) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                sendAction(AdminConsoleAction.Loading)
                val q = query.trim().takeIf { it.isNotEmpty() }
                when (val result = getAdminUsersUseCase(query = q, status = status?.apiValue)) {
                    is Result.Success -> sendAction(AdminConsoleAction.UsersLoaded(result.value.items))
                    is Result.Failure -> sendAction(AdminConsoleAction.Failed(result.error))
                }
            }
    }

    private fun loadAuditLogs() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                sendAction(AdminConsoleAction.Loading)
                when (val result = getAuditLogsUseCase()) {
                    is Result.Success -> sendAction(AdminConsoleAction.AuditLogsLoaded(result.value.items))
                    is Result.Failure -> sendAction(AdminConsoleAction.Failed(result.error))
                }
            }
    }

    private fun runMutation(
        successMessage: String,
        block: suspend () -> Result<*>,
        onSuccessReload: () -> Unit,
    ) {
        viewModelScope.launch {
            sendAction(AdminConsoleAction.Submitting)
            when (val result = block()) {
                is Result.Success -> {
                    sendAction(AdminConsoleAction.MutationSuccess(successMessage))
                    onSuccessReload()
                }

                is Result.Failure -> {
                    sendAction(AdminConsoleAction.Failed(result.error))
                }
            }
        }
    }
}
