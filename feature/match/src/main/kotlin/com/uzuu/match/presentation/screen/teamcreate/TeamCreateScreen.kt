package com.uzuu.match.presentation.screen.teamcreate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.MatchShape
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamCreateScreen(
    onBackClick: () -> Unit = {},
    onTeamCreated: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: TeamCreateViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamCreateContent(
        uiState = uiState,
        onNameChange = viewModel::updateTeamName,
        onShortNameChange = viewModel::updateShortName,
        onDescriptionChange = viewModel::updateDescription,
        onWardSelect = viewModel::updateWard,
        onSkillToggle = viewModel::toggleSkill,
        onSubmit = {
            viewModel.createTeam(onSuccess = onTeamCreated)
        },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamCreateContent(
    uiState: TeamCreateUiState,
    onNameChange: (String) -> Unit,
    onShortNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onWardSelect: (String) -> Unit,
    onSkillToggle: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tạo đội bóng mới",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Quay lại",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Notice Info Banner
            Surface(
                shape = MatchShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Column {
                        Text(
                            text = "Thành lập đội bóng của bạn",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bạn sẽ tự động là Đội trưởng và có toàn quyền quản lý thành viên, tạo kèo và tuyển quân.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Team Name Field
            OutlinedTextField(
                value = uiState.teamName,
                onValueChange = onNameChange,
                label = { Text("Tên đội bóng *") },
                placeholder = { Text("VD: Bình Minh FC, Sài Gòn United") },
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
            )

            // Short Name Field
            OutlinedTextField(
                value = uiState.shortName,
                onValueChange = onShortNameChange,
                label = { Text("Tên viết tắt (Tối đa 6 ký tự)") },
                placeholder = { Text("VD: BMFC, SGU") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Description Field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Giới thiệu đội bóng") },
                placeholder = { Text("Lịch sinh hoạt định kỳ, phong cách đá, tiêu chí giao lưu...") },
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("${uiState.description.length}/300 ký tự") },
                modifier = Modifier.fillMaxWidth(),
            )

            // Operating Area Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Khu vực hoạt động chính *",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
                val wards = listOf(
                    "P. An Khánh (TP. Thủ Đức, TP.HCM)",
                    "P. Hải Châu 1 (Q. Hải Châu, Đà Nẵng)",
                    "P. Bến Nghé (Quận 1, TP.HCM)",
                )
                wards.forEach { ward ->
                    val isSelected = uiState.selectedWard == ward
                    Surface(
                        shape = ControlShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWardSelect(ward) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onWardSelect(ward) },
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = ward,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Skill Levels (Max 3)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Trình độ thi đấu",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        text = "Chọn tối đa 3 mức",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val skillLevels = listOf("Vui vẻ", "Trung bình", "Bán chuyên")
                    skillLevels.forEach { skill ->
                        val isSelected = uiState.selectedSkills.contains(skill)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSkillToggle(skill) },
                            label = { Text(skill, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else null,
                            modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = onSubmit,
                enabled = !uiState.isSubmitting && uiState.teamName.isNotBlank(),
                shape = ControlShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = "Tạo đội bóng ngay",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}
