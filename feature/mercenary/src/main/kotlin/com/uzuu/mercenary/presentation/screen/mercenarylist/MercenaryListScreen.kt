@file:Suppress("TooManyFunctions", "MagicNumber")

package com.uzuu.mercenary.presentation.screen.mercenarylist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.uzuu.mercenary.domain.model.MercenaryPost
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val PositionOptions =
    listOf(
        "Thủ môn" to "GK",
        "Hậu vệ" to "DF",
        "Tiền vệ" to "MF",
        "Tiền đạo" to "FW",
    )

private val RadiusOptions =
    listOf(
        "3 km" to 3.0f,
        "5 km" to 5.0f,
        "10 km" to 10.0f,
        "20 km" to 20.0f,
    )

internal data class MercenaryListCallbacks(
    val onRefresh: () -> Unit,
    val onFilterPosition: (String) -> Unit,
    val onRadiusChange: (Float) -> Unit,
    val onApply: (String) -> Unit,
    val onNotificationClick: () -> Unit = {},
    val onPostClick: (String) -> Unit = {},
)

@Composable
fun MercenaryListScreen(
    onPostClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MercenaryListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MercenaryListContent(
        uiState = uiState,
        callbacks =
            MercenaryListCallbacks(
                onRefresh = viewModel::loadPosts,
                onFilterPosition = viewModel::setFilterPosition,
                onRadiusChange = viewModel::setRadius,
                onApply = { postId -> viewModel.applyToPost(postId) },
                onNotificationClick = onNotificationClick,
                onPostClick = onPostClick,
            ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MercenaryListContent(
    uiState: MercenaryListUiState,
    callbacks: MercenaryListCallbacks,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredPosts =
        uiState.posts.filter { post ->
            (uiState.filterPosition == "ALL" || post.position == uiState.filterPosition) &&
                post.distanceKm <= uiState.radiusKm
        }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ArenaColors.TextPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                ) {
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                }
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = callbacks.onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                renderMercenaryStatus(uiState, callbacks.onRefresh)
                item { MercenaryHero() }
                item {
                    MercenaryPositionDropdown(
                        selectedPosition = uiState.filterPosition,
                        onPositionSelect = callbacks.onFilterPosition,
                    )
                }
                item {
                    MercenaryRadiusDropdown(
                        selectedRadius = uiState.radiusKm,
                        onRadiusSelect = callbacks.onRadiusChange,
                    )
                }
                renderMercenaryBody(
                    uiState = uiState,
                    filteredPosts = filteredPosts,
                    onRetry = callbacks.onRefresh,
                    onApplyClick = { postId ->
                        callbacks.onApply(postId)
                        scope.launch {
                            snackbarHostState.showSnackbar("Đơn đang chờ đội trưởng duyệt")
                        }
                    },
                )
            }
        }
    }
}

