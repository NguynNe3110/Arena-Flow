@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.match.presentation.screen.matchcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.match.domain.model.CostSplit
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import org.koin.androidx.compose.koinViewModel

/** S5 Match Create / Edit screen (MATCH-05). */
@Composable
fun MatchCreateScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenVenuePicker: (String) -> Unit = {},
    onNavigateToReview: () -> Unit = {},
    onSavedSuccess: (String) -> Unit = {},
    matchId: String? = null,
) {
    val viewModel: MatchCreateViewModel = koinViewModel()
    val uiState = viewModel.uiStateFlow.collectAsStateWithLifecycle().value
    val currentOnSavedSuccess by androidx.compose.runtime.rememberUpdatedState(onSavedSuccess)

    LaunchedEffect(matchId) {
        viewModel.onEvent(MatchCreateEvent.Init(matchId))
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(MatchCreateEvent.ConsumePendingVenue)
    }

    LaunchedEffect(uiState.savedMatchId) {
        val savedId = uiState.savedMatchId ?: return@LaunchedEffect
        currentOnSavedSuccess(savedId)
    }

    MatchCreateContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onOpenVenuePicker = { onOpenVenuePicker(MATCH_VENUE_PICKER_REQUEST_KEY) },
        onNavigateToReview = onNavigateToReview,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MatchCreateContent(
    uiState: MatchCreateUiState,
    onEvent: (MatchCreateEvent) -> Unit,
    onBackClick: () -> Unit,
    onOpenVenuePicker: () -> Unit,
    onNavigateToReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = uiState.draft

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PitchBrandHeader(
                compact = true,
                showBack = true,
                onBackClick = onBackClick,
                title = if (uiState.isEditMode) "Chỉnh sửa kèo" else "Đăng kèo mới",
                subtitle = draft.hostTeamName.ifBlank { "Chọn sân, giờ đá và mức chia tiền" },
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (!uiState.validationError.isNullOrBlank()) {
                    InlineNotice(text = uiState.validationError)
                }

                if (!uiState.isEditMode) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Loại kèo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppFilterChip(
                                text = "Đã có sân (Luồng A)",
                                selected = draft.kind == MatchType.HAS_VENUE,
                                onClick = { onEvent(MatchCreateEvent.UpdateDraft { it.copy(kind = MatchType.HAS_VENUE) }) },
                                modifier = Modifier.weight(1f),
                            )
                            AppFilterChip(
                                text = "Chưa có sân (Luồng B)",
                                selected = draft.kind == MatchType.NEED_VENUE,
                                onClick = { onEvent(MatchCreateEvent.UpdateDraft { it.copy(kind = MatchType.NEED_VENUE) }) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Địa điểm thi đấu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (draft.kind == MatchType.HAS_VENUE) {
                        Text(
                            text = draft.venueName ?: "Chưa chọn sân bóng",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (draft.venueName != null) ArenaColors.TextHeading else ArenaColors.TextMuted,
                        )
                        if (!draft.venueAddress.isNullOrBlank()) {
                            Text(text = draft.venueAddress, style = MaterialTheme.typography.bodySmall, color = ArenaColors.TextMuted)
                        }
                    } else {
                        Text(
                            text =
                                if (draft.proposedVenues.isEmpty()) {
                                    "Chưa có sân đề xuất (tối đa 5 sân)"
                                } else {
                                    draft.proposedVenues.joinToString("\n") { "• ${it.venueName}" }
                                },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    AppOutlinedButton(
                        text = if (draft.venueName == null) "Chọn sân từ danh sách" else "Đổi sân bóng",
                        onClick = onOpenVenuePicker,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Thể thức & Trình độ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PitchFormat.entries.forEach { fmt ->
                            AppFilterChip(
                                text = fmt.label,
                                selected = draft.format == fmt.name,
                                onClick = { onEvent(MatchCreateEvent.UpdateDraft { it.copy(format = fmt.name) }) },
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkillLevel.entries.forEach { level ->
                            AppFilterChip(
                                text = level.label,
                                selected = draft.skillLevel == level.label,
                                onClick = { onEvent(MatchCreateEvent.UpdateDraft { it.copy(skillLevel = level.label) }) },
                            )
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Thời gian & Chi phí", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = draft.matchDate,
                        onValueChange = { v -> onEvent(MatchCreateEvent.UpdateDraft { it.copy(matchDate = v) }) },
                        label = { Text("Ngày đá (YYYY-MM-DD)") },
                        shape = ArenaShapes.Input,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = draft.startTime,
                            onValueChange = { v -> onEvent(MatchCreateEvent.UpdateDraft { it.copy(startTime = v) }) },
                            label = { Text("Bắt đầu (HH:mm)") },
                            shape = ArenaShapes.Input,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = draft.endTime,
                            onValueChange = { v -> onEvent(MatchCreateEvent.UpdateDraft { it.copy(endTime = v) }) },
                            label = { Text("Kết thúc (HH:mm)") },
                            shape = ArenaShapes.Input,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CostSplit.entries.forEach { split ->
                            AppFilterChip(
                                text = split.label,
                                selected = draft.costSplit == split.name,
                                onClick = { onEvent(MatchCreateEvent.UpdateDraft { it.copy(costSplit = split.name) }) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = if (draft.venueAmount > 0) draft.venueAmount.toString() else "",
                        onValueChange = { v ->
                            val parsed = v.filter { it.isDigit() }.toIntOrNull() ?: 0
                            onEvent(MatchCreateEvent.UpdateDraft { it.copy(venueAmount = parsed) })
                        },
                        label = { Text("Tiền sân dự kiến (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ArenaShapes.Input,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = draft.minReputationScore?.toString().orEmpty(),
                        onValueChange = { v ->
                            val parsed = v.filter { it.isDigit() }.toIntOrNull()
                            onEvent(MatchCreateEvent.UpdateDraft { it.copy(minReputationScore = parsed) })
                        },
                        label = { Text("Điểm uy tín tối thiểu (tùy chọn)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ArenaShapes.Input,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = draft.notes,
                        onValueChange = { v -> onEvent(MatchCreateEvent.UpdateDraft { it.copy(notes = v) }) },
                        label = { Text("Ghi chú cho đối thủ") },
                        shape = ArenaShapes.Input,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!uiState.isEditMode) {
                        AppSecondaryButton(
                            text = "Xem lại",
                            onClick = {
                                onEvent(MatchCreateEvent.ValidateForReview)
                                onNavigateToReview()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    AppButton(
                        text = if (uiState.isEditMode) "Lưu thay đổi" else "Đăng kèo ngay",
                        onClick = { onEvent(MatchCreateEvent.Submit) },
                        isLoading = uiState.isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
