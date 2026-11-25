@file:Suppress("CognitiveComplexMethod", "CyclomaticComplexMethod", "LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.venue.presentation.screen.venuedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.venue.domain.model.VenueDetail
import com.igorwojda.showcase.feature.venue.domain.model.VenueReportReason
import com.igorwojda.showcase.feature.venue.presentation.composable.VenueInactiveBanner
import com.igorwojda.showcase.feature.venue.presentation.composable.VenuePhotoThumbnail
import com.igorwojda.showcase.feature.venue.presentation.composable.venueLabel
import org.koin.androidx.compose.koinViewModel

@Composable
fun VenueDetailScreen(
    venueId: String,
    onBackClick: () -> Unit,
    onEditVenue: (String) -> Unit,
    onOpenPhotos: (String) -> Unit,
    modifier: Modifier = Modifier,
    requestKey: String? = null,
    onVenueSelectedBack: () -> Unit = onBackClick,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: VenueDetailViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(venueId) {
        viewModel.loadVenue(venueId)
    }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeSnackbar()
    }

    val currentOnVenueSelectedBack by androidx.compose.runtime.rememberUpdatedState(onVenueSelectedBack)

    LaunchedEffect(state.isSelectionCompleted) {
        if (state.isSelectionCompleted) {
            viewModel.consumeSelectionCompleted()
            currentOnVenueSelectedBack()
        }
    }

    if (state.isReportSheetOpen && state.venue != null) {
        VenueReportBottomSheet(
            venue = state.venue!!,
            selectedReason = state.selectedReason,
            detail = state.reportDetail,
            detailError = state.reportDetailError,
            submitError = state.reportSubmitError,
            isSubmitting = state.isSubmittingReport,
            onReasonSelect = viewModel::onReportReasonChange,
            onDetailChange = viewModel::onReportDetailChange,
            onSubmit = viewModel::submitReport,
            onDismiss = viewModel::closeReportSheet,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ArenaTopBar(
                title = "Chi tiết sân bóng",
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
                    .navigationBarsPadding(),
            contentPadding = PaddingValues(Dimen.pagePadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.stackGap),
        ) {
            when {
                state.isLoading -> {
                    item { ArenaLoadingState(message = "Đang tải thông tin sân...") }
                }

                state.errorMessage != null || state.venue == null -> {
                    item {
                        ArenaErrorState(
                            title = "Không tìm thấy sân",
                            message = state.errorMessage ?: "Địa điểm không tồn tại.",
                            onBackClick = onBackClick,
                            onRetryClick = { viewModel.loadVenue(venueId) },
                        )
                    }
                }

                else -> {
                    val venue = state.venue!!
                    if (venue.isInactive) {
                        item {
                            VenueInactiveBanner(
                                message =
                                    "Sân này đã ngừng hoạt động theo báo cáo xác thực. " +
                                        "Bạn vẫn có thể xem lại thông tin nhưng không thể chọn cho kèo mới.",
                            )
                        }
                    }

                    item {
                        VenueDetailCard(
                            venue = venue,
                            onOpenPhotos = { onOpenPhotos(venue.venueId) },
                        )
                    }

                    if (!requestKey.isNullOrBlank() && !venue.isInactive) {
                        item {
                            AppButton(
                                text = "Chọn sân này",
                                onClick = { viewModel.selectVenueForCaller(requestKey) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    if (state.canEdit || state.canReport) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                if (state.canEdit) {
                                    AppSecondaryButton(
                                        text = "Sửa thông tin",
                                        onClick = { onEditVenue(venue.venueId) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (state.canReport) {
                                    Button(
                                        onClick = viewModel::openReportSheet,
                                        modifier = Modifier.weight(1f).heightIn(min = Dimen.minTouchTarget),
                                        shape = ArenaShapes.Button,
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = ArenaColors.ErrorBg,
                                                contentColor = ArenaColors.ErrorBright,
                                            ),
                                        border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
                                    ) {
                                        Text(
                                            text = "Báo cáo sai sót",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VenueDetailCard(
    venue: VenueDetail,
    onOpenPhotos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryImage = venue.images.firstOrNull { it.isPrimary } ?: venue.images.firstOrNull()

    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(168.dp),
        ) {
            VenuePhotoThumbnail(
                imageUrl = primaryImage?.imageUrl,
                contentDescription = venue.venueName,
                modifier = Modifier.fillMaxSize(),
            )
            Surface(
                onClick = onOpenPhotos,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .heightIn(min = Dimen.minTouchTarget),
                shape = ArenaShapes.Button,
                color = Color.Black.copy(alpha = 0.65f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Thư viện (${venue.images.size}/5)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(Dimen.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = venue.venueName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.TextHeading,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                AppBadge(
                    text = venue.status.label,
                    variant = if (venue.isInactive) BadgeVariant.Error else BadgeVariant.Success,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = ArenaColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = venue.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ArenaColors.TextMuted,
                )
            }

            KeyValueRow(label = "Loại sân", value = venue.venueType.venueLabel())
            KeyValueRow(label = "Khu vực", value = venue.ward?.fullLabel ?: "Chưa cập nhật")
            KeyValueRow(label = "Liên hệ sân", value = venue.phone ?: "Chưa cập nhật")
            if (!venue.website.isNullOrBlank()) {
                KeyValueRow(label = "Website", value = venue.website)
            }

            InlineNotice(
                text = "Lưu ý: Xem thông tin sân trên ArenaFlow không đồng nghĩa đã đặt sân. Hai đội tự liên hệ chủ sân để giữ giờ.",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VenueReportBottomSheet(
    venue: VenueDetail,
    selectedReason: VenueReportReason,
    detail: String,
    detailError: String?,
    submitError: String?,
    isSubmitting: Boolean,
    onReasonSelect: (VenueReportReason) -> Unit,
    onDetailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = ArenaColors.CardBg,
        shape = ArenaShapes.Sheet,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimen.pagePadding)
                    .padding(bottom = Dimen.pagePadding)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Báo cáo thông tin sân",
                style = MaterialTheme.typography.titleLarge,
                color = ArenaColors.TextHeading,
            )

            InlineNotice(text = "${venue.venueName}\n${venue.address}")

            Text(
                text = "Loại sai sót",
                style = MaterialTheme.typography.titleSmall,
                color = ArenaColors.TextPrimary,
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                VenueReportReason.entries.forEach { reason ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = Dimen.minTouchTarget)
                                .clickable(role = Role.RadioButton) { onReasonSelect(reason) }
                                .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArenaColors.TextPrimary,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = detail,
                onValueChange = onDetailChange,
                label = { Text("Mô tả chi tiết *") },
                placeholder = { Text("Ghi rõ thông tin cần Admin kiểm tra...") },
                isError = detailError != null,
                supportingText = detailError?.let { err -> { Text(err) } },
                minLines = 3,
                shape = ArenaShapes.Input,
                modifier = Modifier.fillMaxWidth(),
            )

            if (!submitError.isNullOrBlank()) {
                Text(
                    text = submitError,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.ErrorBright,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppSecondaryButton(
                    text = "Hủy",
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onSubmit,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f).heightIn(min = Dimen.minTouchTarget),
                    shape = ArenaShapes.Button,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = ArenaColors.ErrorBright,
                            contentColor = Color.White,
                        ),
                ) {
                    Text(
                        text = if (isSubmitting) "Đang gửi..." else "Gửi báo cáo",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
