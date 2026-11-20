package com.uzuu.match.presentation.screen.matchdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.igorwojda.showcase.feature.base.domain.error.AppError
import java.text.NumberFormat
import java.util.Locale
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.MatchBadge
import com.uzuu.match.presentation.composable.MatchInfo
import com.uzuu.match.presentation.composable.MatchSection
import com.uzuu.match.presentation.composable.MatchStatusBadge
import com.uzuu.match.presentation.composable.formatLabel
import com.uzuu.match.presentation.composable.localDateTime
import com.uzuu.match.presentation.composable.statusLabel
import com.uzuu.match.presentation.composable.timeLabel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchDetailScreen(
    matchId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MatchDetailViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(matchId) { viewModel.load(matchId) }
    MatchDetailContent(
        state, onBackClick, { viewModel.load(matchId) },
        viewModel::sendDemoRequest, viewModel::withdrawDemoRequest, modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MatchDetailContent(
    state: MatchDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSendRequest: (String) -> Unit,
    onWithdraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTeamPicker by rememberSaveable { mutableStateOf(false) }
    var showWithdraw by rememberSaveable { mutableStateOf(false) }
    var selectedTeam by rememberSaveable { mutableStateOf("Rồng Vàng FC") }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết kèo", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
            )
        },
    ) { padding ->
        val match = state.match
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { LoadingIndicator() }
            state.error != null && state.error != AppError.NotFound -> Box(Modifier.padding(padding)) { AppErrorContent(state.error, onRetry) }
            match == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Không tìm thấy kèo", style = MaterialTheme.typography.headlineMedium)
                Text("Kèo này không tồn tại hoặc đã bị xóa.", Modifier.padding(vertical = 12.dp))
                Button(onClick = onBack, shape = ControlShape) { Text("Quay lại danh sách") }
            }
            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MatchInfo(Icons.Outlined.Info, "Dữ liệu mẫu · Các yêu cầu chỉ được mô phỏng trên thiết bị.")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MatchBadge(if (match.type == MatchType.HAS_VENUE) "Luồng A · Đã có sân" else "Luồng B · Nhiều lựa chọn")
                    MatchBadge(match.formatLabel())
                    MatchStatusBadge(match.status)
                }
                MatchSection("Đội giao lưu", Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TeamSide(match.authorTeamName, "Chủ kèo · Uy tín ${match.reliabilityScore}", Modifier.weight(1f))
                        Text("VS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        TeamSide(match.opponentTeamName ?: "Chờ đối thủ", if (match.opponentTeamName == null) "Đang chờ nhận kèo" else "Đội khách", Modifier.weight(1f))
                    }
                }
                MatchSection("Thời gian & địa điểm", Modifier.fillMaxWidth()) {
                    if (match.type == MatchType.HAS_VENUE || match.venueName != null) {
                        MatchInfo(Icons.Outlined.CalendarMonth, match.timeLabel())
                        MatchInfo(Icons.Outlined.LocationOn, match.venueName ?: "Chưa chốt sân")
                        match.venueAddress?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    } else {
                        Text("${match.venueOptionCount} sân đề xuất", style = MaterialTheme.typography.titleSmall)
                        state.detail?.venueOptions?.forEach { venue ->
                            MatchInfo(Icons.Outlined.LocationOn, venue.venueName)
                            Text(venue.address, style = MaterialTheme.typography.bodySmall)
                            if (venue.venueStatus == "INACTIVE") MatchBadge("Ngừng hoạt động")
                        }
                        Text("${match.timeOptionCount} khung giờ đề xuất", style = MaterialTheme.typography.titleSmall)
                        state.detail?.timeOptions?.forEach { slot ->
                            MatchInfo(Icons.Outlined.CalendarMonth, "${localDateTime(slot.startsAt)} – ${localDateTime(slot.endsAt, "HH:mm")}")
                        }
                        Text("Chọn chính xác một sân và một giờ trong gói thương lượng.", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Hạn nhận kèo: ${localDateTime(match.expiresAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                MatchSection("Trình độ giao lưu", Modifier.fillMaxWidth()) {
                    Text(match.skillLevels.ifEmpty { listOf("Mọi trình độ") }.joinToString(", "), style = MaterialTheme.typography.bodyLarge)
                }
                MatchSection("Thỏa thuận chi phí & tiền cọc", Modifier.fillMaxWidth()) {
                    val cost = state.detail?.costAgreement
                    if (cost == null || (cost.venueAmount == null && cost.drinksAmount == null && cost.customItems.isEmpty())) {
                        Text("Chưa chốt chi phí. Hai đội sẽ thỏa thuận trước khi xác nhận.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        cost.venueAmount?.let { CostRow("Tiền sân", it) }
                        cost.drinksAmount?.let { CostRow("Tiền nước", it) }
                        cost.customItems.forEach { CostRow(it.itemName, it.amount) }
                        Text(when (cost.costSplit) {
                            "FIFTY_FIFTY" -> "Chia đôi 50/50"
                            "LOSER_PAYS_ALL" -> "Đội thua trả toàn bộ"
                            else -> "Theo thỏa thuận hai đội"
                        }, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    cost?.depositAmount?.let { CostRow("Tiền cọc · ${cost.depositHolder ?: "Chưa xác định bên giữ"}", it) }
                    Text("Chi phí và cọc được thanh toán bên ngoài. ArenaFlow không đặt sân hay thu tiền.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.detail?.matchNature?.let {
                    MatchSection("Ghi chú giao lưu", Modifier.fillMaxWidth()) { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
                if (match.status == "CANCELLED") {
                    Text(state.detail?.cancelReason ?: "Kèo đã bị hủy.", color = MaterialTheme.colorScheme.error)
                }
                if (match.status == "OPEN") {
                    if (state.requestTeam == null) {
                        Button(
                            onClick = { showTeamPicker = true },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            shape = ControlShape,
                        ) { Text(if (match.type == MatchType.HAS_VENUE) "Gửi yêu cầu bắt kèo" else "Nhận kèo & thương lượng") }
                    } else {
                        MatchSection(if (match.type == MatchType.HAS_VENUE) "Yêu cầu đang chờ" else "Đã nhận kèo để thương lượng", Modifier.fillMaxWidth()) {
                            Text("${state.requestTeam} · Mô phỏng trên thiết bị")
                            Text(
                                if (match.type == MatchType.HAS_VENUE) "Đang chờ đội chủ nhà duyệt yêu cầu."
                                else "Chưa chốt trận. Hai đội cần xác nhận cùng một gói sân, giờ và chi phí.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        if (match.type == MatchType.HAS_VENUE) {
                            OutlinedButton(
                                onClick = { showWithdraw = true },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                shape = ControlShape,
                            ) { Text("Rút yêu cầu") }
                        }
                    }
                } else {
                    Text("Kèo hiện không nhận yêu cầu mới.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    if (showTeamPicker) {
        ModalBottomSheet(onDismissRequest = { showTeamPicker = false }) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Chọn đội gửi yêu cầu", style = MaterialTheme.typography.headlineMedium)
                Text("Đội trưởng mẫu · Chỉ mô phỏng UI", style = MaterialTheme.typography.bodySmall)
                listOf("Rồng Vàng FC", "Phượng Hoàng FC").forEach { team ->
                    OutlinedButton(onClick = { selectedTeam = team }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = ControlShape) {
                        RadioButton(selectedTeam == team, onClick = null)
                        Text(team, Modifier.weight(1f).padding(start = 8.dp))
                    }
                }
                Button(
                    onClick = { onSendRequest(selectedTeam); showTeamPicker = false },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = ControlShape,
                ) { Text(if (state.match?.type == MatchType.NEED_VENUE) "Nhận kèo mẫu" else "Gửi yêu cầu mẫu") }
            }
        }
    }
    if (showWithdraw) {
        AlertDialog(
            onDismissRequest = { showWithdraw = false },
            title = { Text("Rút yêu cầu bắt kèo?") },
            text = { Text("Yêu cầu đang chờ duyệt. Rút lúc này không bị trừ điểm uy tín.") },
            confirmButton = { TextButton(onClick = { onWithdraw(); showWithdraw = false }) { Text("Rút yêu cầu") } },
            dismissButton = { TextButton(onClick = { showWithdraw = false }) { Text("Giữ yêu cầu") } },
        )
    }
}

@Composable
private fun CostRow(label: String, amount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("${NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(amount)} đ", style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun TeamSide(name: String, description: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(Modifier.size(48.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) { Text(name.take(2).uppercase(), style = MaterialTheme.typography.titleMedium) }
        }
        Text(name, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun MatchDetailPreview() {
    ShowcaseTheme {
        MatchDetailContent(
            MatchDetailUiState(isLoading = false, match = MatchSummary(
                "preview", MatchType.HAS_VENUE, "Bình Minh FC", "Sân bóng An Phú", listOf("Trung bình"),
                startsAt = "2026-11-21T12:00:00Z", endsAt = "2026-11-21T13:30:00Z",
                expiresAt = "2026-11-21T11:00:00Z", reliabilityScore = 98,
                venueAddress = "Số 8 Đường số 2, Phường An Khánh, TP. Thủ Đức",
            )),
            onBack = {}, onRetry = {}, onSendRequest = {}, onWithdraw = {},
        )
    }
}
