package com.uzuu.match.presentation.screen.matchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.MatchCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchListScreen(
    onMatchClick: (String) -> Unit,
    onCreateMatchClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchListViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    MatchListContent(uiState, viewModel::onRefresh, onMatchClick, onCreateMatchClick, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchListContent(
    uiState: MatchListUiState,
    onRefresh: () -> Unit,
    onMatchClick: (String) -> Unit,
    onCreateMatchClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf("ALL") }
    var format by rememberSaveable { mutableStateOf("ALL") }
    var skill by rememberSaveable { mutableStateOf("ALL") }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var draftFormat by rememberSaveable { mutableStateOf("ALL") }
    var draftSkill by rememberSaveable { mutableStateOf("ALL") }
    val matches =
        uiState.matches.filter {
            (tab == "ALL" || it.type.name == tab) &&
                (format == "ALL" || it.format == format) &&
                (skill == "ALL" || skill in it.skillLevels)
        }
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tìm kèo giao lưu", Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge)
                IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                    Icon(Icons.Outlined.Refresh, "Tải lại danh sách kèo")
                }
                Button(
                    onClick = onCreateMatchClick,
                    shape = ControlShape,
                    modifier = Modifier.padding(end = 12.dp).heightIn(min = 38.dp),
                ) {
                    Text("+ Đăng kèo", style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(
                "Dữ liệu mẫu · Chưa kết nối backend",
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = {
                        draftFormat = format
                        draftSkill = skill
                        showFilters = true
                    },
                    shape = ControlShape,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Outlined.FilterList, null)
                    val filterLabel = "Bộ lọc" + if (format != "ALL" || skill != "ALL") " · Đang áp dụng" else ""
                    Text(filterLabel, Modifier.padding(start = 8.dp))
                }
                Text("${matches.size} kèo", style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "ALL" to "Tất cả kèo",
                    "HAS_VENUE" to "Đã có sân (Luồng A)",
                    "NEED_VENUE" to "Chưa có sân (Luồng B)",
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = tab == value,
                        onClick = { tab = value },
                        label = { Text(label) },
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = ControlShape,
                    )
                }
            }
            if (uiState.isLoading && uiState.matches.isNotEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (uiState.error != null && uiState.matches.isNotEmpty()) {
                TextButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                    Text("Chưa tải được dữ liệu mới. Chạm để thử lại.")
                }
            }
            when {
                uiState.isLoading && uiState.matches.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
                }

                uiState.error != null && uiState.matches.isEmpty() -> {
                    AppErrorContent(uiState.error, onRefresh)
                }

                matches.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Chưa có kèo nào phù hợp", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Thử thay đổi bộ lọc hoặc chủ động đăng bài tìm đối giao lưu!",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    tab = "ALL"
                                    format = "ALL"
                                    skill = "ALL"
                                }, shape = ControlShape) {
                                    Text("Đặt lại bộ lọc")
                                }
                                Button(onClick = onCreateMatchClick, shape = ControlShape) {
                                    Text("Đăng kèo ngay")
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(matches, key = { it.matchId }) { match ->
                            MatchCard(match, onClick = { onMatchClick(match.matchId) })
                        }
                    }
                }
            }
        }
    }
    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Bộ lọc tìm kèo", style = MaterialTheme.typography.headlineMedium)
                Text("Định dạng sân", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val formats =
                        listOf(
                            "ALL" to "Tất cả",
                            "FIVE_A_SIDE" to "Sân 5",
                            "SEVEN_A_SIDE" to "Sân 7",
                            "ELEVEN_A_SIDE" to "Sân 11",
                        )
                    formats.forEach { (value, label) ->
                        FilterChip(draftFormat == value, { draftFormat = value }, { Text(label) }, Modifier.heightIn(min = 48.dp))
                    }
                }
                Text("Trình độ", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val skills = listOf("ALL") + uiState.matches.flatMap { it.skillLevels }.distinct()
                    skills.forEach { value ->
                        val chipText = if (value == "ALL") "Mọi trình độ" else value
                        FilterChip(draftSkill == value, { draftSkill = value }, { Text(chipText) }, Modifier.heightIn(min = 48.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            draftFormat = "ALL"
                            draftSkill = "ALL"
                        },
                        shape = ControlShape,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text("Đặt lại")
                    }
                    Button(
                        onClick = {
                            format = draftFormat
                            skill = draftSkill
                            showFilters = false
                        },
                        shape = ControlShape,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text("Áp dụng")
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun MatchListContentPreview() {
    ShowcaseTheme {
        MatchListContent(
            MatchListUiState(
                matches =
                    listOf(
                        MatchSummary(
                            "preview-a",
                            MatchType.HAS_VENUE,
                            "Bình Minh FC",
                            "Sân bóng An Phú",
                            listOf("Trung bình"),
                            startsAt = "2026-11-21T12:00:00Z",
                            endsAt = "2026-11-21T13:30:00Z",
                            reliabilityScore = 98,
                        ),
                        MatchSummary(
                            "preview-b",
                            MatchType.NEED_VENUE,
                            "Sài Gòn United",
                            null,
                            venueOptionCount = 2,
                            timeOptionCount = 2,
                        ),
                    ),
            ),
            onRefresh = {},
            onMatchClick = {},
        )
    }
}
