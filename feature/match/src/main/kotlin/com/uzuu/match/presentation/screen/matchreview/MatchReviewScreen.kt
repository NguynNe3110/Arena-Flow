package com.uzuu.match.presentation.screen.matchreview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.MatchInfo
import com.uzuu.match.presentation.composable.MatchSection
import java.text.NumberFormat
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchReviewScreen(
    onBackClick: () -> Unit,
    onPostSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchReviewViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatchReviewContent(
        uiState = uiState,
        onConfirmPost = { viewModel.confirmPost { onPostSuccess() } },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchReviewContent(
    uiState: MatchReviewUiState,
    onConfirmPost: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = uiState.draft
    val isKindA = draft.kind == MatchType.HAS_VENUE

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Kiểm tra thông tin trước khi đăng", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Notice banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ControlShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "Sau khi đăng, bạn không thể thay đổi giờ thi đấu và địa điểm đã chốt (BR-M02).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Card 1: Đội đăng bài
            MatchSection("Đội đăng bài", Modifier.fillMaxWidth()) {
                Text(draft.hostTeamName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Định dạng: ${when (draft.format) { "FIVE_A_SIDE" -> "Sân 5"; "ELEVEN_A_SIDE" -> "Sân 11"; else -> "Sân 7" }} · Trình độ: ${draft.skillLevel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Card 2: Địa điểm & Thời gian
            MatchSection("Địa điểm & Thời gian thi đấu", Modifier.fillMaxWidth()) {
                if (isKindA) {
                    MatchInfo(Icons.Outlined.LocationOn, draft.venueName ?: "Chưa chọn sân")
                    draft.venueAddress?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    MatchInfo(Icons.Outlined.CalendarMonth, "${draft.matchDate} (${draft.startTime} - ${draft.endTime})")
                } else {
                    Text("${draft.proposedVenues.size} sân đề xuất", style = MaterialTheme.typography.titleSmall)
                    draft.proposedVenues.forEach { venue ->
                        MatchInfo(Icons.Outlined.LocationOn, venue.venueName)
                        Text(venue.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${draft.proposedTimeSlots.size} khung giờ đề xuất (${draft.matchDate})", style = MaterialTheme.typography.titleSmall)
                    draft.proposedTimeSlots.forEach { slot ->
                        MatchInfo(Icons.Outlined.CalendarMonth, slot)
                    }
                }
            }

            // Card 3: Thỏa thuận chi phí
            MatchSection("Thỏa thuận chi phí & Tiền cọc", Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng chi phí ước tính:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(draft.totalCost)} đ",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cách chia tiền:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        when (draft.costSharing) {
                            "SPLIT_50_50" -> "Chia đôi 50/50"
                            "HOST_100" -> "Đội nhà bao sân"
                            "SEVENTY_THIRTY_LOSER" -> "70/30 (Đội thua 70%)"
                            "LOSER_PAYS_DRINKS" -> "Đội thua trả nước"
                            else -> "Theo thỏa thuận"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (draft.depositAmount > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tiền cọc yêu cầu:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(draft.depositAmount)} đ",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }

            // Card 4: Ghi chú
            if (draft.notes.isNotBlank()) {
                MatchSection("Ghi chú giao lưu", Modifier.fillMaxWidth()) {
                    Text(draft.notes, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Error display
            if (uiState.error != null) {
                Text("Không thể đăng bài. Vui lòng thử lại.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Footer actions
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onBackClick,
                    enabled = !uiState.isPosting,
                    shape = ControlShape,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                ) {
                    Text("Sửa lại")
                }
                Button(
                    onClick = onConfirmPost,
                    enabled = !uiState.isPosting,
                    shape = ControlShape,
                    modifier = Modifier.weight(1.5f).heightIn(min = 48.dp),
                ) {
                    if (uiState.isPosting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Xác nhận đăng bài")
                    }
                }
            }
        }
    }
}
