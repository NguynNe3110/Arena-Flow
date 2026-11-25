@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.venue.presentation.screen.venueform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VenueFormScreen(
    onBackClick: () -> Unit,
    onSavedVenueDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    venueId: String? = null,
    returnKey: String? = null,
    onReturnSelection: () -> Unit = onBackClick,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: VenueFormViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsState()
    val currentOnSelectionReturned by androidx.compose.runtime.rememberUpdatedState(onReturnSelection)
    val currentOnSavedVenueDetail by androidx.compose.runtime.rememberUpdatedState(onSavedVenueDetail)

    LaunchedEffect(venueId) {
        viewModel.loadForm(venueId)
    }

    LaunchedEffect(state.savedVenueId, state.isSelectionReturned) {
        val savedId = state.savedVenueId ?: return@LaunchedEffect
        val returned = state.isSelectionReturned
        viewModel.consumeNavigation()
        if (returned) {
            currentOnSelectionReturned()
        } else {
            currentOnSavedVenueDetail(savedId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = if (venueId != null) "Sửa thông tin sân" else "Thêm sân bóng",
                showBack = true,
                userName = state.currentUser?.displayName ?: "Khách",
                unreadNotificationCount = state.unreadNotifications,
                onBackClick = onBackClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .navigationBarsPadding(),
            contentPadding = PaddingValues(Dimen.pagePadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.stackGap),
        ) {
            when {
                state.isLoading -> {
                    item { ArenaLoadingState(message = "Đang tải biểu mẫu sân...") }
                }

                state.screenError != null -> {
                    item {
                        ArenaErrorState(
                            title = if (state.isEditMode) "Không có quyền sửa sân" else "Cần đăng nhập",
                            message = state.screenError.orEmpty(),
                            onBackClick = onBackClick,
                        )
                    }
                }

                else -> {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.venueName,
                                onValueChange = viewModel::onNameChange,
                                label = { Text("Tên sân bóng *") },
                                placeholder = { Text("VD: Sân bóng Gia Định") },
                                isError = state.nameError != null,
                                supportingText = state.nameError?.let { err -> { Text(err) } },
                                singleLine = true,
                                shape = ArenaShapes.Input,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedTextField(
                                value = state.address,
                                onValueChange = viewModel::onAddressChange,
                                label = { Text("Địa chỉ chi tiết *") },
                                placeholder = { Text("Số nhà, tên đường...") },
                                isError = state.addressError != null,
                                supportingText = state.addressError?.let { err -> { Text(err) } },
                                shape = ArenaShapes.Input,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Loại sân",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArenaColors.TextPrimary,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    PitchFormat.entries.forEach { format ->
                                        AppFilterChip(
                                            text = format.label,
                                            selected = state.selectedFormat == format,
                                            onClick = { viewModel.onFormatSelect(format) },
                                            modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Phường / Khu vực *",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ArenaColors.TextPrimary,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    state.wards.forEach { ward ->
                                        AppFilterChip(
                                            text = ward.wardName,
                                            selected = state.selectedWardId == ward.wardId,
                                            onClick = { viewModel.onWardSelect(ward.wardId) },
                                            modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                                        )
                                    }
                                }
                                if (state.wardError != null) {
                                    Text(
                                        text = state.wardError.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArenaColors.ErrorBright,
                                    )
                                }
                            }

                            if (state.isAdmin) {
                                OutlinedTextField(
                                    value = state.phone,
                                    onValueChange = viewModel::onPhoneChange,
                                    label = { Text("SĐT liên hệ (Chỉ Admin)") },
                                    placeholder = { Text("090xxxxxxx") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    shape = ArenaShapes.Input,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                OutlinedTextField(
                                    value = state.website,
                                    onValueChange = viewModel::onWebsiteChange,
                                    label = { Text("Website (Chỉ Admin)") },
                                    placeholder = { Text("https://...") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                    shape = ArenaShapes.Input,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            if (!state.submitError.isNullOrBlank()) {
                                Text(
                                    text = state.submitError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ArenaColors.ErrorBright,
                                )
                            }
                        }
                    }

                    item {
                        AppButton(
                            text = if (state.isEditMode) "Lưu thông tin sân" else "Tạo sân (Hiệu lực ngay)",
                            onClick = { viewModel.submit(returnKey) },
                            isLoading = state.isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
