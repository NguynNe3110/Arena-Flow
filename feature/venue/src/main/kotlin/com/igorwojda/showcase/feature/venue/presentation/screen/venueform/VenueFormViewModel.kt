@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LabeledExpression", "LongMethod")

package com.igorwojda.showcase.feature.venue.presentation.screen.venueform

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.AccountRole
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.domain.model.SessionUserSummary
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.domain.usecase.GetSessionStateUseCase
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.venue.domain.model.CreateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.UpdateVenueParams
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.domain.usecase.CreateVenueUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueDetailUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.GetVenueWardsUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.SelectVenueForCallerUseCase
import com.igorwojda.showcase.feature.venue.domain.usecase.UpdateVenueUseCase
import kotlinx.coroutines.launch

data class VenueFormUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val venueId: String? = null,
    val currentUser: SessionUserSummary? = null,
    val venueName: String = "",
    val address: String = "",
    val selectedWardId: String = "",
    val selectedFormat: PitchFormat = PitchFormat.SEVEN_A_SIDE,
    val phone: String = "",
    val website: String = "",
    val wards: List<VenueWardOption> = emptyList(),
    val nameError: String? = null,
    val addressError: String? = null,
    val wardError: String? = null,
    val screenError: String? = null,
    val submitError: String? = null,
    val isSubmitting: Boolean = false,
    val savedVenueId: String? = null,
    val isSelectionReturned: Boolean = false,
    val unreadNotifications: Int = 0,
) : BaseState {
    val isAdmin: Boolean
        get() = currentUser?.role == AccountRole.ADMIN
}

sealed interface VenueFormAction : BaseAction<VenueFormUiState> {
    data class Initialize(
        val venueId: String?,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) =
            state.copy(
                isLoading = true,
                isEditMode = venueId != null,
                venueId = venueId,
                screenError = null,
                submitError = null,
            )
    }

    data class SessionUpdated(
        val user: SessionUserSummary?,
        val unreadNotifications: Int,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(currentUser = user, unreadNotifications = unreadNotifications)
    }

    data class WardsLoaded(
        val wards: List<VenueWardOption>,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) =
            state.copy(
                wards = wards,
                selectedWardId = state.selectedWardId.ifBlank { wards.firstOrNull()?.wardId.orEmpty() },
                isLoading = if (state.isEditMode) state.isLoading else false,
            )
    }

    data class VenueLoaded(
        val venue: VenueDetail,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) =
            state.copy(
                isLoading = false,
                venueName = venue.venueName,
                address = venue.address,
                selectedWardId = venue.ward?.wardId ?: state.selectedWardId,
                selectedFormat = venue.venueType ?: PitchFormat.SEVEN_A_SIDE,
                phone = venue.phone.orEmpty(),
                website = venue.website.orEmpty(),
                screenError = null,
            )
    }

    data class ScreenError(
        val message: String,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(isLoading = false, screenError = message)
    }

    data class NameChanged(
        val value: String,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(venueName = value, nameError = null, submitError = null)
    }

    data class AddressChanged(
        val value: String,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(address = value, addressError = null, submitError = null)
    }

    data class WardSelected(
        val wardId: String,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(selectedWardId = wardId, wardError = null, submitError = null)
    }

    data class FormatSelected(
        val format: PitchFormat,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(selectedFormat = format, submitError = null)
    }

    data class PhoneChanged(
        val value: String,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(phone = value, submitError = null)
    }

    data class WebsiteChanged(
        val value: String,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(website = value, submitError = null)
    }

    data class ValidationFailed(
        val nameError: String?,
        val addressError: String?,
        val wardError: String?,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(nameError = nameError, addressError = addressError, wardError = wardError)
    }

    data object Submitting : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(isSubmitting = true, submitError = null)
    }

    data class SubmitError(
        val message: String,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(isSubmitting = false, submitError = message)
    }

    data class Saved(
        val venueId: String,
        val selectionReturned: Boolean,
    ) : VenueFormAction {
        override fun reduce(state: VenueFormUiState) =
            state.copy(
                isSubmitting = false,
                savedVenueId = venueId,
                isSelectionReturned = selectionReturned,
            )
    }

    data object NavigationConsumed : VenueFormAction {
        override fun reduce(state: VenueFormUiState) = state.copy(savedVenueId = null, isSelectionReturned = false)
    }
}

