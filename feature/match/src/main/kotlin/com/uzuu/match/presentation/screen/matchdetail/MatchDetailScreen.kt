package com.uzuu.match.presentation.screen.matchdetail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.domain.error.AppError
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.uzuu.match.domain.model.MatchCostAgreement
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchTimeSlot
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.model.MatchVenue
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.formatLabel
import com.uzuu.match.presentation.composable.localDateTime
import java.text.NumberFormat
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

// Design tokens matching docs/prototype/index.html & IMPLEMENTATION_PLAN.md
private val GreenPrimary = Color(0xFF146B46)
private val TextPrimary = Color(0xFF172E27)
private val TextMuted = Color(0xFF52675D)
private val AppBackground = Color(0xFFFAFBF7)
private val LineDividerColor = Color(0xFFE2E7DE)
private val SecondaryButtonBg = Color(0xFFE7EEE6)
private val BadgeGreenBg = Color(0xFFDFEFDF)
private val BadgeGreenText = Color(0xFF155B39)
private val BadgeAmberBg = Color(0xFFFFF0CE)
private val BadgeAmberText = Color(0xFF6B4A08)
private val NoticeBg = Color(0xFFF1F0DF)
private val InputBorder = Color(0xFFB2C2B5)

@Composable
fun MatchDetailScreen(
    matchId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onChatClick: () -> Unit = {},
) {
    val viewModel: MatchDetailViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(matchId) { viewModel.load(matchId) }

    MatchDetailContent(
        state = state,
        onBack = onBackClick,
        onRetry = { viewModel.load(matchId) },
        onSendRequest = viewModel::sendDemoRequest,
        onWithdraw = viewModel::withdrawDemoRequest,
        onChatClick = onChatClick,
        modifier = modifier,
    )
}

