package com.uzuu.match.presentation.screen.matchlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
        onRefresh = viewModel::onRefresh,
        onFilterChanged = viewModel::onFilterChanged,
        onDateChanged = viewModel::onDateChanged,
        onMatchClick = onMatchClick,
        onCreateMatchClick = onCreateMatchClick,
        onNotificationClick = onNotificationClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchListContent(
    uiState: MatchListUiState,
    onRefresh: () -> Unit,
    onFilterChanged: (MatchTypeFilter) -> Unit,
    onDateChanged: (String?) -> Unit,
    onMatchClick: (String) -> Unit,
    onCreateMatchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAFBF7),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFAFBF7),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("ArenaFlow")
                                withStyle(SpanStyle(color = Color(0xFF146B46))) {
                                    append(".")
                                }
                            },
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.04).sp,
                            color = Color(0xFF172E27),
                        )
                        IconButton(
                            onClick = {
                                if (onNotificationClick != {}) {
                                    onNotificationClick()
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Chưa có thông báo mới")
                                    }
                                }
                            },
                            modifier = Modifier.size(44.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Thông báo",
                                tint = Color(0xFF172E27),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFDCE3D8), thickness = 1.dp)
                }
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val matches = uiState.filteredMatches

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                // Progress indicator during refresh
                if (uiState.isLoading && uiState.matches.isNotEmpty()) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF146B46),
                        )
                    }
                }

                // Retry notice if refresh failed while retaining existing items
                if (uiState.error != null && uiState.matches.isNotEmpty()) {
                    item {
                        TextButton(
                            onClick = onRefresh,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Chưa tải được dữ liệu mới. Chạm để thử lại.",
                                color = Color(0xFF6B4A08),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }

                // Subtitle: "TP. Hồ Chí Minh · Dữ liệu mẫu"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "TP. Hồ Chí Minh",
                            color = Color(0xFF52675D),
                            fontSize = 13.sp,
                        )
                        Text(
                            text = "Dữ liệu mẫu",
                            color = Color(0xFF52675D),
                            fontSize = 13.sp,
                        )
                    }
                }

                // Heading & Muted Subtitle
                item {
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
                }

                // Full-width primary CTA button
                item {
                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                        Button(
                            onClick = onCreateMatchClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
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

                // Chip row: "Tất cả", "Đã có sân", "Chưa có sân"
                item {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MatchTypeFilter.entries.forEach { filterOption ->
                            FilterChipItem(
                                label = filterOption.label,
                                selected = uiState.selectedFilter == filterOption,
                                onClick = { onFilterChanged(filterOption) },
                            )
                        }
                    }
                }

                // Date picker / filter
                item {
                    Column(
                        modifier = Modifier
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
                        Surface(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFB2C2B5)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formatDisplayDate(uiState.selectedDate),
                                    color = Color(0xFF172E27),
                                    fontSize = 15.sp,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    if (!uiState.selectedDate.isNullOrBlank()) {
                                        IconButton(
                                            onClick = { onDateChanged(null) },
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
                }

                // Match items or Empty/Loading/Error states
                when {
                    uiState.isLoading && uiState.matches.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                LoadingIndicator()
                            }
                        }
                    }

                    uiState.error != null && uiState.matches.isEmpty() -> {
                        item {
                            AppErrorContent(uiState.error, onRefresh)
                        }
                    }

                    matches.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
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
                                            onClick = {
                                                onFilterChanged(MatchTypeFilter.ALL)
                                                onDateChanged(null)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            Text("Đặt lại bộ lọc", color = Color(0xFF172E27))
                                        }
                                        Button(
                                            onClick = onCreateMatchClick,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF146B46),
                                                contentColor = Color.White,
                                            ),
                                        ) {
                                            Text("+ Tạo kèo ngay")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        items(matches, key = { it.matchId }) { match ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 7.dp)) {
                                MatchCard(
                                    match = match,
                                    onClick = { onMatchClick(match.matchId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(uiState.selectedDate) {
                runCatching {
                    uiState.selectedDate?.let {
                        LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    }
                }.getOrNull()
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            onDateChanged(date.toString())
                            scope.launch {
                                snackbarHostState.showSnackbar("Đang hiển thị kèo ngày ${formatDisplayDate(date.toString())}")
                            }
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Chọn", color = Color(0xFF146B46), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = Color(0xFF52675D))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
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
    if (dateIso.isNullOrBlank()) return "Tất cả các ngày"
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
            uiState = MatchListUiState(
                matches = listOf(
                    MatchSummary(
                        matchId = "preview-a",
                        type = MatchType.HAS_VENUE,
                        authorTeamName = "Thảo Điền FC",
                        venueName = "Sân An Phú",
                        venueAddress = "Phường An Khánh",
                        skillLevels = listOf("Trung bình"),
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
                        skillLevels = listOf("Trung bình"),
                        reliabilityScore = 92,
                        startsAt = "2026-11-21T18:00:00Z",
                    ),
                ),
            ),
            onRefresh = {},
            onFilterChanged = {},
            onDateChanged = {},
            onMatchClick = {},
        )
    }
}
