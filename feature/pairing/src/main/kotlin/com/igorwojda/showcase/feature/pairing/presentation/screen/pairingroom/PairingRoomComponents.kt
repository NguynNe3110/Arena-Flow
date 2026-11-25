@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber", "TooManyFunctions")

package com.igorwojda.showcase.feature.pairing.presentation.screen.pairingroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.time.VietnamDateTime
import com.igorwojda.showcase.feature.base.domain.model.SelectedVenueResult
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.pairing.domain.model.AddPairingOptionInput
import com.igorwojda.showcase.feature.pairing.domain.model.PairingCaptain
import com.igorwojda.showcase.feature.pairing.domain.model.PairingOption
import com.igorwojda.showcase.feature.pairing.domain.model.PairingRoom
import com.igorwojda.showcase.feature.pairing.domain.model.PairingStatus
import java.text.NumberFormat
import java.util.Locale

/** Live status banner with status badge, revision, and 2-hour countdown deadline. */
@Composable
internal fun PairingStatusBanner(
    room: PairingRoom,
    modifier: Modifier = Modifier,
) {
    val (label, variant) = statusBadgeInfo(room.status)
    val bgColor =
        when (room.status) {
            PairingStatus.CONFIRMED -> ArenaColors.SuccessBg
            PairingStatus.DISPUTED, PairingStatus.CANCELLED -> ArenaColors.ErrorBg
            PairingStatus.NEGOTIATING -> ArenaColors.WarningBg
            PairingStatus.SUPERSEDED, PairingStatus.EXPIRED -> ArenaColors.SurfaceContainer
        }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = bgColor,
        border = BorderStroke(1.dp, ArenaColors.Border),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppBadge(text = label, variant = variant)
                AppBadge(text = "Phiên bản v${room.revision}", variant = BadgeVariant.Neutral)
            }
            if (room.status == PairingStatus.NEGOTIATING) {
                Text(
                    text = "Hạn thương lượng: ${formatDeadlineCountdown(room.deadlineAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = ArenaColors.TextPrimary,
                )
            }
            if (!room.disputeReason.isNullOrBlank()) {
                Text(
                    text = "Lý do tranh chấp: ${room.disputeReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.ErrorBright,
                )
            }
            if (!room.cancelReason.isNullOrBlank()) {
                Text(
                    text = "Lý do hủy: ${room.cancelReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.ErrorBright,
                )
            }
        }
    }
}

/** Side-by-side Captain A and Captain B readiness cards. */
@Composable
internal fun CaptainReadinessSection(
    captainA: PairingCaptain,
    captainB: PairingCaptain,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CaptainReadinessCard(
            roleLabel = "Chủ kèo",
            captain = captainA,
            modifier = Modifier.weight(1f),
        )
        CaptainReadinessCard(
            roleLabel = "Đội nhận kèo",
            captain = captainB,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CaptainReadinessCard(
    roleLabel: String,
    captain: PairingCaptain,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Text(
            text = roleLabel,
            style = MaterialTheme.typography.labelSmall,
            color = ArenaColors.TextMuted,
        )
        Text(
            text = captain.teamName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
        )
        Text(
            text = "ĐT: ${captain.captainName ?: "Đội trưởng"} · Uy tín ${captain.reliabilityScore}",
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.TextMuted,
        )
        AppBadge(
            text = if (captain.hasConfirmed) "Đã xác nhận" else "Chờ xác nhận",
            variant = if (captain.hasConfirmed) BadgeVariant.Success else BadgeVariant.Warning,
        )
    }
}

/** Card displaying a venue, time slot, fee split option and selection button. */
@Composable
internal fun PairingOptionCard(
    option: PairingOption,
    isSelected: Boolean,
    canInteract: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVenueInactive = option.venueStatus.equals("INACTIVE", ignoreCase = true)
    val borderColor = if (isSelected) ArenaColors.Primary else ArenaColors.Border
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = option.venueName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                    modifier = Modifier.weight(1f),
                )
                if (isSelected) {
                    AppBadge(text = "Đang chọn", variant = BadgeVariant.Success)
                } else if (isVenueInactive) {
                    AppBadge(text = "Sân ngừng HĐ", variant = BadgeVariant.Error)
                }
            }
            OptionMetaRow(icon = Icons.Outlined.LocationOn, text = option.venueAddress)
            OptionMetaRow(icon = Icons.Outlined.AccessTime, text = option.dateTimeLabel)
            OptionMetaRow(
                icon = Icons.Outlined.Payments,
                text = "${formatVnd(option.totalCost)} · ${option.feeSplitLabel}",
            )
            if (option.depositAmount > 0) {
                Text(
                    text = "Cọc: ${formatVnd(option.depositAmount)} (${option.depositHolder})",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Đề xuất: ${option.proposedByTeamName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextMuted,
                )
                if (canInteract && !isSelected && !isVenueInactive) {
                    AppOutlinedButton(text = "Chọn phương án", onClick = onSelect)
                }
            }
        }
    }
}

@Composable
private fun OptionMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArenaColors.TextMuted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.TextPrimary,
        )
    }
}

