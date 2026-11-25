package com.igorwojda.showcase.feature.venue.presentation.screen.venuedetail

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueDetailUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.ReportVenueUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SelectVenueForCallerUseCase
import kotlinx.coroutines.launch

data class VenueDetailUiState(
    val isLoading: Boolean = true,
    val venue: VenueDetail? = null,
    val currentUser: SessionUserSummary? = null,
    val errorMessage: String? = null,
    val isReportSheetOpen: Boolean = false,
    val selectedReason: VenueReportReason = VenueReportReason.CLOSED,
    val reportDetail: String = "",
    val reportDetailError: String? = null,
    val reportSubmitError: String? = null,
    val isSubmittingReport: Boolean = false,
    val snackbarMessage: String? = null,
    val isSelectionCompleted: Boolean = false,
    val unreadNotifications: Int = 0,
) : BaseState {
    val canEdit: Boolean
        get() {
            val v = venue ?: return false
            val u = currentUser ?: return false
            return u.role == AccountRole.ADMIN || (v.createdById == u.id && !v.isInactive)
        }

    val canReport: Boolean
        get() {
            val v = venue ?: return false
            val u = currentUser ?: return false
            return u.role != AccountRole.ADMIN && !v.isInactive
        }
}

sealed interface VenueDetailAction : BaseAction<VenueDetailUiState> {
    data object Loading : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(isLoading = true, errorMessage = null)
    }

    data class SessionUpdated(
        val user: SessionUserSummary?,
        val unreadNotifications: Int,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(currentUser = user, unreadNotifications = unreadNotifications)
    }

    data class Loaded(
        val venue: VenueDetail,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(isLoading = false, venue = venue, errorMessage = null)
    }

    data class Error(
        val message: String,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(isLoading = false, errorMessage = message)
    }

    data class ReportSheetVisibilityChanged(
        val isOpen: Boolean,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) =
            state.copy(
                isReportSheetOpen = isOpen,
                reportDetailError = null,
                reportSubmitError = null,
            )
    }

    data class ReportReasonChanged(
        val reason: VenueReportReason,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(selectedReason = reason)
    }

    data class ReportDetailChanged(
        val detail: String,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) =
            state.copy(reportDetail = detail, reportDetailError = null, reportSubmitError = null)
    }

    data class ReportValidationFailed(
        val message: String,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(reportDetailError = message)
    }

    data object ReportSubmitting : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(isSubmittingReport = true, reportSubmitError = null)
    }

    data object ReportSubmitted : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) =
            state.copy(
                isSubmittingReport = false,
                isReportSheetOpen = false,
                reportDetail = "",
                reportDetailError = null,
                reportSubmitError = null,
                snackbarMessage = "Đã gửi báo cáo thông tin sân cho Admin.",
            )
    }

    data class ReportFailed(
        val message: String,
    ) : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(isSubmittingReport = false, reportSubmitError = message)
    }

    data object SnackbarConsumed : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(snackbarMessage = null)
    }

    data object SelectionCompleted : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(isSelectionCompleted = true)
    }

    data object SelectionConsumed : VenueDetailAction {
        override fun reduce(state: VenueDetailUiState) = state.copy(isSelectionCompleted = false)
    }
}

class VenueDetailViewModel(
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val getVenueDetailUseCase: GetVenueDetailUseCase,
    private val reportVenueUseCase: ReportVenueUseCase,
    private val selectVenueForCallerUseCase: SelectVenueForCallerUseCase,
) : BaseViewModel<VenueDetailUiState, VenueDetailAction>(VenueDetailUiState()) {
    init {
        viewModelScope.launch {
            getSessionStateUseCase().collect { session ->
                sendAction(
                    VenueDetailAction.SessionUpdated(
                        user = session.currentUser,
                        unreadNotifications = session.unreadNotifications,
                    ),
                )
            }
        }
    }

    fun loadVenue(venueId: String) {
        viewModelScope.launch {
            sendAction(VenueDetailAction.Loading)
            when (val result = getVenueDetailUseCase(venueId)) {
                is Result.Success -> sendAction(VenueDetailAction.Loaded(result.value))
                is Result.Failure -> sendAction(VenueDetailAction.Error(result.error.userMessage()))
            }
        }
    }

    fun openReportSheet() {
        sendAction(VenueDetailAction.ReportSheetVisibilityChanged(true))
    }

    fun closeReportSheet() {
        sendAction(VenueDetailAction.ReportSheetVisibilityChanged(false))
    }

    fun onReportReasonChange(reason: VenueReportReason) {
        sendAction(VenueDetailAction.ReportReasonChanged(reason))
    }

    fun onReportDetailChange(detail: String) {
        sendAction(VenueDetailAction.ReportDetailChanged(detail))
    }

    fun submitReport() {
        val state = uiStateFlow.value
        val venueId = state.venue?.venueId ?: return
        val trimmedDetail = state.reportDetail.trim()
        if (trimmedDetail.isEmpty()) {
            sendAction(VenueDetailAction.ReportValidationFailed("Vui lòng nhập mô tả sai sót."))
            return
        }
        viewModelScope.launch {
            sendAction(VenueDetailAction.ReportSubmitting)
            when (val result = reportVenueUseCase(venueId, state.selectedReason, trimmedDetail)) {
                is Result.Success -> sendAction(VenueDetailAction.ReportSubmitted)
                is Result.Failure -> sendAction(VenueDetailAction.ReportFailed(result.error.userMessage()))
            }
        }
    }

    fun selectVenueForCaller(requestKey: String) {
        val venue = uiStateFlow.value.venue ?: return
        if (venue.isInactive) return
        viewModelScope.launch {
            selectVenueForCallerUseCase(
                SelectedVenueResult(
                    requestKey = requestKey,
                    venueId = venue.venueId,
                    venueName = venue.venueName,
                    venueAddress = venue.address,
                ),
            )
            sendAction(VenueDetailAction.SelectionCompleted)
        }
    }

    fun consumeSnackbar() {
        sendAction(VenueDetailAction.SnackbarConsumed)
    }

    fun consumeSelectionCompleted() {
        sendAction(VenueDetailAction.SelectionConsumed)
    }
}
