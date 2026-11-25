@file:Suppress("LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber", "UnusedImports")

package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarycreate

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import org.koin.androidx.compose.koinViewModel

/** S9 Create or edit a mercenary recruitment post with VenuePicker hook. */
@Composable
fun MercenaryCreateScreen(
    postId: String? = null,
    onBackClick: () -> Unit = {},
    onPickVenueClick: (requestKey: String) -> Unit = {},
    onSavedSuccess: (postId: String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MercenaryCreateViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val currentOnSavedSuccess by androidx.compose.runtime.rememberUpdatedState(onSavedSuccess)

    LaunchedEffect(postId) {
        viewModel.onEvent(MercenaryCreateEvent.Initialize(postId))
    }

    LaunchedEffect(uiState.savedPostId) {
        val savedId = uiState.savedPostId ?: return@LaunchedEffect
        currentOnSavedSuccess(savedId)
    }

    MercenaryCreateContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onPickVenueClick = { onPickVenueClick(MercenaryCreateViewModel.VENUE_PICKER_KEY) },
        modifier = modifier,
    )
}

@Composable
internal fun MercenaryCreateContent(
    uiState: MercenaryCreateUiState,
    onEvent: (MercenaryCreateEvent) -> Unit,
    onBackClick: () -> Unit,
    onPickVenueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnEvent by androidx.compose.runtime.rememberUpdatedState(onEvent)

    LaunchedEffect(uiState.validationMessage) {
        val message = uiState.validationMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        currentOnEvent(MercenaryCreateEvent.ClearValidation)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = if (uiState.editingPostId == null) "Đăng tin tìm người" else "Sửa tin tuyển",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                VenuePickerSection(
                    venueName = uiState.venueName,
                    venueAddress = uiState.venueAddress,
                    onPickVenueClick = onPickVenueClick,
                )
            }
            item {
                FormatAndPositionSection(
                    selectedFormat = uiState.format,
                    selectedPositions = uiState.neededPositions,
                    slotsNeeded = uiState.slotsNeeded,
                    onFormatChange = { onEvent(MercenaryCreateEvent.FormatChanged(it)) },
                    onTogglePosition = { onEvent(MercenaryCreateEvent.TogglePosition(it)) },
                    onSlotsChange = { onEvent(MercenaryCreateEvent.SlotsChanged(it)) },
                )
            }
            item {
                MatchMetaSection(
                    allowanceVndText = uiState.allowanceVndText,
                    matchStartAt = uiState.matchStartAt,
                    selectedSkillLevel = uiState.skillLevel,
                    notes = uiState.notes,
                    onAllowanceChange = { onEvent(MercenaryCreateEvent.AllowanceChanged(it)) },
                    onMatchStartAtChange = { onEvent(MercenaryCreateEvent.MatchStartAtChanged(it)) },
                    onSkillLevelChange = { onEvent(MercenaryCreateEvent.SkillLevelChanged(it)) },
                    onNotesChange = { onEvent(MercenaryCreateEvent.NotesChanged(it)) },
                )
            }
            item {
                AppButton(
                    text = if (uiState.editingPostId == null) "Đăng tin tuyển" else "Lưu thay đổi",
                    onClick = { onEvent(MercenaryCreateEvent.Submit) },
                    isLoading = uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun VenuePickerSection(
    venueName: String,
    venueAddress: String,
    onPickVenueClick: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Sân thi đấu",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = venueName.ifBlank { "Chưa chọn sân" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (venueAddress.isNotBlank()) {
            Text(
                text = venueAddress,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
            )
        }
        AppSecondaryButton(
            text = if (venueName.isBlank()) "Chọn sân từ danh sách" else "Đổi sân",
            onClick = onPickVenueClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FormatAndPositionSection(
    selectedFormat: PitchFormat,
    selectedPositions: Set<PlayerPosition>,
    slotsNeeded: Int,
    onFormatChange: (PitchFormat) -> Unit,
    onTogglePosition: (PlayerPosition) -> Unit,
    onSlotsChange: (Int) -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text("Loại sân", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PitchFormat.entries.forEach { format ->
                FilterChip(
                    selected = selectedFormat == format,
                    onClick = { onFormatChange(format) },
                    label = { Text(format.label) },
                )
            }
        }

        Text("Vị trí cần tuyển", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlayerPosition.entries.forEach { position ->
                FilterChip(
                    selected = position in selectedPositions,
                    onClick = { onTogglePosition(position) },
                    label = { Text(position.label) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Số lượng cần", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppSecondaryButton(text = "-", onClick = { onSlotsChange(slotsNeeded - 1) })
                Text(
                    text = "$slotsNeeded",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                AppSecondaryButton(text = "+", onClick = { onSlotsChange(slotsNeeded + 1) })
            }
        }
    }
}

@Composable
private fun MatchMetaSection(
    allowanceVndText: String,
    matchStartAt: String,
    selectedSkillLevel: SkillLevel,
    notes: String,
    onAllowanceChange: (String) -> Unit,
    onMatchStartAtChange: (String) -> Unit,
    onSkillLevelChange: (SkillLevel) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = allowanceVndText,
            onValueChange = { onAllowanceChange(it.filter(Char::isDigit)) },
            label = { Text("Hỗ trợ / chi phí (VNĐ)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = matchStartAt,
            onValueChange = onMatchStartAtChange,
            label = { Text("Giờ bóng lăn (ISO)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Trình độ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SkillLevel.entries.take(3).forEach { skill ->
                FilterChip(
                    selected = selectedSkillLevel == skill,
                    onClick = { onSkillLevelChange(skill) },
                    label = { Text(skill.label) },
                )
            }
        }
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Ghi chú thêm") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
