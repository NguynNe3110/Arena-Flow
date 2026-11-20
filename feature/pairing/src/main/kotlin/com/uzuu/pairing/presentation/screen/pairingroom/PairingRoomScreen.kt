package com.uzuu.pairing.presentation.screen.pairingroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.uzuu.pairing.domain.model.PairingChatMessage
import com.uzuu.pairing.presentation.composable.ControlShape
import com.uzuu.pairing.presentation.composable.MatchShape
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PairingRoomScreen(
    pairingId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PairingRoomViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pairingId) {
        viewModel.load(pairingId)
    }

    PairingRoomContent(
        uiState = uiState,
        onSelectVenue = viewModel::selectVenue,
        onSelectTimeSlot = viewModel::selectTimeSlot,
        onConfirmPackage = viewModel::confirmPackage,
        onSendMessage = viewModel::sendChatMessage,
        onRetry = { viewModel.load(pairingId) },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PairingRoomContent(
    uiState: PairingRoomUiState,
    onSelectVenue: (String) -> Unit,
    onSelectTimeSlot: (String) -> Unit,
    onConfirmPackage: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var messageText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Thương lượng ghép kèo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            uiState.pairingRoom?.code ?: "Mã phòng: #SG-BM-2111",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { LoadingIndicator() }
            }

            uiState.error != null -> {
                Box(Modifier.fillMaxSize().padding(padding)) { AppErrorContent(uiState.error, onRetry) }
            }

            uiState.pairingRoom != null -> {
                val room = uiState.pairingRoom
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 1. BANNER ĐỐI ĐẦU
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MatchShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                // Host Team
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Surface(Modifier.size(44.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                room.hostTeam.shortName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                    Text(
                                        room.hostTeam.teamName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                    Surface(shape = ControlShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                        Text(
                                            room.hostTeam.statusLabel,
                                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }

                                Text(
                                    "⚔️ VS",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )

                                // Guest Team
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Surface(Modifier.size(44.dp), CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                room.guestTeam.shortName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                    Text(
                                        room.guestTeam.teamName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                    Surface(shape = ControlShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(
                                            room.guestTeam.statusLabel,
                                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.SwapHoriz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    "Chat trao đổi và cùng thống nhất gói sân + giờ + chi phí bên dưới.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // 2. KHỐI GÓI THỎA THUẬN KÈO ĐẤU
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MatchShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "GÓI THỎA THUẬN KÈO ĐẤU",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Surface(shape = ControlShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(
                                        room.version,
                                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            // Tùy chọn Sân
                            Text(
                                "Địa điểm thi đấu đề xuất",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            room.venueOptions.forEach { venue ->
                                val isSelected = room.selectedVenueId == venue.venueId
                                val bgColor =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                val borderStroke =
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    )
                                Surface(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectVenue(venue.venueId) },
                                    shape = ControlShape,
                                    color = bgColor,
                                    border = borderStroke,
                                ) {
                                    Row(
                                        Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        RadioButton(selected = isSelected, onClick = null)
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                venue.venueName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                venue.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (isSelected) {
                                            Surface(shape = ControlShape, color = MaterialTheme.colorScheme.primary) {
                                                Text(
                                                    "Đã chọn",
                                                    Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Tùy chọn Giờ
                            Text("Khung giờ thi đấu", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            room.timeOptions.forEach { slot ->
                                val isSelected = room.selectedTimeSlot == slot
                                val slotBgColor =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                val slotBorder =
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    )
                                Surface(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectTimeSlot(slot) },
                                    shape = ControlShape,
                                    color = slotBgColor,
                                    border = slotBorder,
                                ) {
                                    Row(
                                        Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        RadioButton(selected = isSelected, onClick = null)
                                        Column(Modifier.weight(1f)) {
                                            Text(slot, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Thứ Bảy, 21/11/2026 • 90 Phút",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            // Chi phí & Tiền cọc ngoài
                            Surface(shape = ControlShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(Icons.Outlined.Payments, null, modifier = Modifier.size(16.dp))
                                            Text("Tổng tiền sân:", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            val currencyFmt = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
                                            val costFormatted = "${currencyFmt.format(room.totalCost)} đ"
                                            Text(costFormatted, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(
                                                "Chia 50-50 (300.000đ/đội)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                Icons.Outlined.LockClock,
                                                null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Text("Tiền cọc giữ kèo:", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            val currencyFmt = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
                                            val depFormatted = "${currencyFmt.format(room.depositAmount)} đ"
                                            Text(
                                                depFormatted,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            Text(
                                                room.depositHolder,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Text(
                                        "ArenaFlow không can thiệp, không thu phí hay giữ tiền cọc của người chơi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            // Trạng thái xác nhận đôi bên
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "TRẠNG THÁI XÁC NHẬN ĐÔI BÊN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline,
                                )

                                // Host confirmation
                                val hostBg =
                                    if (room.hostConfirmed) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                val hostBorder =
                                    BorderStroke(
                                        1.dp,
                                        if (room.hostConfirmed) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                    )
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ControlShape,
                                    color = hostBg,
                                    border = hostBorder,
                                ) {
                                    Row(
                                        Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            room.hostTeam.teamName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            if (room.hostConfirmed) "✓ Đã bấm xác nhận" else "⏳ Chưa xác nhận",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color =
                                                if (room.hostConfirmed) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                        )
                                    }
                                }

                                // Guest confirmation
                                val guestBg =
                                    if (room.guestConfirmed) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                val guestBorder =
                                    BorderStroke(
                                        1.dp,
                                        if (room.guestConfirmed) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant
                                        },
                                    )
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ControlShape,
                                    color = guestBg,
                                    border = guestBorder,
                                ) {
                                    Row(
                                        Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            "${room.guestTeam.teamName} (Bạn)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            if (room.guestConfirmed) "✓ Đã bấm xác nhận" else "⏳ Chưa xác nhận",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color =
                                                if (room.guestConfirmed) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                        )
                                    }
                                }

                                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        Icons.Outlined.ErrorOutline,
                                        null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        "Lưu ý: Khi một bên thay đổi phương án sân/giờ hoặc chi phí, " +
                                            "xác nhận của 2 bên sẽ tự động hủy bỏ để bảo đảm " +
                                            "đồng thuận (AC-P04).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    // 3. KHỐI CHAT TRAO ĐỔI
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MatchShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Trao đổi tin nhắn", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                room.chatMessages.forEach { msg ->
                                    ChatMessageBubble(msg)
                                }
                            }

                            Row(
                                Modifier.fillMaxWidth().padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedTextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    placeholder = { Text("Nhập tin nhắn...") },
                                    modifier = Modifier.weight(1f),
                                    shape = ControlShape,
                                )
                                IconButton(
                                    onClick = {
                                        if (messageText.isNotBlank()) {
                                            onSendMessage(messageText)
                                            messageText = ""
                                        }
                                    },
                                    enabled = messageText.isNotBlank(),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Gửi tin nhắn",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    // Nút hành động xác nhận gói
                    Button(
                        onClick = onConfirmPackage,
                        enabled = !room.guestConfirmed && !uiState.isConfirming,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = ControlShape,
                    ) {
                        if (uiState.isConfirming) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            val buttonText =
                                if (room.guestConfirmed) {
                                    "✓ Bạn đã xác nhận gói thương lượng"
                                } else {
                                    "Xác nhận gói thương lượng"
                                }
                            Text(buttonText, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(msg: PairingChatMessage) {
    val alignment = if (msg.isMe) Alignment.End else Alignment.Start
    val bgColor = if (msg.isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val textColor = if (msg.isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Text(msg.senderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Surface(
            shape = ControlShape,
            color = bgColor,
            modifier = Modifier.padding(top = 2.dp),
        ) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(msg.message, style = MaterialTheme.typography.bodyMedium, color = textColor)
                Text(
                    msg.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}
