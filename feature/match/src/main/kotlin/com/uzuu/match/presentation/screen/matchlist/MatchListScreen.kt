@file:Suppress("TooManyFunctions", "MagicNumber")

package com.uzuu.match.presentation.screen.matchlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.presentation.composable.MatchCard
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val SUBTITLE_CITY = "TP. Hồ Chí Minh"
private const val SUBTITLE_SAMPLE_DATA = "Dữ liệu mẫu"
private const val TEXT_ALL_DAYS = "Tất cả các ngày"
private const val BRAND_NAME = "ArenaFlow"
private const val LEVEL_MEDIUM = "Trung bình"
private const val DEFAULT_VENUE_NAME = "Sân An Phú"
private const val DEFAULT_VENUE_ADDRESS = "Phường An Khánh"

internal data class MatchListCallbacks(
    val onRefresh: () -> Unit,
    val onFilterChange: (MatchTypeFilter) -> Unit,
    val onDateChange: (String?) -> Unit,
    val onMatchClick: (String) -> Unit,
    val onCreateMatchClick: () -> Unit = {},
    val onNotificationClick: () -> Unit = {},
)

@Composable
fun MatchListScreen(
    onMatchClick: (String) -> Unit,
    onCreateMatchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchListViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    MatchListContent(
        uiState = uiState,
        callbacks =
            MatchListCallbacks(
                onRefresh = viewModel::onRefresh,
                onFilterChange = viewModel::onFilterChanged,
                onDateChange = viewModel::onDateChanged,
                onMatchClick = onMatchClick,
                onCreateMatchClick = onCreateMatchClick,
                onNotificationClick = onNotificationClick,
            ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchListContent(
    uiState: MatchListUiState,
    callbacks: MatchListCallbacks,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFBF7),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                renderMatchListStatus(uiState, callbacks.onRefresh)
                item { MatchListHero(callbacks.onCreateMatchClick) }
                item {
                    MatchListFilterChips(
                        selectedFilter = uiState.selectedFilter,
                        onFilterChange = callbacks.onFilterChange,
                    )
                }
                item {
                    MatchListDateField(
                        selectedDate = uiState.selectedDate,
                        onOpenDatePicker = { showDatePicker = true },
                        onClearDate = { callbacks.onDateChange(null) },
                    )
                }
                renderMatchListBody(uiState, callbacks)
            }
        }
    }

    if (showDatePicker) {
        MatchListDatePickerDialog(
            selectedDate = uiState.selectedDate,
            onDateSelect = { dateStr ->
                callbacks.onDateChange(dateStr)
                scope.launch {
                    snackbarHostState.showSnackbar("Đang hiển thị kèo ngày ${formatDisplayDate(dateStr)}")
                }
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

private fun LazyListScope.renderMatchListStatus(
    uiState: MatchListUiState,
    onRefresh: () -> Unit,
) {
    if (uiState.isLoading && uiState.matches.isNotEmpty()) {
        item {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF146B46),
            )
        }
    }
    if (uiState.error != null && uiState.matches.isNotEmpty()) {
        item {
            TextButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Chưa tải được dữ liệu mới. Chạm để thử lại.",
                    color = Color(0xFF6B4A08),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

private fun LazyListScope.renderMatchListBody(
    uiState: MatchListUiState,
    callbacks: MatchListCallbacks,
) {
    val matches = uiState.filteredMatches
    when {
        uiState.isLoading && uiState.matches.isEmpty() -> {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
        }

        uiState.error != null && uiState.matches.isEmpty() -> {
            item {
                AppErrorContent(uiState.error, callbacks.onRefresh)
            }
        }

        matches.isEmpty() -> {
            item {
                MatchListEmptyContent(
                    onResetFilters = {
                        callbacks.onFilterChange(MatchTypeFilter.ALL)
                        callbacks.onDateChange(null)
                    },
                    onCreateMatchClick = callbacks.onCreateMatchClick,
                )
            }
        }

        else -> {
            items(matches, key = { it.matchId }) { match ->
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 7.dp)) {
                    MatchCard(
                        match = match,
                        onClick = { callbacks.onMatchClick(match.matchId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchListHero(
    onCreateMatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = SUBTITLE_CITY, color = Color(0xFF52675D), fontSize = 13.sp)
            Text(text = SUBTITLE_SAMPLE_DATA, color = Color(0xFF52675D), fontSize = 13.sp)
        }
        Text(
            text = "Tìm đối thủ.\nRa sân thôi.",
            color = Color(0xFF172E27),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
            letterSpacing = (-0.035).sp,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 6.dp),
        )
        Text(
            text = "Một trận hay bắt đầu từ một kèo phù hợp.",
            color = Color(0xFF52675D),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        )
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
            Button(
                onClick = onCreateMatchClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF146B46),
                        contentColor = Color.White,
                    ),
            ) {
                Text(
                    text = "+ Tạo kèo cho đội",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun MatchListFilterChips(
    selectedFilter: MatchTypeFilter,
    onFilterChange: (MatchTypeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MatchTypeFilter.entries.forEach { filterOption ->
            FilterChipItem(
                label = filterOption.label,
                selected = selectedFilter == filterOption,
                onSelect = { onFilterChange(filterOption) },
            )
        }
    }
}

@Composable
private fun MatchListDateField(
    selectedDate: String?,
    onOpenDatePicker: () -> Unit,
    onClearDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 2.dp, bottom = 14.dp),
    ) {
        Text(
            text = "Ngày thi đấu",
            color = Color(0xFF172E27),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 7.dp),
        )
        MatchListDateSurface(selectedDate, onOpenDatePicker, onClearDate)
    }
}

@Composable
private fun MatchListDateSurface(
    selectedDate: String?,
    onOpenDatePicker: () -> Unit,
    onClearDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onOpenDatePicker,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFB2C2B5)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatDisplayDate(selectedDate),
                color = Color(0xFF172E27),
                fontSize = 15.sp,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!selectedDate.isNullOrBlank()) {
                    IconButton(
                        onClick = onClearDate,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Bỏ lọc ngày",
                            tint = Color(0xFF52675D),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Chọn ngày thi đấu",
                    tint = Color(0xFF52675D),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun MatchListEmptyContent(
    onResetFilters: () -> Unit,
    onCreateMatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Chưa có kèo nào phù hợp",
                color = Color(0xFF172E27),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Thử thay đổi bộ lọc hoặc chủ động đăng bài tìm đối giao lưu!",
                color = Color(0xFF52675D),
                fontSize = 14.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = "Đặt lại bộ lọc", color = Color(0xFF172E27))
                }
                Button(
                    onClick = onCreateMatchClick,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF146B46),
                            contentColor = Color.White,
                        ),
                ) {
                    Text(text = "+ Tạo kèo ngay")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchListDatePickerDialog(
    selectedDate: String?,
    onDateSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                remember(selectedDate) {
                    runCatching {
                        selectedDate?.let {
                            LocalDate
                                .parse(it)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toInstant()
                                .toEpochMilli()
                        }
                    }.getOrNull()
                },
        )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateSelect(date.toString())
                    }
                    onDismiss()
                },
            ) {
                Text(text = "Chọn", color = Color(0xFF146B46), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Hủy", color = Color(0xFF52675D))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onSelect,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFF172E27) else Color(0xFFE7EEE6),
        contentColor = if (selected) Color.White else Color(0xFF172E27),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

internal fun formatDisplayDate(dateIso: String?): String {
    if (dateIso.isNullOrBlank()) return TEXT_ALL_DAYS
    val parts = dateIso.split("-")
    return if (parts.size == 3) {
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } else {
        dateIso
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun MatchListContentPreview() {
    ShowcaseTheme {
        MatchListContent(
            uiState =
                MatchListUiState(
                    matches =
                        listOf(
                            MatchSummary(
                                matchId = "preview-a",
                                type = MatchType.HAS_VENUE,
                                authorTeamName = "Thảo Điền FC",
                                venueName = DEFAULT_VENUE_NAME,
                                venueAddress = DEFAULT_VENUE_ADDRESS,
                                skillLevels = listOf(LEVEL_MEDIUM),
                                startsAt = "2026-11-21T19:00:00Z",
                                endsAt = "2026-11-21T20:30:00Z",
                                reliabilityScore = 92,
                            ),
                            MatchSummary(
                                matchId = "preview-b",
                                type = MatchType.NEED_VENUE,
                                authorTeamName = "Sài Gòn United",
                                venueName = null,
                                venueOptionCount = 2,
                                timeOptionCount = 2,
                                skillLevels = listOf(LEVEL_MEDIUM),
                                reliabilityScore = 92,
                                startsAt = "2026-11-21T18:00:00Z",
                            ),
                        ),
                ),
            callbacks =
                MatchListCallbacks(
                    onRefresh = {},
                    onFilterChange = {},
                    onDateChange = {},
                    onMatchClick = {},
                ),
        )
    }
}
