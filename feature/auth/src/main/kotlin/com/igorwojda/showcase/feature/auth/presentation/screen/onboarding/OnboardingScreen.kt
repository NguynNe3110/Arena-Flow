package com.igorwojda.showcase.feature.auth.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igorwojda.showcase.feature.base.presentation.compose.composable.PitchBrandHeader
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes
import org.koin.androidx.compose.koinViewModel

private data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val onboardingSlides =
    listOf(
        OnboardingSlide(
            icon = Icons.Outlined.Handshake,
            title = "Tìm kèo & Ghép đối ngang trình",
            description = "Khám phá kèo A đã có sân hoặc kèo B thương lượng linh hoạt theo khu vực, thời gian và chi phí minh bạch.",
        ),
        OnboardingSlide(
            icon = Icons.Outlined.PersonSearch,
            title = "Tuyển người đá lẻ chuẩn vị trí",
            description = "Thiếu thủ môn hay tiền đạo? Đăng tin tuyển theo vị trí, duyệt ứng viên dựa trên điểm uy tín cá nhân.",
        ),
        OnboardingSlide(
            icon = Icons.Outlined.VerifiedUser,
            title = "Xây dựng uy tín đội bóng",
            description = "Đánh giá hai chiều sau mỗi trận, ghi nhận tỷ lệ hoàn thành kèo và quản lý lịch sử thành viên rõ ràng.",
        ),
    )

/** APP-02: 3-slide onboarding screen introducing ArenaFlow core workflows. */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val slide = onboardingSlides[uiState.currentSlide.coerceIn(0, onboardingSlides.lastIndex)]

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(ArenaColors.Background),
    ) {
        PitchBrandHeader(
            compact = true,
            showLogo = true,
            trailingContent = {
                TextButton(
                    onClick = { viewModel.skip(onComplete) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = "Bỏ qua",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                    )
                }
            },
        )

        OnboardingSlideContent(
            slide = slide,
            currentSlide = uiState.currentSlide,
            totalSlides = onboardingSlides.size,
            onSelectSlide = viewModel::selectSlide,
            modifier = Modifier.weight(1f),
        )

        OnboardingBottomBar(
            isLastSlide = uiState.isLastSlide,
            onNextClick = { viewModel.nextSlide(onComplete) },
        )
    }
}

@Composable
private fun OnboardingSlideContent(
    slide: OnboardingSlide,
    currentSlide: Int,
    totalSlides: Int,
    onSelectSlide: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = ArenaColors.PrimaryContainer.copy(alpha = 0.6f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    tint = ArenaColors.PitchGreen,
                    modifier = Modifier.size(44.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = slide.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ArenaColors.TextHeading,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyMedium,
            color = ArenaColors.TextMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        SlideIndicatorRow(
            currentSlide = currentSlide,
            totalSlides = totalSlides,
            onSelectSlide = onSelectSlide,
        )
    }
}

@Composable
private fun SlideIndicatorRow(
    currentSlide: Int,
    totalSlides: Int,
    onSelectSlide: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSlides) { index ->
            val selected = index == currentSlide
            Box(
                modifier =
                    Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onSelectSlide(index) }
                        .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .height(8.dp)
                            .width(if (selected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (selected) ArenaColors.Primary else ArenaColors.Border),
                )
            }
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    isLastSlide: Boolean,
    onNextClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(ArenaColors.CardBg)
                .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = ArenaColors.BorderSubtle)
        Button(
            onClick = onNextClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .heightIn(min = 48.dp),
            shape = ArenaShapes.Button,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = ArenaColors.Primary,
                    contentColor = ArenaColors.OnPrimary,
                ),
        ) {
            Text(
                text = if (isLastSlide) "Bắt đầu ngay" else "Tiếp tục",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
