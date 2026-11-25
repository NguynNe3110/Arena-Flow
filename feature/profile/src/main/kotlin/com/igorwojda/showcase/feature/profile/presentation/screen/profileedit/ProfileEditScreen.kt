@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.profile.presentation.screen.profileedit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaAvatar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.profile.domain.model.ProfileGender
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileEditScreen(
    modifier: Modifier = Modifier,
    isFirstTime: Boolean = false,
    onBackClick: () -> Unit = {},
    onSaveComplete: () -> Unit = {},
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val currentOnSaved by androidx.compose.runtime.rememberUpdatedState(onSaveComplete)

    LaunchedEffect(isFirstTime) {
        viewModel.setFirstTime(isFirstTime)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            currentOnSaved()
        }
    }

    ProfileEditContent(
        modifier = modifier,
        state = state,
        onDisplayNameChange = viewModel::onDisplayNameChanged,
        onBioChange = viewModel::onBioChanged,
        onPositionSelect = viewModel::onPositionSelected,
        onDateOfBirthChange = viewModel::onDateOfBirthChanged,
        onGenderSelect = viewModel::onGenderSelected,
        onProvinceSelect = viewModel::onProvinceSelected,
        onWardSelect = viewModel::onWardSelected,
        onCancelClick = onBackClick,
        onSaveClick = { viewModel.saveProfile() },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditContent(
    state: ProfileEditUiState,
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onPositionSelect: (PlayerPosition) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onGenderSelect: (ProfileGender) -> Unit,
    onProvinceSelect: (String) -> Unit,
    onWardSelect: (String) -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        if (state.isLoading) {
            ArenaLoadingState(
                modifier = Modifier.weight(1f),
                message = "Đang tải thông tin hồ sơ...",
            )
            return@Column
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimen.spaceL, vertical = Dimen.spaceL),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (state.isFirstTime) "Hoàn thiện hồ sơ" else "Sửa hồ sơ cá nhân",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )

            if (state.isFirstTime) {
                InlineNotice(
                    text = "Hoàn thiện hồ sơ để bắt đầu tìm kèo và tham gia đội bóng.",
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ArenaAvatar(
                    name = state.displayName.ifBlank { "AF" },
                    size = 88.dp,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = { if (it.length <= 100) onDisplayNameChange(it) },
                    label = { Text("Tên hiển thị *") },
                    placeholder = { Text("Nhập tên hiển thị của bạn") },
                    isError = state.displayNameError != null,
                    singleLine = true,
                    shape = ArenaShapes.Input,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = state.displayNameError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.ErrorBright,
                    )
                    Text(
                        text = "${state.displayName.length}/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.TextMuted,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.bio,
                    onValueChange = onBioChange,
                    label = { Text("Tiểu sử") },
                    placeholder = { Text("Giới thiệu ngắn về lối chơi, khung giờ đá...") },
                    minLines = 3,
                    maxLines = 4,
                    shape = ArenaShapes.Input,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${state.bio.length}/1000",
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                    modifier = Modifier.align(Alignment.End),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Vị trí chơi ưa thích",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArenaColors.TextPrimary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlayerPosition.entries.forEach { position ->
                        val selected = state.preferredPosition == position
                        FilterChip(
                            selected = selected,
                            onClick = { onPositionSelect(position) },
                            label = { Text(position.label) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ArenaColors.Primary,
                                    selectedLabelColor = Color.White,
                                ),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = state.dateOfBirth,
                    onValueChange = onDateOfBirthChange,
                    label = { Text("Ngày sinh (YYYY-MM-DD)") },
                    placeholder = { Text("1998-05-15") },
                    isError = state.dateOfBirthError != null,
                    singleLine = true,
                    shape = ArenaShapes.Input,
                    modifier = Modifier.fillMaxWidth(),
                )
                state.dateOfBirthError?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.ErrorBright,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Giới tính",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArenaColors.TextPrimary,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProfileGender.entries.forEach { gender ->
                        val selected = state.gender == gender
                        FilterChip(
                            selected = selected,
                            onClick = { onGenderSelect(gender) },
                            label = { Text(gender.label) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ArenaColors.Primary,
                                    selectedLabelColor = Color.White,
                                ),
                        )
                    }
                }
            }

            LocationDropdownField(
                label = "Tỉnh / Thành phố",
                selectedText = state.selectedProvinceName,
                options = state.provinces.map { it.provinceId to it.provinceName },
                onOptionSelect = onProvinceSelect,
            )

            LocationDropdownField(
                label = "Phường / Xã",
                selectedText =
                    when {
                        state.isLoadingWards -> "Đang tải phường/xã..."
                        state.selectedProvinceId == null -> "Vui lòng chọn tỉnh/thành trước"
                        state.wards.isEmpty() -> "Chưa có dữ liệu phường/xã"
                        else -> state.selectedWardName
                    },
                options = state.wards.map { it.wardId to it.wardName },
                onOptionSelect = onWardSelect,
                enabled = state.selectedProvinceId != null && !state.isLoadingWards && state.wards.isNotEmpty(),
            )

            state.submitError?.let { err ->
                Surface(
                    shape = ArenaShapes.Button,
                    color = ArenaColors.ErrorBg,
                    border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArenaColors.ErrorBright,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Surface(
            color = Color.White,
            border = BorderStroke(1.dp, ArenaColors.BorderSubtle),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimen.spaceL, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppOutlinedButton(
                    text = "Huỷ",
                    onClick = onCancelClick,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Lưu thay đổi",
                    onClick = onSaveClick,
                    isLoading = state.isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LocationDropdownField(
    label: String,
    selectedText: String,
    options: List<Pair<String, String>>,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ArenaColors.TextPrimary,
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(enabled = enabled) { expanded = true },
                shape = ArenaShapes.Input,
                color = if (enabled) Color.White else ArenaColors.SurfaceContainer,
                border = BorderStroke(1.dp, ArenaColors.Border),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) ArenaColors.TextPrimary else ArenaColors.TextMuted,
                    )
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = ArenaColors.TextMuted,
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            expanded = false
                            onOptionSelect(id)
                        },
                    )
                }
            }
        }
    }
}
