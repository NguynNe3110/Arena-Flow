package com.uzuu.match.presentation.screen.matchcreate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.MatchSection
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchCreateScreen(
    onNavigateToReview: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchCreateViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatchCreateContent(
        uiState = uiState,
        onUpdateDraft = viewModel::updateDraft,
        onSubmit = { viewModel.validateAndProceed(onNavigateToReview) },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun MatchCreateContent(
    uiState: MatchCreateUiState,
    onUpdateDraft: ((com.uzuu.match.domain.model.MatchDraft) -> com.uzuu.match.domain.model.MatchDraft) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = uiState.draft

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Đăng kèo giao lưu", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Notice alert
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ControlShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Lưu ý quan trọng", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Không thể chỉnh sửa thông tin kèo sau khi đăng để đảm bảo tính minh bạch. " +
                                "Nếu cần thay đổi, bạn có thể Hủy kèo và tạo lại kèo mới.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Segmented Tabs: Kèo A vs Kèo B
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val isKindA = draft.kind == MatchType.HAS_VENUE
                Button(
                    onClick = { onUpdateDraft { it.copy(kind = MatchType.HAS_VENUE) } },
                    shape = ControlShape,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors =
                        if (isKindA) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                    border = if (!isKindA) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                ) {
                    Text("ĐÃ CÓ SÂN (Luồng A)", style = MaterialTheme.typography.labelMedium)
                }
                Button(
                    onClick = { onUpdateDraft { it.copy(kind = MatchType.NEED_VENUE) } },
                    shape = ControlShape,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors =
                        if (!isKindA) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                    border = if (isKindA) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                ) {
                    Text("CHƯA CÓ SÂN (Luồng B)", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Section: Đội bóng của bạn
            MatchSection("Đội bóng của bạn", Modifier.fillMaxWidth()) {
                uiState.availableTeams.forEach { (id, name) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateDraft { it.copy(hostTeamId = id, hostTeamName = name) } }
                                .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RadioButton(selected = draft.hostTeamId == id, onClick = null)
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Section: Định dạng sân & Trình độ
            MatchSection("Định dạng & Trình độ", Modifier.fillMaxWidth()) {
                Text("Định dạng sân", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SEVEN_A_SIDE" to "Sân 7", "FIVE_A_SIDE" to "Sân 5", "ELEVEN_A_SIDE" to "Sân 11").forEach { (fmt, label) ->
                        FilterChip(
                            selected = draft.format == fmt,
                            onClick = { onUpdateDraft { it.copy(format = fmt) } },
                            label = { Text(label) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = ControlShape,
                        )
                    }
                }
                Text("Trình độ nhận kèo", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Trung bình", "Mới chơi", "Khá / Chuyên").forEach { skill ->
                        FilterChip(
                            selected = draft.skillLevel == skill,
                            onClick = { onUpdateDraft { it.copy(skillLevel = skill) } },
                            label = { Text(skill) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = ControlShape,
                        )
                    }
                }
            }

            // Kind A: Đã có sân (Cố định 1 sân + 1 giờ)
            if (draft.kind == MatchType.HAS_VENUE) {
                MatchSection("Địa điểm & Khung giờ cố định", Modifier.fillMaxWidth()) {
                    Text("Sân bóng đã đặt", style = MaterialTheme.typography.titleSmall)
                    uiState.availableVenues.forEach { venue ->
                        val isSelected = draft.venueId == venue.venueId
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onUpdateDraft {
                                            it.copy(
                                                venueId = venue.venueId,
                                                venueName = venue.venueName,
                                                venueAddress = venue.address,
                                            )
                                        }
                                    },
                            shape = ControlShape,
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            border =
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                ),
                        ) {
                            Column(Modifier.padding(10.dp)) {
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
                        }
                    }
                    if (uiState.errors.containsKey("venue")) {
                        Text(
                            uiState.errors["venue"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    OutlinedTextField(
                        value = draft.matchDate,
                        onValueChange = { d -> onUpdateDraft { it.copy(matchDate = d) } },
                        label = { Text("Ngày thi đấu (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ControlShape,
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = draft.startTime,
                            onValueChange = { t -> onUpdateDraft { it.copy(startTime = t) } },
                            label = { Text("Giờ bắt đầu (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            shape = ControlShape,
                        )
                        OutlinedTextField(
                            value = draft.endTime,
                            onValueChange = { t -> onUpdateDraft { it.copy(endTime = t) } },
                            label = { Text("Giờ kết thúc (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            shape = ControlShape,
                        )
                    }
                    if (uiState.errors.containsKey("time")) {
                        Text(
                            uiState.errors["time"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else {
                // Kind B: Chưa có sân (1-5 sân đề xuất + 1-5 giờ đề xuất)
                MatchSection("Đề xuất sân & Khung giờ linh hoạt", Modifier.fillMaxWidth()) {
                    Text("Đề xuất 1 - 5 sân bóng", style = MaterialTheme.typography.titleSmall)
                    uiState.availableVenues.forEach { venue ->
                        val isChecked = draft.proposedVenues.any { it.venueId == venue.venueId }
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val current = draft.proposedVenues.toMutableList()
                                        if (isChecked) {
                                            current.removeAll { it.venueId == venue.venueId }
                                        } else {
                                            current.add(venue)
                                        }
                                        onUpdateDraft { it.copy(proposedVenues = current) }
                                    }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(checked = isChecked, onCheckedChange = null)
                            Column {
                                Text(venue.venueName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    venue.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    if (uiState.errors.containsKey("venues")) {
                        Text(
                            uiState.errors["venues"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    OutlinedTextField(
                        value = draft.matchDate,
                        onValueChange = { d -> onUpdateDraft { it.copy(matchDate = d) } },
                        label = { Text("Ngày thi đấu dự kiến (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ControlShape,
                    )

                    Text("Đề xuất 1 - 5 khung giờ", style = MaterialTheme.typography.titleSmall)
                    uiState.availableTimeSlots.forEach { slot ->
                        val isChecked = draft.proposedTimeSlots.contains(slot)
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val current = draft.proposedTimeSlots.toMutableList()
                                        if (isChecked) {
                                            current.remove(slot)
                                        } else {
                                            current.add(slot)
                                        }
                                        onUpdateDraft { it.copy(proposedTimeSlots = current) }
                                    }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(checked = isChecked, onCheckedChange = null)
                            Text(slot, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (uiState.errors.containsKey("slots")) {
                        Text(
                            uiState.errors["slots"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    OutlinedTextField(
                        value = if (draft.depositAmount == 0) "" else draft.depositAmount.toString(),
                        onValueChange = { v -> onUpdateDraft { it.copy(depositAmount = v.toIntOrNull() ?: 0) } },
                        label = { Text("Tiền cọc yêu cầu (VND)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ControlShape,
                    )
                }
            }

            // Section: Thỏa thuận chi phí & Chia tiền
            MatchSection("Thỏa thuận chi phí & Chia tiền", Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = draft.totalCost.toString(),
                    onValueChange = { v -> onUpdateDraft { it.copy(totalCost = v.toIntOrNull() ?: 0) } },
                    label = { Text("Tổng chi phí ước tính (VND)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = ControlShape,
                )
                Text("Cách chia tiền", style = MaterialTheme.typography.titleSmall)
                listOf(
                    "SPLIT_50_50" to "Chia đôi 50/50",
                    "HOST_100" to "Đội nhà bao sân",
                    "SEVENTY_THIRTY_LOSER" to "70/30 (Đội thua 70%)",
                    "LOSER_PAYS_DRINKS" to "Đội thua trả nước",
                ).forEach { (splitKey, label) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateDraft { it.copy(costSharing = splitKey) } }
                                .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RadioButton(selected = draft.costSharing == splitKey, onClick = null)
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Section: Ghi chú giao lưu
            MatchSection("Ghi chú giao lưu", Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = { n -> onUpdateDraft { it.copy(notes = n) } },
                    label = { Text("Màu áo, tính chất trận đấu, yêu cầu...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = ControlShape,
                )
            }

            // Submit Buttons
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = ControlShape,
            ) {
                Text("Xem lại & Đăng kèo đấu", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = ControlShape,
            ) {
                Text("Hủy bỏ", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