/** Dialog for proposing a new venue/time/fee option with VenuePicker hook. */
@Composable
internal fun AddPairingOptionDialog(
    selectedVenueFromPicker: SelectedVenueResult?,
    onOpenVenuePicker: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (AddPairingOptionInput) -> Unit,
) {
    var venueName by rememberSaveable(selectedVenueFromPicker) {
        mutableStateOf(selectedVenueFromPicker?.venueName ?: "Sân bóng An Phú")
    }
    var venueAddress by rememberSaveable(selectedVenueFromPicker) {
        mutableStateOf(selectedVenueFromPicker?.venueAddress ?: "Số 8 Đường số 2, TP. Thủ Đức")
    }
    var dateTimeLabel by rememberSaveable { mutableStateOf("2026-11-22 · 19:00 - 20:30") }
    var totalCostText by rememberSaveable { mutableStateOf("600000") }
    var feeSplitLabel by rememberSaveable { mutableStateOf("Chia đôi 50-50") }
    var depositText by rememberSaveable { mutableStateOf("200000") }
    var depositHolder by rememberSaveable { mutableStateOf("Đội chủ nhà") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đề xuất phương án mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppOutlinedButton(
                    text = "Chọn sân từ danh sách",
                    onClick = onOpenVenuePicker,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = venueName,
                    onValueChange = { venueName = it },
                    label = { Text("Tên sân") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = venueAddress,
                    onValueChange = { venueAddress = it },
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = dateTimeLabel,
                    onValueChange = { dateTimeLabel = it },
                    label = { Text("Ngày & Khung giờ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = totalCostText,
                    onValueChange = { totalCostText = it.filter(Char::isDigit) },
                    label = { Text("Tổng tiền sân (VND)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = feeSplitLabel,
                    onValueChange = { feeSplitLabel = it },
                    label = { Text("Chia tiền") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = depositText,
                    onValueChange = { depositText = it.filter(Char::isDigit) },
                    label = { Text("Tiền cọc (VND)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = depositHolder,
                    onValueChange = { depositHolder = it },
                    label = { Text("Bên giữ cọc") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Thêm phương án",
                onClick = {
                    val venueId = selectedVenueFromPicker?.venueId ?: "ven-anphu"
                    onSubmit(
                        AddPairingOptionInput(
                            venueId = venueId,
                            venueName = venueName.trim(),
                            venueAddress = venueAddress.trim(),
                            dateTimeLabel = dateTimeLabel.trim(),
                            totalCost = totalCostText.toIntOrNull() ?: 600_000,
                            feeSplitLabel = feeSplitLabel.trim(),
                            depositAmount = depositText.toIntOrNull() ?: 0,
                            depositHolder = depositHolder.trim(),
                        ),
                    )
                },
                enabled = venueName.isNotBlank() && dateTimeLabel.isNotBlank(),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        },
    )
}

/** Confirmation dialog when the other captain has already selected/confirmed. */
@Composable
internal fun DoubleConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận chốt kèo?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Đội trưởng bên kia đã chọn hoặc xác nhận gói này. Xác nhận lần nữa để chốt kèo chính thức.",
            )
        },
        confirmButton = {
            AppButton(text = "Chốt kèo ngay", onClick = onConfirm)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Xem lại") }
        },
    )
}

/** Dialog for entering a dispute or cancellation reason. */
@Composable
internal fun ReasonInputDialog(
    title: String,
    placeholder: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        confirmButton = {
            AppButton(
                text = confirmLabel,
                onClick = { onConfirm(reason.trim()) },
                enabled = reason.isNotBlank(),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy bỏ") }
        },
    )
}

private fun statusBadgeInfo(status: PairingStatus): Pair<String, BadgeVariant> =
    when (status) {
        PairingStatus.NEGOTIATING -> "Đang thương lượng" to BadgeVariant.Warning
        PairingStatus.CONFIRMED -> "Đã chốt kèo" to BadgeVariant.Success
        PairingStatus.DISPUTED -> "Đang tranh chấp" to BadgeVariant.Error
        PairingStatus.CANCELLED -> "Đã hủy" to BadgeVariant.Error
        PairingStatus.SUPERSEDED -> "Đã bị thay thế" to BadgeVariant.Neutral
        PairingStatus.EXPIRED -> "Đã hết hạn" to BadgeVariant.Neutral
    }

private fun formatDeadlineCountdown(deadlineAt: String): String {
    val deadlineMs = VietnamDateTime.parseToEpochMillis(deadlineAt) ?: return deadlineAt
    val nowMs = VietnamDateTime.currentEpochMillis()
    val diffMinutes = ((deadlineMs - nowMs) / 60_000L).coerceAtLeast(0L)
    val hours = diffMinutes / 60L
    val minutes = diffMinutes % 60L
    return if (diffMinutes == 0L) "Đã đến hạn ($deadlineAt)" else "Còn ${hours}g ${minutes}p ($deadlineAt)"
}

private fun formatVnd(amount: Int): String {
    val fmt = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
    return "${fmt.format(amount)} đ"
}
