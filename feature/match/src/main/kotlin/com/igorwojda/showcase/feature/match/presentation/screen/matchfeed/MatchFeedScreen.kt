@file:Suppress(
    "CognitiveComplexMethod",
    "LabeledExpression",
    "LongMethod",
    "LongParameterList",
    "MagicNumber",
    "TooManyFunctions",
    "UnusedImports",
)

package com.igorwojda.showcase.feature.match.presentation.screen.matchfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.domain.model.SkillLevel
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ActingTeamSelectorSheet
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppFilterChip
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.ArenaSearchFilterBar
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.igorwojda.showcase.feature.match.BuildConfig
import com.igorwojda.showcase.feature.match.domain.model.MatchFilter
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.presentation.composable.MatchFeedCard
import org.koin.androidx.compose.koinViewModel

/** S3 Home / Match Discovery feed screen (MATCH-01, MATCH-02, MATCH-07). */
@Composable
fun MatchFeedScreen(
    onMatchClick: (String) -> Unit,
    onCreateMatchClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    onDebugClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchFeedViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    MatchFeedContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onMatchClick = onMatchClick,
        onCreateMatchClick = onCreateMatchClick,
        onNotificationClick = onNotificationClick,
        onDebugClick = onDebugClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchFeedContent(
    uiState: MatchFeedUiState,
    onEvent: (MatchFeedEvent) -> Unit,
    onMatchClick: (String) -> Unit,
    onCreateMatchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onDebugClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnEvent by androidx.compose.runtime.rememberUpdatedState(onEvent)
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showTeamSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        currentOnEvent(MatchFeedEvent.ConsumeSnackbar)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateMatchClick,
                containerColor = ArenaColors.Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Đăng kèo mới", fontWeight = FontWeight.Bold) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            PitchBrandHeader(
                compact = true,
                title = "Tìm kèo giao lưu",
                subtitle = "Kết nối nhanh các đội bóng khu vực TP.HCM",
                trailingContent = {
                    HeaderActions(
                        actingTeamName = uiState.actingTeam?.name,
                        unreadNotifications = uiState.unreadNotifications,
                        onTeamPillClick = { if (uiState.captainedTeams.isNotEmpty()) showTeamSheet = true },
                        onNotificationClick = onNotificationClick,
                        onDebugClick = onDebugClick,
                    )
                },
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { onEvent(MatchFeedEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                MatchFeedBody(
                    uiState = uiState,
                    onEvent = onEvent,
                    onMatchClick = onMatchClick,
                    onOpenFilters = { showFilterSheet = true },
                    onCreateMatchClick = onCreateMatchClick,
                )
            }
        }
    }

    if (showTeamSheet && uiState.captainedTeams.isNotEmpty()) {
        ActingTeamSelectorSheet(
            teams = uiState.captainedTeams,
            selectedTeamId = uiState.actingTeam?.id,
            onSelectTeam = { onEvent(MatchFeedEvent.SelectActingTeam(it)) },
            onDismiss = { showTeamSheet = false },
        )
    }

    if (showFilterSheet) {
        MatchFilterBottomSheet(
            initialFilter = uiState.filter,
            wards = uiState.wards,
            onApply = {
                onEvent(MatchFeedEvent.ApplyFilter(it))
                showFilterSheet = false
            },
            onReset = {
                onEvent(MatchFeedEvent.ResetFilter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@Composable
private fun HeaderActions(
    actingTeamName: String?,
    unreadNotifications: Int,
    onTeamPillClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onDebugClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!actingTeamName.isNullOrBlank()) {
            Row(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape)
                        .clickable(onClick = onTeamPillClick)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = actingTeamName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Outlined.SwapHoriz,
                    contentDescription = "Đổi đội",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        IconButton(onClick = onNotificationClick) {
            Box {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Thông báo",
                    tint = Color.White,
                )
                if (unreadNotifications > 0) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ArenaColors.ErrorBright),
                    )
                }
            }
        }

        if (BuildConfig.DEBUG) {
            IconButton(onClick = onDebugClick) {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = "Debug Controls",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun MatchFeedBody(
    uiState: MatchFeedUiState,
    onEvent: (MatchFeedEvent) -> Unit,
    onMatchClick: (String) -> Unit,
    onOpenFilters: () -> Unit,
    onCreateMatchClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MatchFeedTab.entries.forEach { tab ->
                    AppFilterChip(
                        text = tab.label,
                        selected = uiState.activeTab == tab,
                        onClick = { onEvent(MatchFeedEvent.SelectTab(tab)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            ArenaSearchFilterBar(
                query = uiState.filter.query,
                onQueryChange = { onEvent(MatchFeedEvent.QueryChanged(it)) },
                onFilterClick = onOpenFilters,
                placeholder = "Tìm đội, sân hoặc phường...",
                activeFilterCount = uiState.activeFilterCount,
            )
        }

        if (uiState.activeTab == MatchFeedTab.DISCOVER) {
            item {
                QuickFilterRow(
                    filter = uiState.filter,
                    onSelectType = { onEvent(MatchFeedEvent.SelectQuickType(it)) },
                    onSelectFormat = { onEvent(MatchFeedEvent.SelectQuickFormat(it)) },
                )
            }
        }

        when {
            uiState.isLoading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp)) {
                        LoadingIndicator()
                    }
                }
            }

            uiState.errorMessage != null -> {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ArenaColors.Error,
                        )
                        AppSecondaryButton(
                            text = "Thử lại",
                            onClick = { onEvent(MatchFeedEvent.Refresh) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            uiState.visibleMatches.isEmpty() -> {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SportsSoccer,
                                contentDescription = null,
                                tint = ArenaColors.TextMuted,
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = "Chưa có kèo phù hợp",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ArenaColors.TextHeading,
                            )
                            Text(
                                text = "Thử nới lỏng bộ lọc hoặc đăng kèo mới để tìm đối nhanh hơn.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ArenaColors.TextMuted,
                            )
                            AppButton(
                                text = "Đăng kèo mới",
                                onClick = onCreateMatchClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            else -> {
                items(items = uiState.visibleMatches, key = { it.matchId }) { match ->
                    MatchFeedCard(
                        match = match,
                        isOwnTeamMatch = match.authorTeamId == uiState.actingTeam?.id,
                        onClick = { onMatchClick(match.matchId) },
                        onQuickRequestClick = {
                            if (match.type == MatchType.HAS_VENUE) {
                                onEvent(MatchFeedEvent.QuickSendRequest(match.matchId))
                            } else {
                                onMatchClick(match.matchId)
                            }
                        },
                    )
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun QuickFilterRow(
    filter: MatchFilter,
    onSelectType: (MatchType?) -> Unit,
    onSelectFormat: (String?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppFilterChip(
            text = "Tất cả",
            selected = filter.type == null && filter.format == null,
            onClick = {
                onSelectType(null)
                onSelectFormat(null)
            },
        )
        AppFilterChip(
            text = "Đã có sân",
            selected = filter.type == MatchType.HAS_VENUE,
            onClick = { onSelectType(if (filter.type == MatchType.HAS_VENUE) null else MatchType.HAS_VENUE) },
        )
        AppFilterChip(
            text = "Chưa có sân",
            selected = filter.type == MatchType.NEED_VENUE,
            onClick = { onSelectType(if (filter.type == MatchType.NEED_VENUE) null else MatchType.NEED_VENUE) },
        )
        PitchFormat.entries.forEach { pitch ->
            AppFilterChip(
                text = pitch.label,
                selected = filter.format == pitch.name,
                onClick = { onSelectFormat(if (filter.format == pitch.name) null else pitch.name) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MatchFilterBottomSheet(
    initialFilter: MatchFilter,
    wards: List<com.igorwojda.showcase.feature.base.domain.model.WardOption>,
    onApply: (MatchFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(initialFilter) { mutableStateOf(initialFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ArenaColors.CardBg,
        shape = ArenaShapes.Sheet,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Bộ lọc tìm kèo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
            )

            Text("Loại sân", style = MaterialTheme.typography.titleSmall, color = ArenaColors.TextHeading)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PitchFormat.entries.forEach { fmt ->
                    AppFilterChip(
                        text = fmt.label,
                        selected = draft.format == fmt.name,
                        onClick = { draft = draft.copy(format = if (draft.format == fmt.name) null else fmt.name) },
                    )
                }
            }

            if (wards.isNotEmpty()) {
                Text("Khu vực (Phường)", style = MaterialTheme.typography.titleSmall, color = ArenaColors.TextHeading)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    wards.forEach { ward ->
                        AppFilterChip(
                            text = ward.name,
                            selected = draft.wardId == ward.id,
                            onClick = { draft = draft.copy(wardId = if (draft.wardId == ward.id) null else ward.id) },
                        )
                    }
                }
            }

            Text("Trình độ", style = MaterialTheme.typography.titleSmall, color = ArenaColors.TextHeading)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillLevel.entries.forEach { level ->
                    AppFilterChip(
                        text = level.label,
                        selected = draft.skillLevel == level.label,
                        onClick = {
                            draft = draft.copy(skillLevel = if (draft.skillLevel == level.label) null else level.label)
                        },
                    )
                }
            }

            OutlinedTextField(
                value = draft.dateFrom.orEmpty(),
                onValueChange = { value ->
                    val cleaned = value.ifBlank { null }
                    draft = draft.copy(dateFrom = cleaned, dateTo = cleaned)
                },
                label = { Text("Ngày đá (YYYY-MM-DD)") },
                placeholder = { Text("2026-11-21") },
                singleLine = true,
                shape = ArenaShapes.Input,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppSecondaryButton(
                    text = "Đặt lại",
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Áp dụng",
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
