@file:Suppress("MagicNumber", "LongMethod")

package com.igorwojda.showcase.feature.team.presentation.screen.teamform

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.team.domain.model.OfficialSkillLabels
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamFormScreen(
    modifier: Modifier = Modifier,
    teamId: String? = null,
    onBackClick: () -> Unit = {},
    onSaveComplete: (teamId: String) -> Unit = {},
) {
    val viewModel: TeamFormViewModel = koinViewModel()
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(teamId) {
        viewModel.init(teamId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            ArenaTopBar(
                title = if (teamId == null) "Tạo đội mới" else "Sửa thông tin đội",
                showBack = true,
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                HorizontalDivider(color = ArenaColors.BorderSubtle)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppSecondaryButton(
                        text = "Hủy",
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                    )
                    AppButton(
                        text = if (teamId == null) "Tạo đội" else "Lưu thay đổi",
                        onClick = { viewModel.submit(onSaveComplete) },
                        isLoading = state.isSubmitting,
                        enabled = !state.isLoading && !state.isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { innerPadding ->
        if (state.isLoading) {
            ArenaLoadingState(modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else {
            TeamFormBody(
                state = state,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                onNameChange = viewModel::onNameChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onToggleSkill = viewModel::toggleSkill,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamFormBody(
    state: TeamFormUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleSkill: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (!state.formError.isNullOrBlank()) {
            InlineNotice(text = state.formError)
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Tên đội *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = ArenaColors.TextPrimary,
            )
            OutlinedTextField(
                value = state.teamName,
                onValueChange = onNameChange,
                placeholder = { Text("Ví dụ: FC Phượng Hoàng") },
                singleLine = true,
                isError = state.nameError != null,
                shape = ArenaShapes.Input,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaColors.Primary,
                        unfocusedBorderColor = ArenaColors.Border,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = state.nameError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.Error,
                )
                Text(
                    text = "${state.teamName.length}/100",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Mô tả",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = ArenaColors.TextPrimary,
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("Đá giao lưu tối thứ 4 hằng tuần...") },
                minLines = 3,
                maxLines = 5,
                shape = ArenaShapes.Input,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArenaColors.Primary,
                        unfocusedBorderColor = ArenaColors.Border,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${state.description.length}/1000",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
                modifier = Modifier.align(Alignment.End),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trình độ đội (1–3 nhãn)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ArenaColors.TextPrimary,
                )
                AppBadge(
                    text = "Đã chọn ${state.selectedSkills.size}/3",
                    variant = BadgeVariant.Green,
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OfficialSkillLabels.all.forEach { label ->
                    AppFilterChip(
                        text = label,
                        selected = label in state.selectedSkills,
                        onClick = { onToggleSkill(label) },
                    )
                }
            }

            if (!state.skillError.isNullOrBlank()) {
                Text(
                    text = state.skillError,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.Error,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