@Composable
internal fun MatchDetailContent(
    state: MatchDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSendRequest: (String) -> Unit,
    onWithdraw: () -> Unit,
    modifier: Modifier = Modifier,
    onChatClick: () -> Unit = {},
) {
    var selectedTeam by rememberSaveable { mutableStateOf("Bình Minh FC") }
    var showWithdraw by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = AppBackground,
    ) { padding ->
        val match = state.match
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }

            state.error != null && state.error != AppError.NotFound -> {
                Box(modifier = Modifier.padding(padding)) {
                    AppErrorContent(state.error, onRetry)
                }
            }

            match == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Không tìm thấy kèo",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        ),
                    )
                    Text(
                        text = "Kèo này không tồn tại hoặc đã bị xóa.",
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                    Button(
                        onClick = onBack,
                        shape = ControlShape,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    ) {
                        Text("Quay lại danh sách")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    // Back button: ghost "← Khám phá"
                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp),
                        modifier = Modifier.padding(bottom = 6.dp),
                    ) {
                        Text(
                            text = "← Khám phá",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            ),
                        )
                    }

                    // Status badge: "Đang tìm đối"
                    DetailStatusBadge(
                        status = match.status,
                        type = match.type,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Large team heading: e.g. "Thảo Điền FC"
                    Text(
                        text = match.authorTeamName,
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.035).em,
                            lineHeight = 36.sp,
                            color = TextPrimary,
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    )

                    // Subtitle: e.g. "Bóng đá 7 người · Trung bình"
                    val formatText = when (match.format) {
                        "FIVE_A_SIDE" -> "Bóng đá 5 người"
                        "ELEVEN_A_SIDE" -> "Bóng đá 11 người"
                        else -> "Bóng đá 7 người"
                    }
                    val skillText = if (match.skillLevels.isNotEmpty()) {
                        match.skillLevels.joinToString(", ")
                    } else {
                        "Trung bình"
                    }
                    Text(
                        text = "$formatText · $skillText",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = TextMuted,
                            lineHeight = 22.sp,
                        ),
                        modifier = Modifier.padding(bottom = 16.dp),
                    )

                    // Key-value rows with dividers (.line)
                    // 1. Ngày thi đấu
                    KeyValueRow(
                        label = "Ngày thi đấu",
                        value = formatDate(match.startsAt),
                    )

                    // 2. Khung giờ
                    KeyValueRow(
                        label = "Khung giờ",
                        value = formatTimeRange(match.startsAt, match.endsAt),
                    )

                    // 3. Sân & địa chỉ
                    if (match.type == MatchType.HAS_VENUE || match.venueName != null) {
                        VenueAddressRow(
                            venueName = match.venueName ?: "Sân An Phú",
                            address = match.venueAddress ?: "Phường An Khánh, TP. Hồ Chí Minh",
                        )
                    } else {
                        // Type B multi-venue & time options
                        TypeBVenueOptionsSection(
                            venueOptionCount = match.venueOptionCount,
                            venueOptions = state.detail?.venueOptions ?: emptyList(),
                            timeOptionCount = match.timeOptionCount,
                            timeOptions = state.detail?.timeOptions ?: emptyList(),
                        )
                    }

                    // "Thỏa thuận chi phí" heading
                    Text(
                        text = "Thỏa thuận chi phí",
                        style = TextStyle(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        ),
                        modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
                    )

                    // Cost rows: Tiền sân (600.000 ₫), Chia 50–50 (300.000 ₫ / đội), etc.
                    CostAgreementSection(
                        costAgreement = state.detail?.costAgreement,
                    )

                    // Notice callout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .background(color = NoticeBg, shape = RoundedCornerShape(12.dp))
                            .padding(14.dp),
                    ) {
                        Text(
                            text = "Hai đội tự thanh toán bên ngoài. ArenaFlow chỉ ghi nhận thỏa thuận.",
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = TextPrimary,
                            ),
                        )
                    }

                    // Team selection dropdown ("Gửi yêu cầu với đội": "Bình Minh FC · Bạn là đội trưởng")
                    Text(
                        text = "Gửi yêu cầu với đội",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        ),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )

                    val isSent = state.requestTeam != null
                    val isMatchOpen = match.status == "OPEN"

                    TeamDropdownSelector(
                        selectedTeam = state.requestTeam ?: selectedTeam,
                        onTeamSelected = { selectedTeam = it },
                        teams = listOf("Bình Minh FC", "Rồng Vàng FC", "Phượng Hoàng FC"),
                        enabled = !isSent && isMatchOpen,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary button: "Gửi yêu cầu ghép kèo" -> switches to "Đang chờ duyệt" (disabled)
                    if (isMatchOpen) {
                        Button(
                            onClick = {
                                onSendRequest(selectedTeam)
                            },
                            enabled = !isSent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = GreenPrimary.copy(alpha = 0.55f),
                                disabledContentColor = Color.White.copy(alpha = 0.85f),
                            ),
                        ) {
                            Text(
                                text = if (isSent) {
                                    "Đang chờ duyệt"
                                } else {
                                    if (match.type == MatchType.HAS_VENUE) {
                                        "Gửi yêu cầu ghép kèo"
                                    } else {
                                        "Nhận kèo & thương lượng"
                                    }
                                },
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }

                        // When sent: secondary button "Nhắn với đội trưởng"
                        if (isSent) {
                            Button(
                                onClick = onChatClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .heightIn(min = 48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecondaryButtonBg,
                                    contentColor = TextPrimary,
                                ),
                            ) {
                                Text(
                                    text = "Nhắn với đội trưởng",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                    ),
                                )
                            }

                            if (match.type == MatchType.HAS_VENUE) {
                                TextButton(
                                    onClick = { showWithdraw = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                ) {
                                    Text(
                                        text = "Rút yêu cầu",
                                        color = TextMuted,
                                        style = TextStyle(fontSize = 14.sp),
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .background(Color(0xFFF1F5EC), RoundedCornerShape(10.dp))
                                .padding(14.dp),
                        ) {
                            Text(
                                text = if (match.status == "CANCELLED") {
                                    state.detail?.cancelReason ?: "Kèo đã bị hủy."
                                } else {
                                    "Kèo hiện không nhận yêu cầu mới."
                                },
                                color = if (match.status == "CANCELLED") MaterialTheme.colorScheme.error else TextMuted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWithdraw) {
        AlertDialog(
            onDismissRequest = { showWithdraw = false },
            title = { Text("Rút yêu cầu ghép kèo?") },
            text = { Text("Yêu cầu đang chờ duyệt. Rút lúc này không bị trừ điểm uy tín.") },
            confirmButton = {
                TextButton(onClick = {
                    onWithdraw()
                    showWithdraw = false
                }) { Text("Rút yêu cầu", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showWithdraw = false }) { Text("Giữ yêu cầu") }
            },
        )
    }
}

@Composable
private fun DetailStatusBadge(
    status: String,
    type: MatchType,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor, label) = when {
        status == "CONFIRMED" -> Triple(BadgeGreenBg, BadgeGreenText, "Đã ghép kèo")
        status == "CANCELLED" -> Triple(Color(0xFFFFEAEA), Color(0xFFBA1A1A), "Đã hủy")
        type == MatchType.NEED_VENUE -> Triple(BadgeAmberBg, BadgeAmberText, "Chưa có sân · Đang tìm đối")
        else -> Triple(BadgeGreenBg, BadgeGreenText, "Đang tìm đối")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        color = bgColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            ),
        )
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = TextMuted,
                ),
            )
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )
        }
        HorizontalDivider(
            color = LineDividerColor,
            thickness = 1.dp,
        )
    }
}

