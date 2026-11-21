@file:Suppress("MaxLineLength", "TooManyFunctions")

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
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppBadge
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.BadgeVariant
import com.igorwojda.showcase.feature.base.presentation.compose.composable.InlineNotice
import com.igorwojda.showcase.feature.base.presentation.compose.composable.KeyValueRow
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import com.uzuu.match.domain.model.MatchCostAgreement
import com.uzuu.match.domain.model.MatchDetail
import com.uzuu.match.domain.model.MatchSummary
import com.uzuu.match.domain.model.MatchTimeSlot
import com.uzuu.match.domain.model.MatchType
import com.uzuu.match.domain.model.MatchVenue
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.localDateTime
import java.text.NumberFormat
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

private const val DEFAULT_MOCK_VENUE_AMOUNT = 600_000
private const val DEFAULT_LOCATION_FALLBACK = "Phường An Khánh, TP. Hồ Chí Minh"
private const val DEFAULT_VENUE_FALLBACK = "Sân An Phú"
private const val DEFAULT_TEAM_NAME = "Bình Minh FC"
private const val DEFAULT_AUTHOR_TEAM = "Thảo Điền FC"
private const val DEFAULT_SKILL_LEVEL = "Trung bình"
private const val FIFTY_FIFTY_SPLIT = "FIFTY_FIFTY"

internal data class MatchDetailActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onSendRequest: (String) -> Unit,
    val onWithdraw: () -> Unit,
    val onChatClick: () -> Unit = {},
)

internal data class MatchActionParams(
    val isSent: Boolean,
    val isHasVenue: Boolean,
    val isMatchOpen: Boolean,
    val isCancelled: Boolean,
    val statusMessage: String,
    val buttonLabel: String,
)

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

    val actions =
        remember(matchId, onBackClick, onChatClick) {
            MatchDetailActions(
                onBack = onBackClick,
                onRetry = { viewModel.load(matchId) },
                onSendRequest = viewModel::sendDemoRequest,
                onWithdraw = viewModel::withdrawDemoRequest,
                onChatClick = onChatClick,
            )
        }

    MatchDetailContent(
        state = state,
        actions = actions,
        modifier = modifier,
    )
}

@Composable
internal fun MatchDetailContent(
    state: MatchDetailUiState,
    actions: MatchDetailActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, containerColor = ArenaColors.Background) { padding ->
        val contentModifier = Modifier.padding(padding)
        val match = state.match
        when {
            state.isLoading -> {
                Box(contentModifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
            }

            state.error != null && state.error != AppError.NotFound -> {
                Box(contentModifier) { AppErrorContent(state.error, actions.onRetry) }
            }

            match == null -> {
                MatchDetailNotFound(actions.onBack, contentModifier)
            }

            else -> {
                MatchDetailLoadedContent(match, state.detail, state.requestTeam, actions, contentModifier)
            }
        }
    }
}

@Composable
private fun MatchDetailNotFound(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Không tìm thấy kèo",
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ArenaColors.TextPrimary),
        )
        Text(text = "Kèo này không tồn tại hoặc đã bị xóa.", color = ArenaColors.TextMuted, modifier = Modifier.padding(vertical = 12.dp))
        Button(
            onClick = onBack,
            shape = ControlShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Text("Quay lại danh sách")
        }
    }
}

@Composable
private fun DetailHeaderSection(
    match: MatchSummary,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = onBack,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp),
            modifier = Modifier.padding(bottom = 6.dp),
        ) {
            Text(
                text = "← Khám phá",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = ArenaColors.TextPrimary),
            )
        }

        val badgeVariant = if (match.type == MatchType.NEED_VENUE) BadgeVariant.Amber else BadgeVariant.Green
        AppBadge(text = "Đang tìm đối", variant = badgeVariant)

        Text(
            text = match.authorTeamName,
            style =
                TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.035).em,
                    lineHeight = 36.sp,
                    color = ArenaColors.TextPrimary,
                ),
            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
        )

        val formatText =
            when (match.format) {
                "FIVE_A_SIDE" -> "Bóng đá 5 người"
                "ELEVEN_A_SIDE" -> "Bóng đá 11 người"
                else -> "Bóng đá 7 người"
            }
        val skillText = if (match.skillLevels.isNotEmpty()) match.skillLevels.joinToString(", ") else DEFAULT_SKILL_LEVEL
        Text(
            text = "$formatText · $skillText",
            style = TextStyle(fontSize = 15.sp, color = ArenaColors.TextMuted, lineHeight = 22.sp),
            modifier = Modifier.padding(bottom = 16.dp),
        )

        KeyValueRow(label = "Ngày thi đấu", value = MatchDetailFormatter.formatDate(match.startsAt))
        KeyValueRow(label = "Khung giờ", value = MatchDetailFormatter.formatTimeRange(match.startsAt, match.endsAt))
    }
}

