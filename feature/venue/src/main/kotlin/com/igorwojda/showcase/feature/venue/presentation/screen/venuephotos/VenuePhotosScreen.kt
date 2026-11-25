@file:Suppress("CognitiveComplexMethod", "LabeledExpression", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.venue.presentation.screen.venuephotos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaLoadingState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.venue.domain.model.VenueImage
import com.igorwojda.showcase.feature.venue.presentation.composable.VenuePhotoThumbnail
import org.koin.androidx.compose.koinViewModel

@Composable
fun VenuePhotosScreen(
    venueId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: VenuePhotosViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(venueId) {
        viewModel.loadPhotos(venueId)
    }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeSnackbar()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ArenaTopBar(
                title = "Thư viện ảnh sân",
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
                    item { ArenaLoadingState(message = "Đang tải thư viện ảnh...") }
                }

                state.errorMessage != null || state.venue == null -> {
                    item {
                        ArenaErrorState(
                            title = "Không tìm thấy sân",
                            message = state.errorMessage ?: "Địa điểm không tồn tại.",
                            onBackClick = onBackClick,
                            onRetryClick = { viewModel.loadPhotos(venueId) },
                        )
                    }
                }

                else -> {
                    val venue = state.venue!!
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = venue.venueName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ArenaColors.TextHeading,
                                    )
                                    Text(
                                        text = "Đã tải ${state.images.size} / ${VenuePhotosUiState.MAX_PHOTOS} ảnh tối đa",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ArenaColors.TextMuted,
                                    )
                                }
                                if (state.canUploadMore) {
                                    AppButton(
                                        text = "+ Tải ảnh lên",
                                        onClick = { viewModel.uploadPhoto() },
                                        isLoading = state.isUploading,
                                    )
                                }
                            }
                        }
                    }

                    if (state.failedUploadLabel != null && state.uploadErrorMessage != null) {
                        item {
                            UploadRetryBanner(
                                errorMessage = state.uploadErrorMessage.orEmpty(),
                                onRetry = viewModel::retryFailedUpload,
                            )
                        }
                    }

                    if (state.images.isEmpty()) {
                        item {
                            ArenaEmptyState(
                                title = "Chưa có ảnh sân nào",
                                description = "Thêm tối đa 5 ảnh để các đội dễ dàng nhận diện mặt sân.",
                                icon = Icons.Outlined.AddAPhoto,
                                actionLabel = if (state.canUploadMore) "+ Tải ảnh lên" else null,
                                onAction = { viewModel.uploadPhoto() },
                            )
                        }
                    } else {
                        itemsIndexed(state.images, key = { _, img -> img.imageId }) { index, image ->
                            VenuePhotoCard(
                                image = image,
                                index = index,
                                canModifyPrimary = state.canModifyPrimary,
                                canDelete = state.canDeletePhotos,
                                isBusy = state.busyImageId == image.imageId,
                                onSetPrimary = { viewModel.setPrimary(image.imageId) },
                                onDelete = { viewModel.deletePhoto(image.imageId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadRetryBanner(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = ArenaColors.ErrorBg,
        border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.ErrorBright,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            AppSecondaryButton(
                text = "Thử lại",
                onClick = onRetry,
            )
        }
    }
}

@Composable
private fun VenuePhotoCard(
    image: VenueImage,
    index: Int,
    canModifyPrimary: Boolean,
    canDelete: Boolean,
    isBusy: Boolean,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        VenuePhotoThumbnail(
            imageUrl = image.imageUrl,
            contentDescription = "Ảnh sân ${index + 1}",
            modifier = Modifier.fillMaxWidth().height(156.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (image.isPrimary) {
                AppBadge(text = "Ảnh đại diện", variant = BadgeVariant.Primary)
            } else if (canModifyPrimary) {
                TextButton(
                    onClick = onSetPrimary,
                    enabled = !isBusy,
                    modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                ) {
                    Text(
                        text = "Đặt làm ảnh chính",
                        style = MaterialTheme.typography.labelLarge,
                        color = ArenaColors.Primary,
                    )
                }
            } else {
                Text(
                    text = "Ảnh #${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ArenaColors.TextMuted,
                )
            }

            if (canDelete) {
                TextButton(
                    onClick = onDelete,
                    enabled = !isBusy,
                    modifier = Modifier.heightIn(min = Dimen.minTouchTarget),
                ) {
                    Text(
                        text = "Xóa ảnh",
                        style = MaterialTheme.typography.labelLarge,
                        color = ArenaColors.ErrorBright,
                    )
                }
            }
        }
    }
}
