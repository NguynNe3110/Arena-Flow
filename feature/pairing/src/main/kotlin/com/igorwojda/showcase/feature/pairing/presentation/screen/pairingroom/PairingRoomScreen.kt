@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList")

package com.igorwojda.showcase.feature.pairing.presentation.screen.pairingroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.pairing.domain.model.CaptainRole
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.model.PairingStatus
import org.koin.androidx.compose.koinViewModel

/** Screen S6: Match B pairing negotiation room (`PAIR-01..08`). */
@Composable
fun PairingRoomScreen(
    roomId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenVenuePicker: () -> Unit = {},
    onOpenChat: (conversationId: String) -> Unit = {},
    onOpenPostMatchReport: (matchId: String) -> Unit = {},
    selectedVenueFromPicker: SelectedVenueResult? = null,
) {
    val viewModel: PairingRoomViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(roomId) {
        viewModel.onEvent(PairingRoomEvent.Load(roomId))
    }

    PairingRoomContent(
        uiState = uiState,
        selectedVenueFromPicker = selectedVenueFromPicker,
        onEvent = viewModel::onEvent,
        onRetry = { viewModel.onEvent(PairingRoomEvent.Load(roomId)) },
        onBackClick = onBackClick,
        onOpenVenuePicker = onOpenVenuePicker,
        onOpenChat = onOpenChat,
        onOpenPostMatchReport = onOpenPostMatchReport,
        modifier = modifier,
    )
}

@Composable
internal fun PairingRoomContent(
    uiState: PairingRoomUiState,
    selectedVenueFromPicker: SelectedVenueResult?,
    onEvent: (PairingRoomEvent) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    onOpenVenuePicker: () -> Unit,
    onOpenChat: (conversationId: String) -> Unit,
    onOpenPostMatchReport: (matchId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddOptionDialog by rememberSaveable { mutableStateOf(false) }
    var showDisputeDialog by rememberSaveable { mutableStateOf(false) }
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ArenaTopBar(
                title = "Phòng thương lượng",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                ArenaLoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            }

            uiState.error != null -> {
                ArenaErrorState(
                    title = "Không thể tải phòng thương lượng",
                    message = uiState.error.userMessage(),
                    onBackClick = onBackClick,
                    onRetryClick = onRetry,
                    modifier = Modifier.padding(padding).padding(16.dp),
                )
            }

            uiState.pairingRoom != null -> {
                PairingRoomBody(
                    room = uiState.pairingRoom,
                    uiState = uiState,
                    onEvent = onEvent,
                    onOpenAddOption = { showAddOptionDialog = true },
                    onOpenDispute = { showDisputeDialog = true },
                    onOpenCancel = { showCancelDialog = true },
                    onOpenChat = onOpenChat,
                    onOpenPostMatchReport = onOpenPostMatchReport,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (showAddOptionDialog) {
        AddPairingOptionDialog(
            selectedVenueFromPicker = selectedVenueFromPicker,
            onOpenVenuePicker = onOpenVenuePicker,
            onDismiss = { showAddOptionDialog = false },
            onSubmit = { input ->
                showAddOptionDialog = false
                onEvent(PairingRoomEvent.AddOption(input))
            },
        )
    }

    if (uiState.showDoubleConfirmDialog) {
        DoubleConfirmDialog(
            onConfirm = { onEvent(PairingRoomEvent.ConfirmPackage) },
            onDismiss = { onEvent(PairingRoomEvent.DismissConfirmDialog) },
        )
    }

    if (showDisputeDialog) {
        ReasonInputDialog(
            title = "Gửi tranh chấp",
            placeholder = "Nhập lý do tranh chấp...",
            confirmLabel = "Gửi tranh chấp",
            onConfirm = { reason ->
                showDisputeDialog = false
                onEvent(PairingRoomEvent.Dispute(reason))
            },
            onDismiss = { showDisputeDialog = false },
        )
    }

    if (showCancelDialog) {
        ReasonInputDialog(
            title = "Hủy phòng thương lượng",
            placeholder = "Nhập lý do hủy...",
            confirmLabel = "Xác nhận hủy",
            onConfirm = { reason ->
                showCancelDialog = false
                onEvent(PairingRoomEvent.Cancel(reason))
            },
            onDismiss = { showCancelDialog = false },
        )
    }
}

@Composable
private fun PairingRoomBody(
    room: PairingRoom,
    uiState: PairingRoomUiState,
    onEvent: (PairingRoomEvent) -> Unit,
    onOpenAddOption: () -> Unit,
    onOpenDispute: () -> Unit,
    onOpenCancel: () -> Unit,
    onOpenChat: (conversationId: String) -> Unit,
    onOpenPostMatchReport: (matchId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canNegotiate = room.status == PairingStatus.NEGOTIATING
    val currentConfirmed =
        when (room.currentUserRole) {
            CaptainRole.CAPTAIN_A -> room.captainA.hasConfirmed
            CaptainRole.CAPTAIN_B -> room.captainB.hasConfirmed
            CaptainRole.OBSERVER -> true
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PairingStatusBanner(room = room)

        if (uiState.inlineError != null) {
            InlineNotice(text = uiState.inlineError.userMessage())
        }

        CaptainReadinessSection(captainA = room.captainA, captainB = room.captainB)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Phương án đề xuất (${room.options.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )
            if (canNegotiate) {
                AppOutlinedButton(text = "+ Đề xuất", onClick = onOpenAddOption)
            }
        }

        room.options.forEach { option ->
            PairingOptionCard(
                option = option,
                isSelected = room.selectedOptionId == option.optionId,
                canInteract = canNegotiate,
                onSelect = { onEvent(PairingRoomEvent.SelectOption(option.optionId)) },
            )
        }

        if (canNegotiate && room.selectedOptionId != null) {
            AppButton(
                text = if (currentConfirmed) "Đã xác nhận, chờ đối thủ" else "Xác nhận gói thương lượng",
                onClick = { onEvent(PairingRoomEvent.RequestConfirm) },
                enabled = !currentConfirmed,
                isLoading = uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AppSecondaryButton(
            text = "Mở phòng chat",
            onClick = { onOpenChat(room.conversationId) },
            modifier = Modifier.fillMaxWidth(),
        )

        if (room.status == PairingStatus.CONFIRMED) {
            AppButton(
                text = "Báo cáo sau trận",
                onClick = { onOpenPostMatchReport(room.matchId) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (room.status == PairingStatus.NEGOTIATING || room.status == PairingStatus.CONFIRMED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppOutlinedButton(
                    text = "Tranh chấp",
                    onClick = onOpenDispute,
                    modifier = Modifier.weight(1f),
                )
                AppOutlinedButton(
                    text = "Hủy phòng",
                    onClick = onOpenCancel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
