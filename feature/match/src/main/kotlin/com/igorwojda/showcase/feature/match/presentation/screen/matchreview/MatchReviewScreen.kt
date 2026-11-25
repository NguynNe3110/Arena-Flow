@file:Suppress("CognitiveComplexMethod", "LongMethod")

package com.igorwojda.showcase.feature.match.presentation.screen.matchreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppCard
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppSecondaryButton
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.match.domain.model.MatchType
import com.igorwojda.showcase.feature.match.presentation.composable.MatchUiFormatters
import org.koin.androidx.compose.koinViewModel

/** S6 Match Review screen before final submission (MATCH-06). */
@Composable
fun MatchReviewScreen(
    onBackClick: () -> Unit,
    onPostSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchReviewViewModel = koinViewModel()
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val currentOnPostSuccess by androidx.compose.runtime.rememberUpdatedState(onPostSuccess)

    LaunchedEffect(uiState.postedMatchId) {
        if (uiState.postedMatchId != null) {
            currentOnPostSuccess()
        }
    }

    MatchReviewContent(
        uiState = uiState,
        onConfirmPost = { viewModel.onEvent(MatchReviewEvent.ConfirmPost) },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun MatchReviewContent(
    uiState: MatchReviewUiState,
    onConfirmPost: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = uiState.draft

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ArenaColors.Background,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PitchBrandHeader(
                compact = true,
                showBack = true,
                onBackClick = onBackClick,
                title = "Xem lại trước khi đăng",
                subtitle = draft.hostTeamName,
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                InlineNotice(text = "Vui lòng kiểm tra kỹ sân, giờ đá và thỏa thuận chi phí trước khi đăng công khai.")

                if (!uiState.errorMessage.isNullOrBlank()) {
                    InlineNotice(text = uiState.errorMessage)
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    KeyValueRow(
                        label = "Loại kèo",
                        value = if (draft.kind == MatchType.HAS_VENUE) "Đã có sân (Luồng A)" else "Chưa có sân (Luồng B)",
                    )
                    KeyValueRow(label = "Thể thức", value = MatchUiFormatters.formatLabel(draft.format))
                    KeyValueRow(label = "Trình độ", value = draft.skillLevel)
                    KeyValueRow(label = "Ngày đá", value = draft.matchDate)
                    KeyValueRow(label = "Khung giờ", value = "${draft.startTime} - ${draft.endTime}")
                    KeyValueRow(
                        label = "Sân bóng",
                        value = draft.venueName ?: draft.proposedVenues.joinToString(", ") { it.venueName }.ifBlank { "Chưa chọn" },
                    )
                    KeyValueRow(
                        label = "Chia tiền",
                        value = MatchUiFormatters.costSplitLabel(draft.costSplit),
                        showDivider = draft.venueAmount > 0,
                    )
                    if (draft.venueAmount > 0) {
                        KeyValueRow(label = "Tiền sân", value = MatchUiFormatters.formatVnd(draft.venueAmount), showDivider = false)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppSecondaryButton(
                        text = "Sửa lại",
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                    )
                    AppButton(
                        text = "Xác nhận đăng kèo",
                        onClick = onConfirmPost,
                        isLoading = uiState.isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
