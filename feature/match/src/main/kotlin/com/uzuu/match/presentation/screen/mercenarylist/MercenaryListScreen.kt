package com.uzuu.match.presentation.screen.mercenarylist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsHandball
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.AppErrorContent
import com.igorwojda.showcase.feature.base.presentation.compose.composable.LoadingIndicator
import com.uzuu.match.domain.model.MercenaryPost
import com.uzuu.match.presentation.composable.ControlShape
import com.uzuu.match.presentation.composable.MatchShape
import org.koin.androidx.compose.koinViewModel

@Composable
fun MercenaryListScreen(
    onPostClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: MercenaryListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MercenaryListContent(
        uiState = uiState,
        onFilterPosition = viewModel::setFilterPosition,
        onRadiusChange = viewModel::setRadius,
        onSelectPostForApply = viewModel::selectPostForApply,
        onApply = viewModel::applyToPost,
        onRetry = viewModel::loadPosts,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MercenaryListContent(
    uiState: MercenaryListUiState,
    onFilterPosition: (String) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onSelectPostForApply: (MercenaryPost?) -> Unit,
    onApply: (String, String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var applyMessage by rememberSaveable { mutableStateOf("") }

    val filteredPosts =
        uiState.posts.filter { post ->
            (uiState.filterPosition == "ALL" || post.position == uiState.filterPosition) &&
                post.distanceKm <= uiState.radiusKm
        }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            // Header
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.SportsSoccer, null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "ArenaFlow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )

            // Sub-header & GPS Banner
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Đá lẻ - Tìm người ra sân", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Outlined.MyLocation, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "GPS Bật",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Text(
                    "Ghép vị trí tức thì • Không trung gian sân bóng",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Filter Pills: Vị trí thi đấu
            Row(
                modifier =
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "ALL" to "Tất cả",
                    "GK" to "Thủ môn (GK)",
                    "DF" to "Hậu vệ (DF)",
                    "MF" to "Tiền vệ (MF)",
                    "FW" to "Tiền đạo (FW)",
                ).forEach { (pos, label) ->
                    FilterChip(
                        selected = uiState.filterPosition == pos,
                        onClick = { onFilterPosition(pos) },
                        label = { Text(label) },
                        shape = ControlShape,
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }

            // GPS Radius Card
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                shape = MatchShape,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.NearMe, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("Bán kính quét: ", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "%.1f km".format(uiState.radiusKm),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Surface(shape = ControlShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Text(
                                "Tối đa 20 km",
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    Slider(
                        value = uiState.radiusKm,
                        onValueChange = onRadiusChange,
                        valueRange = 1f..20f,
                        steps = 37,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1 km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("Mặc định: 3 km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("20 km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Định vị GPS: Phường An Khánh, TP. Thủ Đức",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Posts count header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Kèo cần người gấp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${filteredPosts.size} tin gần bạn",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Content List
            when {
                uiState.isLoading && uiState.posts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { LoadingIndicator() }
                }

                uiState.error != null && uiState.posts.isEmpty() -> {
                    AppErrorContent(uiState.error, onRetry)
                }

                filteredPosts.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Chưa có tin tuyển người phù hợp", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Hãy tăng bán kính quét hoặc đổi vị trí thi đấu.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding =
                            androidx.compose.foundation.layout
                                .PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(filteredPosts, key = { it.id }) { post ->
                            MercenaryCard(post = post, onApplyClick = { onSelectPostForApply(post) })
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Ứng tuyển nhanh
    uiState.selectedPostForApply?.let { post ->
        ModalBottomSheet(onDismissRequest = { onSelectPostForApply(null) }) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Ứng tuyển đá lẻ", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Đội: ${post.teamName} · ${post.positionLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("Thời gian: ${post.matchDate} (${post.timeSlot})", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = applyMessage,
                    onValueChange = { applyMessage = it },
                    label = { Text("Lời nhắn tới Đội trưởng (kinh nghiệm, vị trí, liên hệ)") },
                    placeholder = { Text("VD: Tôi đá được 3 năm, có giày và trang phục đầy đủ...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = ControlShape,
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onSelectPostForApply(null) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = ControlShape,
                    ) {
                        Text("Hủy bỏ")
                    }
                    Button(
                        onClick = {
                            onApply(post.id, applyMessage)
                            applyMessage = ""
                        },
                        enabled = !uiState.isApplying,
                        modifier = Modifier.weight(1.5f).heightIn(min = 48.dp),
                        shape = ControlShape,
                    ) {
                        if (uiState.isApplying) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Gửi đơn ứng tuyển")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MercenaryCard(
    post: MercenaryPost,
    onApplyClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MatchShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Top Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(Modifier.size(40.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                post.teamName.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(post.teamName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Icon(Icons.Outlined.Verified, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            "Đội trưởng: ${post.captainName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Outlined.Shield, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "Uy tín ${post.reliabilityScore}/100",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Text(
                        "${post.totalMatches} trận đá phủi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            // Big Position Badge (Amber color)
            Surface(
                shape = ControlShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.SportsHandball,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        post.positionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            // Bento Grid (2 cols)
            Surface(shape = ControlShape, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                            Text(
                                "Thời gian thi đấu",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                        Text(post.matchDate, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            post.timeSlot,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                            Text(
                                "Khu vực ước tính",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                        Text(post.estimatedArea, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Cách bạn ~${post.distanceKm} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Security Rule Notice Box (AC-P05)
            Surface(
                shape = ControlShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(
                        "Nhằm bảo mật và tránh tập trung trước giờ, TÊN SÂN CỤ THỂ, ĐỊA CHỈ CHI TIẾT " +
                            "VÀ TỌA ĐỘ BẢN ĐỒ KHÔNG HIỂN THỊ TRƯỚC KHI ĐỘI TRƯỞNG DUYỆT ĐƠN (AC-P05).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Requirements & Perks
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.CheckCircle, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Yêu cầu: ${post.requirements}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.WaterDrop, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Quyền lợi: ${post.costShare}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Status Summary
            Row(
                Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "● Còn trống ${post.openSlots} suất",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Đã có ${post.pendingApplicants} ứng viên chờ duyệt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // CTA Button (48dp height touch target)
            if (post.isApplied) {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = ControlShape,
                ) {
                    Text("✓ Đã nộp đơn (Đang chờ duyệt)", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = ControlShape,
                ) {
                    Icon(Icons.Outlined.HowToReg, null, modifier = Modifier.size(18.dp))
                    Text(
                        "Ứng tuyển vị trí ${post.position}",
                        Modifier.padding(start = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
