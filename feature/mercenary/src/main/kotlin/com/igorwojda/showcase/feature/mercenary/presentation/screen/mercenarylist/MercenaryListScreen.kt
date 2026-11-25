@file:Suppress(
    "CognitiveComplexMethod",
    "CyclomaticComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "LongParameterList",
    "MagicNumber",
    "TooManyFunctions",
)

package com.igorwojda.showcase.feature.mercenary.presentation.screen.mercenarylist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.error.userMessage
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.PlayerPosition
import com.igorwojda.showcase.feature.base.domain.model.WardOption
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaEmptyState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaErrorState
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaSearchFilterBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaTopBar
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.mercenary.presentation.composable.MercenaryPostCard
import com.igorwojda.showcase.feature.mercenary.presentation.composable.MyApplicationCard
import org.koin.androidx.compose.koinViewModel

/** S7 Tab "Đá lẻ" screen for browsing mercenary posts, player applications, and team posts. */
@Composable
fun MercenaryListScreen(
    onPostClick: (String) -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MercenaryListViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    MercenaryListContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onPostClick = onPostClick,
        onCreatePostClick = onCreatePostClick,
        onNotificationClick = onNotificationClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MercenaryListContent(
    uiState: MercenaryListUiState,
    onEvent: (MercenaryListEvent) -> Unit,
    onPostClick: (String) -> Unit,
    onCreatePostClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnEvent by androidx.compose.runtime.rememberUpdatedState(onEvent)
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        currentOnEvent(MercenaryListEvent.ClearSnackbar)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        topBar = {
            ArenaTopBar(
                title = "Đá lẻ",
                onNotificationClick = onNotificationClick,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePostClick,
                containerColor = ArenaColors.Primary,
                contentColor = Color.White,
            ) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = "Đăng tin tìm người")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { onEvent(MercenaryListEvent.Refresh) },
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            MercenaryListBody(
                uiState = uiState,
                onEvent = onEvent,
                onOpenFilterSheet = { showFilterSheet = true },
                onPostClick = onPostClick,
            )
        }
    }

    if (showFilterSheet) {
        MercenaryFilterBottomSheet(
            wards = uiState.wards,
            selectedWardId = uiState.selectedWardId,
            selectedFormat = uiState.selectedFormat,
            onWardSelect = { onEvent(MercenaryListEvent.WardFilterChanged(it)) },
            onFormatSelect = { onEvent(MercenaryListEvent.FormatFilterChanged(it)) },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@Composable
private fun MercenaryListBody(
    uiState: MercenaryListUiState,
    onEvent: (MercenaryListEvent) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onPostClick: (String) -> Unit,
) {
    val activeFilterCount =
        listOfNotNull(uiState.selectedWardId, uiState.selectedFormat, uiState.selectedPosition).size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.isLoading) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ArenaColors.Primary,
                )
            }
        }

        item {
            MercenarySegmentedTabs(
                selectedTab = uiState.selectedTab,
                onSelectTab = { onEvent(MercenaryListEvent.SelectTab(it)) },
            )
        }

        if (uiState.selectedTab == MercenaryListTab.EXPLORE) {
            item {
                ArenaSearchFilterBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(MercenaryListEvent.SearchQueryChanged(it)) },
                    onFilterClick = onOpenFilterSheet,
                    placeholder = "Tìm đội, khu vực, ghi chú...",
                    activeFilterCount = activeFilterCount,
                )
            }
            item {
                PositionFilterRow(
                    selectedPosition = uiState.selectedPosition,
                    onSelect = { onEvent(MercenaryListEvent.PositionFilterChanged(it)) },
                )
            }
        }

        if (uiState.error != null) {
            item {
                ArenaErrorState(
                    title = "Không thể tải dữ liệu",
                    message = uiState.error.userMessage(),
                    onRetryClick = { onEvent(MercenaryListEvent.Refresh) },
                )
            }
        } else {
            when (uiState.selectedTab) {
                MercenaryListTab.EXPLORE -> {
                    val filtered =
                        uiState.explorePosts.filter { post ->
                            val q = uiState.searchQuery.trim()
                            q.isBlank() ||
                                post.teamName.contains(q, ignoreCase = true) ||
                                post.areaLabel.contains(q, ignoreCase = true) ||
                                post.notes.contains(q, ignoreCase = true)
                        }
                    if (filtered.isEmpty() && !uiState.isLoading) {
                        item {
                            ArenaEmptyState(
                                title = "Chưa có tin tuyển phù hợp",
                                description = "Thử thay đổi bộ lọc vị trí, sân hoặc khu vực.",
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { post ->
                            MercenaryPostCard(
                                post = post,
                                onCardClick = { onPostClick(post.id) },
                                onQuickApplyClick = { onEvent(MercenaryListEvent.QuickApply(post)) },
                            )
                        }
                    }
                }

                MercenaryListTab.MY_APPLICATIONS -> {
                    if (uiState.myApplications.isEmpty() && !uiState.isLoading) {
                        item {
                            ArenaEmptyState(
                                title = "Bạn chưa gửi đơn nào",
                                description = "Chọn một tin tuyển ở tab Tin tuyển lính để ứng tuyển.",
                            )
                        }
                    } else {
                        items(uiState.myApplications, key = { it.id }) { app ->
                            MyApplicationCard(
                                application = app,
                                onCardClick = { onPostClick(app.postId) },
                            )
                        }
                    }
                }

                MercenaryListTab.MY_TEAM_POSTS -> {
                    if (uiState.myTeamPosts.isEmpty() && !uiState.isLoading) {
                        item {
                            ArenaEmptyState(
                                title = "Đội chưa có tin tuyển nào",
                                description = "Bấm nút + để đăng tin tìm lính đánh thuê cho trận đấu.",
                            )
                        }
                    } else {
                        items(uiState.myTeamPosts, key = { it.id }) { post ->
                            MercenaryPostCard(
                                post = post,
                                onCardClick = { onPostClick(post.id) },
                                onQuickApplyClick = {},
                                showQuickApply = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MercenarySegmentedTabs(
    selectedTab: MercenaryListTab,
    onSelectTab: (MercenaryListTab) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        MercenaryListTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = MercenaryListTab.entries.size),
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PositionFilterRow(
    selectedPosition: PlayerPosition?,
    onSelect: (PlayerPosition?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedPosition == null,
            onClick = { onSelect(null) },
            label = { Text("Tất cả") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ArenaColors.PrimaryContainer),
        )
        PlayerPosition.entries.forEach { position ->
            FilterChip(
                selected = selectedPosition == position,
                onClick = { onSelect(if (selectedPosition == position) null else position) },
                label = { Text(position.label) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ArenaColors.PrimaryContainer),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MercenaryFilterBottomSheet(
    wards: List<WardOption>,
    selectedWardId: String?,
    selectedFormat: PitchFormat?,
    onWardSelect: (String?) -> Unit,
    onFormatSelect: (PitchFormat?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Bộ lọc nâng cao",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text("Loại sân", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedFormat == null,
                    onClick = { onFormatSelect(null) },
                    label = { Text("Tất cả") },
                )
                PitchFormat.entries.forEach { format ->
                    FilterChip(
                        selected = selectedFormat == format,
                        onClick = { onFormatSelect(if (selectedFormat == format) null else format) },
                        label = { Text(format.label) },
                    )
                }
            }
            Text("Khu vực", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedWardId == null,
                    onClick = { onWardSelect(null) },
                    label = { Text("Toàn khu vực") },
                )
                wards.forEach { ward ->
                    FilterChip(
                        selected = selectedWardId == ward.id,
                        onClick = { onWardSelect(if (selectedWardId == ward.id) null else ward.id) },
                        label = { Text(ward.name) },
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
