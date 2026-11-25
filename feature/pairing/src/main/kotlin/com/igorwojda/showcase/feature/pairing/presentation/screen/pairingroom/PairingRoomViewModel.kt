package com.igorwojda.showcase.feature.pairing.presentation.screen.pairingroom

import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.igorwojda.showcase.feature.base.presentation.viewmodel.BaseViewModel
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.CaptainRole
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.usecase.AddPairingOptionUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.CancelPairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.ConfirmPairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.DisputePairingUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.GetPairingRoomUseCase
import com.igorwojda.showcase.feature.pairing.domain.usecase.SelectPairingOptionUseCase
import kotlinx.coroutines.launch

/** ViewModel for the Match B pairing negotiation room screen. */
internal class PairingRoomViewModel(
    private val addPairingOptionUseCase: AddPairingOptionUseCase,
    private val cancelPairingUseCase: CancelPairingUseCase,
    private val confirmPairingUseCase: ConfirmPairingUseCase,
    private val disputePairingUseCase: DisputePairingUseCase,
    private val getPairingRoomUseCase: GetPairingRoomUseCase,
    private val selectPairingOptionUseCase: SelectPairingOptionUseCase,
) : BaseViewModel<PairingRoomUiState, PairingRoomAction>(PairingRoomUiState()) {
    /** Dispatches a UI event to update pairing room state. */
    fun onEvent(event: PairingRoomEvent) {
        when (event) {
            is PairingRoomEvent.Load -> loadRoom(event.roomId)
            is PairingRoomEvent.SelectOption -> selectOption(event.optionId)
            is PairingRoomEvent.AddOption -> addOption(event.input)
            PairingRoomEvent.RequestConfirm -> handleRequestConfirm()
            PairingRoomEvent.ConfirmPackage -> confirmPackage()
            PairingRoomEvent.DismissConfirmDialog -> sendAction(PairingRoomAction.ToggleDoubleConfirm(false))
            is PairingRoomEvent.Dispute -> disputeRoom(event.reason)
            is PairingRoomEvent.Cancel -> cancelRoom(event.reason)
            PairingRoomEvent.DismissInlineError -> sendAction(PairingRoomAction.ClearInlineError)
        }
    }

    private fun loadRoom(roomId: String) {
        sendAction(PairingRoomAction.Loading)
        viewModelScope.launch {
            when (val result = getPairingRoomUseCase(roomId)) {
                is Result.Success -> sendAction(PairingRoomAction.RoomLoaded(result.value))
                is Result.Failure -> sendAction(PairingRoomAction.LoadFailed(result.error))
            }
        }
    }

    private fun selectOption(optionId: String) {
        val room = uiStateFlow.value.pairingRoom ?: return
        executeMutation { selectPairingOptionUseCase(room.roomId, optionId) }
    }

    private fun addOption(input: AddPairingOptionInput) {
        val room = uiStateFlow.value.pairingRoom ?: return
        executeMutation { addPairingOptionUseCase(room.roomId, input) }
    }

    private fun handleRequestConfirm() {
        val room = uiStateFlow.value.pairingRoom ?: return
        val otherSelected =
            when (room.currentUserRole) {
                CaptainRole.CAPTAIN_A -> room.captainB.selectedOptionId != null || room.captainB.hasConfirmed
                CaptainRole.CAPTAIN_B -> room.captainA.selectedOptionId != null || room.captainA.hasConfirmed
                CaptainRole.OBSERVER -> false
            }
        if (otherSelected) {
            sendAction(PairingRoomAction.ToggleDoubleConfirm(true))
        } else {
            confirmPackage()
        }
    }

    private fun confirmPackage() {
        val room = uiStateFlow.value.pairingRoom ?: return
        val optionId = room.selectedOptionId ?: return
        executeMutation { confirmPairingUseCase(room.roomId, room.revision, optionId) }
    }

    private fun disputeRoom(reason: String) {
        val room = uiStateFlow.value.pairingRoom ?: return
        executeMutation { disputePairingUseCase(room.roomId, reason) }
    }

    private fun cancelRoom(reason: String) {
        val room = uiStateFlow.value.pairingRoom ?: return
        executeMutation { cancelPairingUseCase(room.roomId, reason) }
    }

    private fun executeMutation(block: suspend () -> Result<PairingRoom>) {
        if (uiStateFlow.value.isSubmitting) return
        sendAction(PairingRoomAction.Submitting)
        viewModelScope.launch {
            when (val result = block()) {
                is Result.Success -> sendAction(PairingRoomAction.RoomLoaded(result.value))
                is Result.Failure -> sendAction(PairingRoomAction.MutationFailed(result.error))
            }
        }
    }
}