@Composable
private fun DetailVenueSection(
    match: MatchSummary,
    detail: MatchDetail?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (match.type == MatchType.HAS_VENUE || match.venueName != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = match.venueName ?: DEFAULT_VENUE_FALLBACK,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ArenaColors.TextPrimary),
                )
                Text(
                    text = match.venueAddress ?: DEFAULT_LOCATION_FALLBACK,
                    style = TextStyle(fontSize = 14.sp, color = ArenaColors.TextMuted),
                )
            }
            HorizontalDivider(color = ArenaColors.BorderSubtle, thickness = 1.dp)
        } else {
            TypeBVenueOptionsSection(
                venueOptionCount = match.venueOptionCount,
                venueOptions = detail?.venueOptions ?: emptyList(),
                timeOptionCount = match.timeOptionCount,
                timeOptions = detail?.timeOptions ?: emptyList(),
            )
        }
    }
}

@Composable
private fun DetailHeaderAndLocationSection(
    match: MatchSummary,
    detail: MatchDetail?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        DetailHeaderSection(match, onBack)
        DetailVenueSection(match, detail)
    }
}

@Composable
private fun MatchDetailLoadedContent(
    match: MatchSummary,
    detail: MatchDetail?,
    requestTeam: String?,
    actions: MatchDetailActions,
    modifier: Modifier = Modifier,
) {
    var selectedTeam by rememberSaveable { mutableStateOf(DEFAULT_TEAM_NAME) }
    var showWithdrawDialog by rememberSaveable { mutableStateOf(false) }
    val isSent = requestTeam != null
    val isMatchOpen = match.status == "OPEN"
    val isCancelled = match.status == "CANCELLED"

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp)) {
            DetailHeaderAndLocationSection(match, detail, actions.onBack)
            Text(
                text = "Thỏa thuận chi phí",
                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = ArenaColors.TextPrimary),
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
            )
            CostAgreementSection(costAgreement = detail?.costAgreement)
            InlineNotice(
                text = "Hai đội tự thanh toán bên ngoài. ArenaFlow chỉ ghi nhận thỏa thuận.",
                modifier = Modifier.padding(vertical = 16.dp),
            )
            Text(
                text = "Gửi yêu cầu với đội",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ArenaColors.TextPrimary),
                modifier = Modifier.padding(bottom = 6.dp),
            )
            TeamDropdownSelector(
                selectedTeam = requestTeam ?: selectedTeam,
                onSelectTeam = {
                    selectedTeam = it
                },
                teams = listOf(DEFAULT_TEAM_NAME, "Rồng Vàng FC", "Phượng Hoàng FC"),
                enabled =
                    !isSent && isMatchOpen,
            )
            Spacer(modifier = Modifier.height(16.dp))

            val buttonLabel = MatchDetailFormatter.resolveButtonLabel(isSent, match.type == MatchType.HAS_VENUE)
            val statusMessage = MatchDetailFormatter.resolveStatusMessage(isCancelled, detail?.cancelReason)
            val actionParams =
                MatchActionParams(isSent, match.type == MatchType.HAS_VENUE, isMatchOpen, isCancelled, statusMessage, buttonLabel)
            OpenMatchActions(actionParams, selectedTeam, actions, onWithdrawClick = { showWithdrawDialog = true })
        }

        if (showWithdrawDialog) {
            AlertDialog(
                onDismissRequest = { showWithdrawDialog = false },
                title = { Text("Rút yêu cầu ghép kèo?") },
                text = { Text("Yêu cầu đang chờ duyệt. Rút lúc này không bị trừ điểm uy tín.") },
                confirmButton = {
                    TextButton(onClick = {
                        actions.onWithdraw()
                        showWithdrawDialog = false
                    }) { Text("Rút yêu cầu", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { showWithdrawDialog = false }) { Text("Giữ yêu cầu") } },
            )
        }
    }
}

