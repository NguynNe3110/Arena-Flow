package com.uzuu.match.presentation.screen.matchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppOutlinedButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.presentation.composable.MatchCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchListScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: MatchListViewModel = koinViewModel()

    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    MatchListContent(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        modifier = modifier,
    )
}

@Composable
internal fun MatchListContent(
    uiState: MatchListUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Tìm kèo giao lưu",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                )

                AppButton(
                    text = "Tải lại",
                    onClick = onRefresh,
                    enabled = !uiState.isLoading && uiState.matches.isNotEmpty(),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    uiState.matches.isNotEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.error != null) {
                                Text(
                                    text = "Chưa tải được dữ liệu mới",
                                    style = MaterialTheme.typography.bodyMedium,
                                )

                                AppOutlinedButton(
                                    text = "Thử lại",
                                    onClick = onRefresh,
                                    enabled = !uiState.isLoading,
                                )
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = uiState.matches,
                                    key = { match -> match.matchId },
                                ) { match ->
                                    MatchCard(match = match)
                                }
                            }
                        }
                    }


                    uiState.isLoading -> {
                        LoadingIndicator()
                    }

                    uiState.error != null -> {
                        AppErrorContent(
                            error = uiState.error,
                            onRetry = onRefresh,
                        )
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Chưa có kèo nào",
                                style = MaterialTheme.typography.titleMedium,
                            )

                            AppOutlinedButton(
                                text = "Tải lại danh sách",
                                onClick = onRefresh,
                            )
                        }
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
            uiState = MatchListUiState(
                matches = listOf(
                    MatchSummary(
                        matchId = "preview-a",
                        type = MatchType.HAS_VENUE,
                        authorTeamName = "Team Alpha",
                        venueName = "Sân bóng Hoa Lư",
                        skillLevels = listOf("Nhiệt tình"),
                    ),
                    MatchSummary(
                        matchId = "preview-b",
                        type = MatchType.NEED_VENUE,
                        authorTeamName = "Team Beta",
                        venueName = null,
                    ),
                ),
            ),
            onRefresh = {},
        )
    }
}