@Composable
private fun VenueAddressRow(
    venueName: String,
    address: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = venueName,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            ),
        )
        Text(
            text = address,
            style = TextStyle(
                fontSize = 14.sp,
                color = TextMuted,
            ),
        )
    }
    HorizontalDivider(
        color = LineDividerColor,
        thickness = 1.dp,
    )
}

@Composable
private fun TypeBVenueOptionsSection(
    venueOptionCount: Int,
    venueOptions: List<MatchVenue>,
    timeOptionCount: Int,
    timeOptions: List<MatchTimeSlot>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val venueCount = if (venueOptionCount > 0) venueOptionCount else venueOptions.size.coerceAtLeast(2)
        Text(
            text = "$venueCount địa điểm đề xuất",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            ),
        )

        if (venueOptions.isNotEmpty()) {
            venueOptions.forEach { venue ->
                Column(modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                    Text(
                        text = "• ${venue.venueName}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        ),
                    )
                    Text(
                        text = venue.address,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = TextMuted,
                        ),
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
        } else {
            Text(
                text = "Sân An Phú · Sân Bình Trưng (thỏa thuận trong phòng)",
                style = TextStyle(fontSize = 14.sp, color = TextMuted),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val timeCount = if (timeOptionCount > 0) timeOptionCount else timeOptions.size.coerceAtLeast(2)
        Text(
            text = "$timeCount khung giờ đề xuất",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            ),
        )

        if (timeOptions.isNotEmpty()) {
            timeOptions.forEach { slot ->
                val slotTime = "${localDateTime(slot.startsAt, "HH:mm")}–${localDateTime(slot.endsAt, "HH:mm")}"
                Text(
                    text = "• $slotTime",
                    style = TextStyle(fontSize = 14.sp, color = TextMuted),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        } else {
            Text(
                text = "18:00–19:30 · 19:30–21:00",
                style = TextStyle(fontSize = 14.sp, color = TextMuted),
            )
        }
    }
    HorizontalDivider(
        color = LineDividerColor,
        thickness = 1.dp,
    )
}

@Composable
private fun CostAgreementSection(
    costAgreement: MatchCostAgreement?,
    modifier: Modifier = Modifier,
) {
    val venueAmount = costAgreement?.venueAmount ?: 600000
    val costSplit = costAgreement?.costSplit ?: "FIFTY_FIFTY"

    Column(modifier = modifier.fillMaxWidth()) {
        KeyValueRow(
            label = "Tiền sân",
            value = formatCurrency(venueAmount),
        )

        val splitValue = when (costSplit) {
            "FIFTY_FIFTY" -> "${formatCurrency(venueAmount / 2)} / đội"
            "LOSER_PAYS_ALL" -> "Đội thua trả toàn bộ"
            else -> "Theo thỏa thuận hai đội"
        }
        val splitLabel = when (costSplit) {
            "FIFTY_FIFTY" -> "Chia 50–50"
            else -> "Thỏa thuận chia"
        }
        KeyValueRow(
            label = splitLabel,
            value = splitValue,
        )

        costAgreement?.drinksAmount?.let { drinks ->
            KeyValueRow(
                label = "Tiền nước",
                value = formatCurrency(drinks),
            )
        }

        costAgreement?.depositAmount?.let { deposit ->
            val holder = costAgreement.depositHolder?.let { " · $it" } ?: ""
            KeyValueRow(
                label = "Tiền cọc$holder",
                value = formatCurrency(deposit),
            )
        }

        costAgreement?.customItems?.forEach { item ->
            KeyValueRow(
                label = item.itemName,
                value = formatCurrency(item.amount),
            )
        }
    }
}

@Composable
private fun TeamDropdownSelector(
    selectedTeam: String,
    onTeamSelected: (String) -> Unit,
    teams: List<String>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .border(1.dp, if (enabled) InputBorder else LineDividerColor, RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { expanded = true }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$selectedTeam · Bạn là đội trưởng",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) TextPrimary else TextMuted,
                ),
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Chọn đội",
                tint = TextMuted,
            )
        }

        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White),
        ) {
            teams.forEach { team ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$team · Bạn là đội trưởng",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = if (team == selectedTeam) FontWeight.Bold else FontWeight.Normal,
                            ),
                        )
                    },
                    onClick = {
                        onTeamSelected(team)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun formatDate(startsAt: String): String {
    if (startsAt.isBlank()) return "21/11/2026"
    val formatted = localDateTime(startsAt, "dd/MM/yyyy")
    return if (formatted == "Chưa xác định") "21/11/2026" else formatted
}

private fun formatTimeRange(startsAt: String, endsAt: String): String {
    if (startsAt.isBlank() || endsAt.isBlank()) return "19:00–20:30"
    val start = localDateTime(startsAt, "HH:mm")
    val end = localDateTime(endsAt, "HH:mm")
    return if (start == "Chưa xác định" || end == "Chưa xác định") {
        "19:00–20:30"
    } else {
        "$start–$end"
    }
}

private fun formatCurrency(amount: Int): String {
    val formatter = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
    return "${formatter.format(amount)} ₫"
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun MatchDetailTypeAPreview() {
    ShowcaseTheme {
        MatchDetailContent(
            state = MatchDetailUiState(
                isLoading = false,
                match = MatchSummary(
                    matchId = "preview-1",
                    type = MatchType.HAS_VENUE,
                    authorTeamName = "Thảo Điền FC",
                    venueName = "Sân An Phú",
                    skillLevels = listOf("Trung bình"),
                    startsAt = "2026-11-21T12:00:00Z",
                    endsAt = "2026-11-21T13:30:00Z",
                    format = "SEVEN_A_SIDE",
                    venueAddress = "Phường An Khánh, TP. Hồ Chí Minh",
                ),
                detail = MatchDetail(
                    summary = MatchSummary(
                        matchId = "preview-1",
                        type = MatchType.HAS_VENUE,
                        authorTeamName = "Thảo Điền FC",
                        venueName = "Sân An Phú",
                    ),
                    costAgreement = MatchCostAgreement(
                        venueAmount = 600000,
                        costSplit = "FIFTY_FIFTY",
                    ),
                ),
            ),
            onBack = {},
            onRetry = {},
            onSendRequest = {},
            onWithdraw = {},
        )
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun MatchDetailSentPreview() {
    ShowcaseTheme {
        MatchDetailContent(
            state = MatchDetailUiState(
                isLoading = false,
                requestTeam = "Bình Minh FC",
                match = MatchSummary(
                    matchId = "preview-2",
                    type = MatchType.HAS_VENUE,
                    authorTeamName = "Thảo Điền FC",
                    venueName = "Sân An Phú",
                    skillLevels = listOf("Trung bình"),
                    startsAt = "2026-11-21T12:00:00Z",
                    endsAt = "2026-11-21T13:30:00Z",
                    format = "SEVEN_A_SIDE",
                    venueAddress = "Phường An Khánh, TP. Hồ Chí Minh",
                ),
            ),
            onBack = {},
            onRetry = {},
            onSendRequest = {},
            onWithdraw = {},
        )
    }
}