class VenueFormViewModel(
    private val createVenueUseCase: CreateVenueUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val getVenueDetailUseCase: GetVenueDetailUseCase,
    private val getVenueWardsUseCase: GetVenueWardsUseCase,
    private val selectVenueForCallerUseCase: SelectVenueForCallerUseCase,
    private val updateVenueUseCase: UpdateVenueUseCase,
) : BaseViewModel<VenueFormUiState, VenueFormAction>(VenueFormUiState()) {
    init {
        viewModelScope.launch {
            getSessionStateUseCase().collect { session ->
                sendAction(
                    VenueFormAction.SessionUpdated(
                        user = session.currentUser,
                        unreadNotifications = session.unreadNotifications,
                    ),
                )
            }
        }
    }

    fun loadForm(venueId: String?) {
        sendAction(VenueFormAction.Initialize(venueId))
        viewModelScope.launch {
            when (val wardsResult = getVenueWardsUseCase()) {
                is Result.Success -> sendAction(VenueFormAction.WardsLoaded(wardsResult.value))
                is Result.Failure -> Unit
            }

            val user = uiStateFlow.value.currentUser
            if (venueId == null) {
                if (user == null) {
                    sendAction(VenueFormAction.ScreenError("Vui lòng đăng nhập để thêm sân mới."))
                }
                return@launch
            }

            when (val detailResult = getVenueDetailUseCase(venueId)) {
                is Result.Success -> {
                    val venue = detailResult.value
                    val canEdit =
                        user != null &&
                            (user.role == AccountRole.ADMIN || (venue.createdById == user.id && !venue.isInactive))
                    if (!canEdit) {
                        sendAction(
                            VenueFormAction.ScreenError(
                                "Chỉ người tạo sân hoặc Admin mới được phép chỉnh sửa thông tin sân.",
                            ),
                        )
                    } else {
                        sendAction(VenueFormAction.VenueLoaded(venue))
                    }
                }

                is Result.Failure -> {
                    sendAction(VenueFormAction.ScreenError(detailResult.error.userMessage()))
                }
            }
        }
    }

    fun onNameChange(value: String) = sendAction(VenueFormAction.NameChanged(value))

    fun onAddressChange(value: String) = sendAction(VenueFormAction.AddressChanged(value))

    fun onWardSelect(wardId: String) = sendAction(VenueFormAction.WardSelected(wardId))

    fun onFormatSelect(format: PitchFormat) = sendAction(VenueFormAction.FormatSelected(format))

    fun onPhoneChange(value: String) = sendAction(VenueFormAction.PhoneChanged(value))

    fun onWebsiteChange(value: String) = sendAction(VenueFormAction.WebsiteChanged(value))

    fun submit(returnKey: String? = null) {
        val state = uiStateFlow.value
        val name = state.venueName.trim()
        val address = state.address.trim()
        val wardId = state.selectedWardId.trim()

        val nameErr = if (name.isEmpty()) "Vui lòng nhập tên sân bóng." else null
        val addressErr = if (address.isEmpty()) "Vui lòng nhập địa chỉ sân bóng." else null
        val wardErr = if (wardId.isEmpty()) "Vui lòng chọn phường/xã." else null
        if (nameErr != null || addressErr != null || wardErr != null) {
            sendAction(VenueFormAction.ValidationFailed(nameErr, addressErr, wardErr))
            return
        }

        viewModelScope.launch {
            sendAction(VenueFormAction.Submitting)
            val result =
                if (state.isEditMode && state.venueId != null) {
                    updateVenueUseCase(
                        venueId = state.venueId,
                        params =
                            UpdateVenueParams(
                                venueName = name,
                                address = address,
                                wardId = wardId,
                                venueType = state.selectedFormat.name,
                                phone = if (state.isAdmin) state.phone.trim() else null,
                                website = if (state.isAdmin) state.website.trim() else null,
                            ),
                    )
                } else {
                    createVenueUseCase(
                        CreateVenueParams(
                            venueName = name,
                            address = address,
                            latitude = DEFAULT_LATITUDE,
                            longitude = DEFAULT_LONGITUDE,
                            wardId = wardId,
                            venueType = state.selectedFormat,
                            phone = if (state.isAdmin) state.phone.trim().takeIf { it.isNotEmpty() } else null,
                            website = if (state.isAdmin) state.website.trim().takeIf { it.isNotEmpty() } else null,
                        ),
                    )
                }

            when (result) {
                is Result.Success -> {
                    val savedVenue = result.value
                    val shouldReturnSelection = !state.isEditMode && !returnKey.isNullOrBlank()
                    if (!state.isEditMode && !returnKey.isNullOrBlank()) {
                        selectVenueForCallerUseCase(
                            SelectedVenueResult(
                                requestKey = returnKey,
                                venueId = savedVenue.venueId,
                                venueName = savedVenue.venueName,
                                venueAddress = savedVenue.address,
                            ),
                        )
                    }
                    sendAction(VenueFormAction.Saved(savedVenue.venueId, shouldReturnSelection))
                }

                is Result.Failure -> {
                    sendAction(VenueFormAction.SubmitError(result.error.userMessage()))
                }
            }
        }
    }

    fun consumeNavigation() {
        sendAction(VenueFormAction.NavigationConsumed)
    }

    private companion object {
        const val DEFAULT_LATITUDE = 10.792
        const val DEFAULT_LONGITUDE = 106.744
    }
}
