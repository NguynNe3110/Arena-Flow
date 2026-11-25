package com.igorwojda.showcase.feature.admin.presentation.screen.adminconsole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.admin.domain.model.ReportDecision
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton

@Composable
internal fun AdminConsoleDialogHost(
    dialog: AdminConsoleDialog,
    isSubmitting: Boolean,
    onEvent: (AdminConsoleEvent) -> Unit,
) {
    when (dialog) {
        is AdminConsoleDialog.ResolveReport -> {
            ResolveReportDialog(
                dialog = dialog,
                isSubmitting = isSubmitting,
                onDismiss = { onEvent(AdminConsoleEvent.DismissDialog) },
                onSubmit = { decision, note ->
                    onEvent(AdminConsoleEvent.SubmitReportDecision(dialog.report.reportId, decision, note))
                },
            )
        }

        is AdminConsoleDialog.RejectVenue -> {
            RejectVenueDialog(
                venueName = dialog.venue.venueName,
                isSubmitting = isSubmitting,
                onDismiss = { onEvent(AdminConsoleEvent.DismissDialog) },
                onSubmit = { reason ->
                    onEvent(AdminConsoleEvent.SubmitRejectVenue(dialog.venue.venueId, reason))
                },
            )
        }

        is AdminConsoleDialog.BanUser -> {
            BanUserDialog(
                userName = dialog.user.displayName,
                isSubmitting = isSubmitting,
                onDismiss = { onEvent(AdminConsoleEvent.DismissDialog) },
                onSubmit = { reason ->
                    onEvent(AdminConsoleEvent.SubmitBanUser(dialog.user.userId, reason))
                },
            )
        }

        is AdminConsoleDialog.AdjustReputation -> {
            AdjustReputationDialog(
                userName = dialog.user.displayName,
                currentScore = dialog.user.reputationScore,
                isSubmitting = isSubmitting,
                onDismiss = { onEvent(AdminConsoleEvent.DismissDialog) },
                onSubmit = { delta, reason ->
                    onEvent(AdminConsoleEvent.SubmitAdjustReputation(dialog.user.userId, delta, reason))
                },
            )
        }
    }
}

@Composable
private fun ResolveReportDialog(
    dialog: AdminConsoleDialog.ResolveReport,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ReportDecision, String?) -> Unit,
) {
    var decision by remember { mutableStateOf(ReportDecision.CONFIRM) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quyết định báo cáo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${dialog.report.reporterName} → ${dialog.report.reportedName}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                ReportDecision.entries.forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = decision == option,
                            onClick = { decision = option },
                        )
                        Text(text = option.label)
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú quản trị viên (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Xác nhận",
                isLoading = isSubmitting,
                onClick = { onSubmit(decision, note.trim().takeIf { it.isNotEmpty() }) },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}

@Composable
private fun RejectVenueDialog(
    venueName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Từ chối sân: $venueName") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Lý do từ chối") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            AppButton(
                text = "Từ chối sân",
                enabled = reason.isNotBlank(),
                isLoading = isSubmitting,
                onClick = { onSubmit(reason.trim()) },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}

@Composable
private fun BanUserDialog(
    userName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Khóa tài khoản: $userName") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Lý do khóa tài khoản") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            AppButton(
                text = "Khóa tài khoản",
                enabled = reason.isNotBlank(),
                isLoading = isSubmitting,
                onClick = { onSubmit(reason.trim()) },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}

@Composable
private fun AdjustReputationDialog(
    userName: String,
    currentScore: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
) {
    var deltaText by remember { mutableStateOf("-5") }
    var reason by remember { mutableStateOf("") }
    val parsedDelta = deltaText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Điều chỉnh uy tín: $userName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Điểm hiện tại: $currentScore/100")
                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it },
                    label = { Text("Mức thay đổi (vd: -5 hoặc +10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Lý do điều chỉnh") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Cập nhật",
                enabled = parsedDelta != null && parsedDelta != 0 && reason.isNotBlank(),
                isLoading = isSubmitting,
                onClick = { parsedDelta?.let { onSubmit(it, reason.trim()) } },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        },
    )
}