@Composable
private fun SentMatchActions(
    isHasVenue: Boolean,
    onChatClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = onChatClick,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 48.dp),
            shape = ArenaShapes.Button,
            colors = ButtonDefaults.buttonColors(containerColor = ArenaColors.SecondaryButtonBg, contentColor = ArenaColors.TextPrimary),
        ) {
            Text(
                text = "Nhắn với đội trưởng",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ArenaColors.TextPrimary),
            )
        }

        if (isHasVenue) {
            TextButton(onClick = onWithdrawClick, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(text = "Rút yêu cầu", color = ArenaColors.TextMuted, style = TextStyle(fontSize = 14.sp))
            }
        }
    }
}

@Composable
private fun OpenMatchActions(
    params: MatchActionParams,
    selectedTeam: String,
    actions: MatchDetailActions,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!params.isMatchOpen) {
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 12.dp,
                    ).background(ArenaColors.SecondaryButtonBg, ArenaShapes.Input)
                    .padding(14.dp),
        ) {
            Text(
                text = params.statusMessage,
                color = if (params.isCancelled) MaterialTheme.colorScheme.error else ArenaColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { actions.onSendRequest(selectedTeam) },
            enabled = !params.isSent,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = ArenaShapes.Button,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    disabledContentColor = Color.White.copy(alpha = 0.85f),
                ),
        ) {
            Text(text = params.buttonLabel, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
        }

        if (params.isSent) {
            SentMatchActions(params.isHasVenue, actions.onChatClick, onWithdrawClick)
        }
    }
}

@Composable
private fun VenueOptionsList(
    venueOptionCount: Int,
    venueOptions: List<MatchVenue>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val venueCount = if (venueOptionCount > 0) venueOptionCount else venueOptions.size.coerceAtLeast(2)
        Text(
            text = "$venueCount địa điểm đề xuất",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ArenaColors.TextPrimary),
        )

        if (venueOptions.isNotEmpty()) {
            venueOptions.forEach {
                Text(
                    text = "• ${it.venueName} (${it.address})",
                    style = TextStyle(fontSize = 14.sp, color = ArenaColors.TextPrimary),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        } else {
            Text(
                text = "Sân An Phú · Sân Bình Trưng (thỏa thuận trong phòng)",
                style = TextStyle(fontSize = 14.sp, color = ArenaColors.TextMuted),
            )
        }
    }
}

@Composable
private fun TimeOptionsList(
    timeOptionCount: Int,
    timeOptions: List<MatchTimeSlot>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val timeCount = if (timeOptionCount > 0) timeOptionCount else timeOptions.size.coerceAtLeast(2)
        Text(
            text = "$timeCount khung giờ đề xuất",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ArenaColors.TextPrimary),
            modifier = Modifier.padding(top = 4.dp),
        )

        if (timeOptions.isNotEmpty()) {
            timeOptions.forEach { slot ->
                val timeLabel = "${localDateTime(slot.startsAt, "HH:mm")}–${localDateTime(slot.endsAt, "HH:mm")}"
                Text(
                    text = "• $timeLabel",
                    style = TextStyle(fontSize = 14.sp, color = ArenaColors.TextMuted),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        } else {
            Text(text = "18:00–19:30 · 19:30–21:00", style = TextStyle(fontSize = 14.sp, color = ArenaColors.TextMuted))
        }
    }
}

@Composable
private fun TypeBVenueOptionsSection(
    venueOptionCount: Int,
    venueOptions: List<MatchVenue>,
    timeOptionCount: Int,
    timeOptions: List<MatchTimeSlot>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VenueOptionsList(venueOptionCount, venueOptions)
            TimeOptionsList(timeOptionCount, timeOptions)
        }
        HorizontalDivider(color = ArenaColors.BorderSubtle, thickness = 1.dp)
    }
}

@Composable
private fun CostAgreementSection(
    costAgreement: MatchCostAgreement?,
    modifier: Modifier = Modifier,
) {
    val venueAmount = costAgreement?.venueAmount ?: DEFAULT_MOCK_VENUE_AMOUNT
    val costSplit = costAgreement?.costSplit ?: FIFTY_FIFTY_SPLIT

    Column(modifier = modifier.fillMaxWidth()) {
        KeyValueRow(label = "Tiền sân", value = MatchDetailFormatter.formatCurrency(venueAmount))
        val splitValue =
            if (costSplit ==
                FIFTY_FIFTY_SPLIT
            ) {
                "${MatchDetailFormatter.formatCurrency(venueAmount / 2)} / đội"
            } else {
                "Theo thỏa thuận hai đội"
            }
        val splitLabel = if (costSplit == FIFTY_FIFTY_SPLIT) "Chia 50–50" else "Thỏa thuận chia"
        KeyValueRow(label = splitLabel, value = splitValue)

        costAgreement?.drinksAmount?.let { drinks ->
            KeyValueRow(label = "Tiền nước", value = MatchDetailFormatter.formatCurrency(drinks))
        }
        costAgreement?.depositAmount?.let { deposit ->
            val holder = costAgreement.depositHolder?.let { " · $it" } ?: ""
            KeyValueRow(label = "Tiền cọc$holder", value = MatchDetailFormatter.formatCurrency(deposit))
        }
        costAgreement?.customItems?.forEach { item ->
            KeyValueRow(label = item.itemName, value = MatchDetailFormatter.formatCurrency(item.amount))
        }
    }
}

