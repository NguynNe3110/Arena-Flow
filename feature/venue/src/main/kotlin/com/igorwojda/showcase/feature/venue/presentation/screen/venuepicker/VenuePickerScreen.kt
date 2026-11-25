@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber", "UnusedImports")

package com.igorwojda.showcase.feature.venue.presentation.screen.venuepicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaSearchFilterBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.venue.domain.model.VenueSummary
import com.igorwojda.showcase.feature.venue.domain.model.VenueWardOption
import com.igorwojda.showcase.feature.venue.presentation.composable.VenuePhotoThumbnail
import com.igorwojda.showcase.feature.venue.presentation.composable.venueLabel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenuePickerScreen(
    onBackClick: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onCreateVenue: (String?) -> Unit,
    onVenueSelectedBack: () -> Unit,
    modifier: Modifier = Modifier,
    requestKey: String? = null,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: VenuePickerViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsState()
    val currentOnVenueSelectedBack by androidx.compose.runtime.rememberUpdatedState(onVenueSelectedBack)

    LaunchedEffect(state.isSelectionCompleted) {
        if (state.isSelectionCompleted) {
            viewModel.consumeSelectionCompleted()
            currentOnVenueSelectedBack()
        }
    }

    if (state.isFilterSheetOpen) {
        VenueFilterBottomSheet(
            initialFormat = state.selectedFormat,
            initialWardId = state.selectedWardId,
            wards = state.wards,
            onDismiss = viewModel::closeFilterSheet,
            onApply = viewModel::applyFilters,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = "Tìm & chọn sân",
                showBack = true,
                userName = state.userName,
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
            if (!requestKey.isNullOrBlank()) {
                item {
                    PickerModeBanner()
                }
            }

            item {
                ArenaSearchFilterBar(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    onFilterClick = viewModel::openFilterSheet,
                    placeholder = "Tìm tên sân, đường, quận...",
                    activeFilterCount = state.activeFilterCount,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${state.visibleVenues.size} địa điểm sân",
                        style = MaterialTheme.typography.labelLarge,
                        color = ArenaColors.TextMuted,
                    )
                    AppButton(
                        text = "Thêm sân mới",
                        onClick = { onCreateVenue(requestKey) },
                    )
                }
            }

            when {
                state.isLoading -> {
                    item { ArenaLoadingState(message = "Đang tìm địa điểm sân...") }
                }

                state.errorMessage != null -> {
                    item {
                        ArenaErrorState(
                            title = "Không tải được danh sách sân",
                            message = state.errorMessage.orEmpty(),
                            onRetryClick = { viewModel.loadVenues() },
                        )
                    }
                }

                state.visibleVenues.isEmpty() -> {
                    item {
                        ArenaEmptyState(
                            title = "Không tìm thấy sân phù hợp",
                            description = "Bạn có thể thêm sân mới để sử dụng ngay.",
                            icon = Icons.Outlined.Stadium,
                            actionLabel = "Thêm sân mới",
                            onAction = { onCreateVenue(requestKey) },
                        )
                    }
                }

                else -> {
                    items(state.visibleVenues, key = { it.venueId }) { venue ->
                        VenuePickerCard(
                            venue = venue,
                            isPickerMode = !requestKey.isNullOrBlank(),
                            onCardClick = { onOpenDetail(venue.venueId) },
                            onSelectClick = {
                                if (!requestKey.isNullOrBlank()) {
                                    viewModel.selectVenue(requestKey, venue)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerModeBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArenaShapes.Notice,
        color = ArenaColors.SuccessBg,
        border = BorderStroke(1.dp, ArenaColors.SuccessBorder),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = ArenaColors.Success,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Chế độ chọn sân: Chọn sân đang hoạt động hoặc thêm sân mới.",
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.Success,
            )
        }
    }
}

@Composable
private fun VenuePickerCard(
    venue: VenueSummary,
    isPickerMode: Boolean,
    onCardClick: () -> Unit,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            VenuePhotoThumbnail(
                imageUrl = venue.primaryImageUrl,
                contentDescription = venue.venueName,
                modifier = Modifier.size(64.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = venue.venueName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ArenaColors.TextHeading,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppBadge(
                        text = venue.status.label,
                        variant = if (venue.isInactive) BadgeVariant.Error else BadgeVariant.Success,
                    )
                }

                Text(
                    text = venue.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                val metaText =
                    buildString {
                        append(venue.venueType.venueLabel())
                        if (venue.distanceKm != null) {
                            append(" • ~${venue.distanceKm} km")
                        }
                    }
                Text(
                    text = metaText,
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.TextMuted,
                )
            }
        }

        if (venue.isInactive) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ArenaShapes.Notice,
                color = ArenaColors.ErrorBg,
                border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
            ) {
                Text(
                    text = "Sân đã ngừng hoạt động — Không thể chọn cho kèo mới.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ArenaColors.ErrorBright,
                    modifier = Modifier.padding(10.dp),
                )
            }
        }

        if (isPickerMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (venue.isInactive) {
                    AppSecondaryButton(
                        text = "Không thể chọn (Ngừng HĐ)",
                        onClick = {},
                        enabled = false,
                    )
                } else {
                    AppButton(
                        text = "Chọn sân này",
                        onClick = onSelectClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun VenueFilterBottomSheet(
    initialFormat: PitchFormat?,
    initialWardId: String?,
    wards: List<VenueWardOption>,
    onDismiss: () -> Unit,
    onApply: (PitchFormat?, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var format by remember(initialFormat) { mutableStateOf(initialFormat) }
    var wardId by remember(initialWardId) { mutableStateOf(initialWardId) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Lọc địa điểm sân",
                style = MaterialTheme.typography.titleLarge,
                color = ArenaColors.TextHeading,
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
                    AppFilterChip(
                        text = "Tất cả",
                        selected = format == null,
                        onClick = { format = null },
                        modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                    )
                    PitchFormat.entries.forEach { option ->
                        AppFilterChip(
                            text = option.label,
                            selected = format == option,
                            onClick = { format = option },
                            modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                        )
                    }
                }
            }

            if (wards.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Phường / Khu vực",
                        style = MaterialTheme.typography.titleSmall,
                        color = ArenaColors.TextPrimary,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppFilterChip(
                            text = "Tất cả khu vực",
                            selected = wardId == null,
                            onClick = { wardId = null },
                            modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                        )
                        wards.forEach { ward ->
                            AppFilterChip(
                                text = ward.wardName,
                                selected = wardId == ward.wardId,
                                onClick = { wardId = ward.wardId },
                                modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppSecondaryButton(
                    text = "Đặt lại",
                    onClick = {
                        format = null
                        wardId = null
                    },
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Áp dụng",
                    onClick = { onApply(format, wardId) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
