@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

/** Empty state card aligned with ArenaUI.emptyState in Prototype v2. */
@Composable
fun ArenaEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ArenaColors.SurfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ArenaColors.Primary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = ArenaColors.TextMuted,
                textAlign = TextAlign.Center,
            )
            if (!actionLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                AppButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

/** Error state card with Back + Retry recovery aligned with ArenaUI.errorState. */
@Composable
fun ArenaErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    errorCode: String? = null,
    onBackClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = ArenaColors.ErrorBg,
        border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = ArenaColors.ErrorBright,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.ErrorBright,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
            )
            if (!errorCode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AppBadge(text = "Mã lỗi: $errorCode", variant = BadgeVariant.Error)
            }
            if (onBackClick != null || onRetryClick != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (onBackClick != null) {
                        AppOutlinedButton(text = "Quay lại", onClick = onBackClick)
                    }
                    if (onRetryClick != null) {
                        Button(
                            onClick = onRetryClick,
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = ArenaShapes.Button,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = ArenaColors.ErrorBright,
                                    contentColor = Color.White,
                                ),
                        ) {
                            Text("Thử lại", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

/** Loading indicator card aligned with ArenaUI.loadingState. */
@Composable
fun ArenaLoadingState(
    modifier: Modifier = Modifier,
    message: String = "Đang tải dữ liệu...",
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(
            color = ArenaColors.Primary,
            trackColor = ArenaColors.Border,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = ArenaColors.TextMuted,
        )
    }
}