@Composable
private fun TeamDropdownSelector(
    selectedTeam: String,
    onSelectTeam: (String) -> Unit,
    teams: List<String>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .border(1.dp, if (enabled) ArenaColors.InputBorder else ArenaColors.BorderSubtle, ArenaShapes.Input)
                .background(Color.White, ArenaShapes.Input)
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
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) ArenaColors.TextPrimary else ArenaColors.TextMuted,
                    ),
            )
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Chọn đội", tint = ArenaColors.TextMuted)
        }

        DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            teams.forEach { team ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$team · Bạn là đội trưởng",
                            style =
                                TextStyle(
                                    fontSize = 14.sp,
                                    color = ArenaColors.TextPrimary,
                                    fontWeight =
                                        if (team ==
                                            selectedTeam
                                        ) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                ),
                        )
                    },
                    onClick = {
                        onSelectTeam(team)
                        expanded = false
                    },
                )
            }
        }
    }
}

internal object MatchDetailFormatter {
    private const val UNDEFINED = "Chưa xác định"

    fun resolveButtonLabel(
        isSent: Boolean,
        isHasVenue: Boolean,
    ): String =
        when {
            isSent -> "Đang chờ duyệt"
            isHasVenue -> "Gửi yêu cầu ghép kèo"
            else -> "Nhận kèo & thương lượng"
        }

    fun resolveStatusMessage(
        isCancelled: Boolean,
        cancelReason: String?,
    ): String = if (isCancelled) cancelReason ?: "Kèo đã bị hủy." else "Kèo hiện không nhận yêu cầu mới."

    fun formatDate(startsAt: String): String {
        if (startsAt.isBlank()) return "21/11/2026"
        val formatted = localDateTime(startsAt, "dd/MM/yyyy")
        return if (formatted == UNDEFINED) "21/11/2026" else formatted
    }

    fun formatTimeRange(
        startsAt: String,
        endsAt: String,
    ): String {
        if (startsAt.isBlank() || endsAt.isBlank()) return "19:00–20:30"
        val start = localDateTime(startsAt, "HH:mm")
        val end = localDateTime(endsAt, "HH:mm")
        return if (start == UNDEFINED || end == UNDEFINED) "19:00–20:30" else "$start–$end"
    }

    fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
        return "${formatter.format(amount)} ₫"
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun MatchDetailPreview() {
    ShowcaseTheme {
        MatchDetailContent(
            state =
                MatchDetailUiState(
                    isLoading = false,
                    match =
                        MatchSummary(
                            matchId = "preview-1",
                            type = MatchType.HAS_VENUE,
                            authorTeamName = DEFAULT_AUTHOR_TEAM,
                            venueName = DEFAULT_VENUE_FALLBACK,
                            skillLevels = listOf(DEFAULT_SKILL_LEVEL),
                            startsAt = "2026-11-21T12:00:00Z",
                            endsAt = "2026-11-21T13:30:00Z",
                            format = "SEVEN_A_SIDE",
                            venueAddress = DEFAULT_LOCATION_FALLBACK,
                        ),
                    detail =
                        MatchDetail(
                            summary =
                                MatchSummary(
                                    matchId = "preview-1",
                                    type = MatchType.HAS_VENUE,
                                    authorTeamName = DEFAULT_AUTHOR_TEAM,
                                    venueName = DEFAULT_VENUE_FALLBACK,
                                ),
                            costAgreement =
                                MatchCostAgreement(
                                    venueAmount = DEFAULT_MOCK_VENUE_AMOUNT,
                                    costSplit = FIFTY_FIFTY_SPLIT,
                                ),
                        ),
                ),
            actions =
                MatchDetailActions(
                    onBack = {},
                    onRetry = {},
                    onSendRequest = {},
                    onWithdraw = {},
                ),
        )
    }
}
