package com.igorwojda.showcase.feature.pairing.presentation.screen.pairingroom

import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseAction
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseState
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom

/** UI state for the Match B pairing negotiation room screen. */
internal data class PairingRoomUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val showDoubleConfirmDialog: Boolean = false,
    val pairingRoom: PairingRoom? = null,
    val error: AppError? = null,
    val inlineError: AppError? = null,
) : BaseState

/** User and lifecycle events handled by [PairingRoomViewModel]. */
internal sealed interface PairingRoomEvent {
    data class Load(
        val roomId: String,
    ) : PairingRoomEvent

    data class SelectOption(
        val optionId: String,
    ) : PairingRoomEvent

    data class AddOption(
        val input: AddPairingOptionInput,
    ) : PairingRoomEvent

    data object RequestConfirm : PairingRoomEvent

    data object ConfirmPackage : PairingRoomEvent

    data object DismissConfirmDialog : PairingRoomEvent

    data class Dispute(
        val reason: String,
    ) : PairingRoomEvent

    data class Cancel(
        val reason: String,
    ) : PairingRoomEvent

    data object DismissInlineError : PairingRoomEvent
}

/** State reducer actions for [PairingRoomViewModel]. */
internal sealed interface PairingRoomAction : BaseAction<PairingRoomUiState> {
    data object Loading : PairingRoomAction {
        override fun reduce(state: PairingRoomUiState): PairingRoomUiState = state.copy(isLoading = true, error = null, inlineError = null)
    }

    data object Submitting : PairingRoomAction {
        override fun reduce(state: PairingRoomUiState): PairingRoomUiState =
            state.copy(isSubmitting = true, inlineError = null, showDoubleConfirmDialog = false)
    }

    data class RoomLoaded(
        val room: PairingRoom,
    ) : PairingRoomAction {
        override fun reduce(state: PairingRoomUiState): PairingRoomUiState =
            state.copy(
                isLoading = false,
                isSubmitting = false,
                showDoubleConfirmDialog = false,
                pairingRoom = room,
                error = null,
                inlineError = null,
            )
    }

    data class LoadFailed(
        val error: AppError,
    ) : PairingRoomAction {
        override fun reduce(state: PairingRoomUiState): PairingRoomUiState =
            state.copy(isLoading = false, isSubmitting = false, error = error)
    }

    data class MutationFailed(
        val error: AppError,
    ) : PairingRoomAction {
        override fun reduce(state: PairingRoomUiState): PairingRoomUiState =
            state.copy(isSubmitting = false, showDoubleConfirmDialog = false, inlineError = error)
    }

    data class ToggleDoubleConfirm(
        val visible: Boolean,
    ) : PairingRoomAction {
        override fun reduce(state: PairingRoomUiState): PairingRoomUiState = state.copy(showDoubleConfirmDialog = visible)
    }

    data object ClearInlineError : PairingRoomAction {
        override fun reduce(state: PairingRoomUiState): PairingRoomUiState = state.copy(inlineError = null)
    }
}