private fun LazyListScope.renderMercenaryStatus(
    uiState: MercenaryListUiState,
    onRefresh: () -> Unit,
) {
    if (uiState.isLoading && uiState.posts.isNotEmpty()) {
        item {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = ArenaColors.Primary,
            )
        }
    }
    if (uiState.error != null && uiState.posts.isNotEmpty()) {
        item {
            TextButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Chưa tải được dữ liệu mới. Chạm để thử lại.",
                    color = ArenaColors.BadgeAmberText,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

private fun LazyListScope.renderMercenaryBody(
    uiState: MercenaryListUiState,
    filteredPosts: List<MercenaryPost>,
    onRetry: () -> Unit,
    onApplyClick: (String) -> Unit,
) {
    when {
        uiState.isLoading && uiState.posts.isEmpty() -> {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
        }

        uiState.error != null && uiState.posts.isEmpty() -> {
            item {
                AppErrorContent(uiState.error, onRetry)
            }
        }

        filteredPosts.isEmpty() -> {
            item {
                MercenaryEmptyContent()
            }
        }

        else -> {
            items(filteredPosts, key = { it.id }) { post ->
                MercenaryCard(
                    post = post,
                    isApplying = uiState.isApplying,
                    onApplyClick = { onApplyClick(post.id) },
                )
            }
        }
    }
}

@Composable
private fun MercenaryHero(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 10.dp),
    ) {
        Text(
            text = "Dữ liệu mẫu · Cá nhân",
            color = ArenaColors.TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Text(
            text = "Ra sân cùng đội mới.",
            color = ArenaColors.TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
            letterSpacing = (-0.035).sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Text(
            text = "Tìm đội đang thiếu vị trí của bạn.",
            color = ArenaColors.TextMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun MercenaryPositionDropdown(
    selectedPosition: String,
    onPositionSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = PositionOptions.firstOrNull { it.second == selectedPosition }?.first ?: "Thủ môn"

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 7.dp),
    ) {
        Text(
            text = "Vị trí",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArenaColors.TextPrimary,
            modifier = Modifier.padding(bottom = 7.dp),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .border(1.dp, ArenaColors.InputBorder, ArenaShapes.Input)
                    .background(Color.White, ArenaShapes.Input)
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayLabel,
                    style =
                        TextStyle(
                            fontSize = 15.sp,
                            color = ArenaColors.TextPrimary,
                        ),
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Chọn vị trí",
                    tint = ArenaColors.TextMuted,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
            ) {
                PositionOptions.forEach { (label, code) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                style =
                                    TextStyle(
                                        fontSize = 15.sp,
                                        color = ArenaColors.TextPrimary,
                                        fontWeight = if (code == selectedPosition) FontWeight.Bold else FontWeight.Normal,
                                    ),
                            )
                        },
                        onClick = {
                            onPositionSelect(code)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MercenaryRadiusDropdown(
    selectedRadius: Float,
    onRadiusSelect: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = RadiusOptions.firstOrNull { it.second == selectedRadius }?.first ?: "3 km"

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 7.dp),
    ) {
        Text(
            text = "Bán kính tìm kiếm",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArenaColors.TextPrimary,
            modifier = Modifier.padding(bottom = 7.dp),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .border(1.dp, ArenaColors.InputBorder, ArenaShapes.Input)
                    .background(Color.White, ArenaShapes.Input)
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayLabel,
                    style =
                        TextStyle(
                            fontSize = 15.sp,
                            color = ArenaColors.TextPrimary,
                        ),
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Chọn bán kính",
                    tint = ArenaColors.TextMuted,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
            ) {
                RadiusOptions.forEach { (label, radius) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                style =
                                    TextStyle(
                                        fontSize = 15.sp,
                                        color = ArenaColors.TextPrimary,
                                        fontWeight = if (radius == selectedRadius) FontWeight.Bold else FontWeight.Normal,
                                    ),
                            )
                        },
                        onClick = {
                            onRadiusSelect(radius)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MercenaryCard(
    post: MercenaryPost,
    isApplying: Boolean,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 7.dp),
        shape = ArenaShapes.Card,
        color = ArenaColors.CardBg,
        border = BorderStroke(1.dp, ArenaColors.Border),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
        ) {
            AppBadge(
                text = "Cần ${post.openSlots} ${post.positionLabel.lowercase()}",
                variant = BadgeVariant.Green,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = post.teamName,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextPrimary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${post.matchDate} · ${post.timeSlot}",
                fontSize = 15.sp,
                color = ArenaColors.TextPrimary,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${post.estimatedArea}\nCách bạn ${formatDistance(post.distanceKm)} km · Trung bình",
                fontSize = 14.sp,
                color = ArenaColors.TextMuted,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(14.dp))

            InlineNotice(
                text = "Tên sân và địa chỉ hiển thị sau khi đơn được duyệt.",
            )

            Spacer(Modifier.height(14.dp))

            if (post.isApplied) {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = ArenaShapes.Button,
                    colors =
                        ButtonDefaults.buttonColors(
                            disabledContainerColor = ArenaColors.Primary.copy(alpha = 0.55f),
                            disabledContentColor = Color.White.copy(alpha = 0.85f),
                        ),
                ) {
                    Text(
                        text = "Đã ứng tuyển · Chờ duyệt",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Button(
                    onClick = onApplyClick,
                    enabled = !isApplying,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = ArenaShapes.Button,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = ArenaColors.Primary,
                            contentColor = Color.White,
                        ),
                ) {
                    Text(
                        text = "Ứng tuyển ${post.positionLabel.lowercase()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun MercenaryEmptyContent(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Chưa có tin tuyển người phù hợp",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = ArenaColors.TextPrimary,
            )
            Text(
                text = "Hãy tăng bán kính tìm kiếm hoặc đổi vị trí thi đấu.",
                fontSize = 14.sp,
                color = ArenaColors.TextMuted,
            )
        }
    }
}

private fun formatDistance(distance: Double): String {
    val rounded = String.format(java.util.Locale.US, "%.1f", distance)
    return rounded.replace('.', ',')
}
