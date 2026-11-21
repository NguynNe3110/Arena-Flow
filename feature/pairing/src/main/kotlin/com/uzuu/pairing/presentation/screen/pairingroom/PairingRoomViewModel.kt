package com.uzuu.pairing.presentation.screen.pairingroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.domain.result.Result
import com.uzuu.pairing.domain.model.PairingChatMessage
import com.uzuu.pairing.domain.model.PairingRoom
import com.uzuu.pairing.domain.usecase.ConfirmPackageUseCase
import com.uzuu.pairing.domain.usecase.GetPairingRoomUseCase
import com.uzuu.pairing.domain.usecase.SelectPairingOptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

internal data class PairingRoomUiState(
    val isLoading: Boolean = true,
    val pairingRoom: PairingRoom? = null,
    val error: AppError? = null,
    val isConfirming: Boolean = false,
)

internal class PairingRoomViewModel(
    private val getPairingRoomUseCase: GetPairingRoomUseCase,
    private val confirmPackageUseCase: ConfirmPackageUseCase,
    private val selectPairingOptionUseCase: SelectPairingOptionUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PairingRoomUiState())
    val uiState = mutableState.asStateFlow()

    fun load(pairingId: String) {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isLoading = true, error = null)
            when (val result = getPairingRoomUseCase(pairingId)) {
                is Result.Success<PairingRoom> -> {
                    mutableState.value =
                        PairingRoomUiState(
                            isLoading = false,
                            pairingRoom = result.value,
                        )
                }

                is Result.Failure -> {
                    mutableState.value =
                        PairingRoomUiState(
                            isLoading = false,
                            error = result.error,
                        )
                }
            }
        }
    }

    fun selectVenue(venueId: String) {
        val room = mutableState.value.pairingRoom ?: return
        viewModelScope.launch {
            when (val result = selectPairingOptionUseCase(room.pairingId, venueId, null)) {
                is Result.Success<PairingRoom> -> mutableState.value = mutableState.value.copy(pairingRoom = result.value)
                is Result.Failure -> mutableState.value = mutableState.value.copy(error = result.error)
            }
        }
    }

    fun selectTimeSlot(timeSlot: String) {
        val room = mutableState.value.pairingRoom ?: return
        viewModelScope.launch {
            when (val result = selectPairingOptionUseCase(room.pairingId, null, timeSlot)) {
                is Result.Success<PairingRoom> -> mutableState.value = mutableState.value.copy(pairingRoom = result.value)
                is Result.Failure -> mutableState.value = mutableState.value.copy(error = result.error)
            }
        }
    }

    fun confirmPackage() {
        val room = mutableState.value.pairingRoom ?: return
        if (mutableState.value.isConfirming || room.guestConfirmed) return
        mutableState.value = mutableState.value.copy(isConfirming = true)

        viewModelScope.launch {
            when (val result = confirmPackageUseCase(room.pairingId)) {
                is Result.Success<PairingRoom> -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isConfirming = false,
                            pairingRoom = result.value,
                        )
                }

                is Result.Failure -> {
                    mutableState.value =
                        mutableState.value.copy(
                            isConfirming = false,
                            error = result.error,
                        )
                }
            }
        }
    }

    fun sendChatMessage(text: String) {
        val room = mutableState.value.pairingRoom ?: return
        val newMsg =
            PairingChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = room.guestTeam.teamName,
                message = text,
                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                isMe = true,
            )
        val updatedMessages = room.chatMessages + newMsg
        mutableState.value =
            mutableState.value.copy(
                pairingRoom = room.copy(chatMessages = updatedMessages),
            )
    }
}
