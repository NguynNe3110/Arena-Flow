package com.igorwojda.showcase.feature.venue.presentation.screen.venuepicker

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueWardsUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenuesUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SelectVenueForCallerUseCase
import kotlinx.coroutines.launch

data class VenuePickerUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedFormat: PitchFormat? = null,
    val selectedWardId: String? = null,
    val wards: List<VenueWardOption> = emptyList(),
    val allVenues: List<VenueSummary> = emptyList(),
    val visibleVenues: List<VenueSummary> = emptyList(),
    val isFilterSheetOpen: Boolean = false,
    val errorMessage: String? = null,
    val isSelectionCompleted: Boolean = false,
    val userName: String = "ArenaFlow",
    val unreadNotifications: Int = 0,
) : BaseState {
    val activeFilterCount: Int
        get() = listOfNotNull(selectedFormat, selectedWardId).size
}

sealed interface VenuePickerAction : BaseAction<VenuePickerUiState> {
    data object Loading : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(isLoading = true, errorMessage = null)
    }

    data class SessionUpdated(
        val userName: String,
        val unreadNotifications: Int,
    ) : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(userName = userName, unreadNotifications = unreadNotifications)
    }

    data class WardsLoaded(
        val wards: List<VenueWardOption>,
    ) : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(wards = wards)
    }

    data class VenuesLoaded(
        val venues: List<VenueSummary>,
    ) : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) =
            state.copy(
                isLoading = false,
                errorMessage = null,
                allVenues = venues,
                visibleVenues = filterVenues(venues, state.selectedFormat, state.selectedWardId, state.wards),
            )
    }

    data class Error(
        val message: String,
    ) : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(isLoading = false, errorMessage = message)
    }

    data class QueryChanged(
        val query: String,
    ) : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(query = query)
    }

    data class FiltersApplied(
        val format: PitchFormat?,
        val wardId: String?,
    ) : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) =
            state.copy(
                selectedFormat = format,
                selectedWardId = wardId,
                isFilterSheetOpen = false,
                visibleVenues = filterVenues(state.allVenues, format, wardId, state.wards),
            )
    }

    data class FilterSheetVisibilityChanged(
        val isOpen: Boolean,
    ) : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(isFilterSheetOpen = isOpen)
    }

    data object SelectionCompleted : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(isSelectionCompleted = true)
    }

    data object SelectionConsumed : VenuePickerAction {
        override fun reduce(state: VenuePickerUiState) = state.copy(isSelectionCompleted = false)
    }
}

private fun filterVenues(
    venues: List<VenueSummary>,
    format: PitchFormat?,
    wardId: String?,
    wards: List<VenueWardOption>,
): List<VenueSummary> {
    val selectedWardName = wards.firstOrNull { it.wardId == wardId }?.wardName
    return venues.filter { venue ->
        val matchesFormat = format == null || venue.venueType == format
        val matchesWard =
            wardId == null ||
                selectedWardName == null ||
                venue.address.contains(selectedWardName, ignoreCase = true)
        matchesFormat && matchesWard
    }
}

class VenuePickerViewModel(
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val getVenueWardsUseCase: GetVenueWardsUseCase,
    private val getVenuesUseCase: GetVenuesUseCase,
    private val selectVenueForCallerUseCase: SelectVenueForCallerUseCase,
) : BaseViewModel<VenuePickerUiState, VenuePickerAction>(VenuePickerUiState()) {
    init {
        observeSession()
        loadWards()
        loadVenues()
    }

    private fun observeSession() {
        viewModelScope.launch {
            getSessionStateUseCase().collect { session ->
                sendAction(
                    VenuePickerAction.SessionUpdated(
                        userName = session.currentUser?.displayName ?: "Khách",
                        unreadNotifications = session.unreadNotifications,
                    ),
                )
            }
        }
    }

    private fun loadWards() {
        viewModelScope.launch {
            when (val result = getVenueWardsUseCase()) {
                is Result.Success -> sendAction(VenuePickerAction.WardsLoaded(result.value))
                is Result.Failure -> Unit
            }
        }
    }

    fun loadVenues(query: String = uiStateFlow.value.query) {
        viewModelScope.launch {
            sendAction(VenuePickerAction.Loading)
            when (val result = getVenuesUseCase(query = query)) {
                is Result.Success -> sendAction(VenuePickerAction.VenuesLoaded(result.value))
                is Result.Failure -> sendAction(VenuePickerAction.Error(result.error.userMessage()))
            }
        }
    }

    fun onQueryChange(query: String) {
        sendAction(VenuePickerAction.QueryChanged(query))
        loadVenues(query)
    }

    fun openFilterSheet() {
        sendAction(VenuePickerAction.FilterSheetVisibilityChanged(true))
    }

    fun closeFilterSheet() {
        sendAction(VenuePickerAction.FilterSheetVisibilityChanged(false))
    }

    fun applyFilters(
        format: PitchFormat?,
        wardId: String?,
    ) {
        sendAction(VenuePickerAction.FiltersApplied(format, wardId))
    }

    fun selectVenue(
        requestKey: String,
        venue: VenueSummary,
    ) {
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
            sendAction(VenuePickerAction.SelectionCompleted)
        }
    }

    fun consumeSelectionCompleted() {
        sendAction(VenuePickerAction.SelectionConsumed)
    }
}
