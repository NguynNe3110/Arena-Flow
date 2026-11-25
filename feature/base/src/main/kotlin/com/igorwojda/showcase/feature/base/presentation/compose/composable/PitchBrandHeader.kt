@file:Suppress("CognitiveComplexMethod", "LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors

/** Deep Pitch Green (#14532D) brand header with subtle soccer pitch-line Canvas motif. */
@Composable
fun PitchBrandHeader(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    compact: Boolean = false,
    showLogo: Boolean = true,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(ArenaColors.PitchGreen)
                .statusBarsPadding(),
    ) {
        PitchLinesOverlay(modifier = Modifier.matchParentSize())

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (compact) 20.dp else 24.dp,
                        vertical = if (compact) 18.dp else 24.dp,
                    ),
        ) {
            PitchHeaderTopRow(
                showBack = showBack,
                showLogo = showLogo,
                onBackClick = onBackClick,
                trailingContent = trailingContent,
            )

            if (title.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                )
            }
        }
    }
}

@Composable
private fun PitchHeaderTopRow(
    showBack: Boolean,
    showLogo: Boolean,
    onBackClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(8.dp)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                    )
                }
            }

            if (showLogo) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.16f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SportsSoccer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = "ArenaFlow",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        trailingContent?.invoke()
    }
}

@Composable
private fun PitchLinesOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lineColor = Color.White.copy(alpha = 0.12f)
        val stroke = Stroke(width = 1.2.dp.toPx())
        val dashed = Stroke(width = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)))

        drawLine(
            lineColor,
            Offset(0f, size.height * 0.16f),
            Offset(size.width, size.height * 0.16f),
            strokeWidth = dashed.width,
            pathEffect = dashed.pathEffect,
        )
        drawLine(lineColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), strokeWidth = stroke.width)
        drawCircle(lineColor, radius = 56.dp.toPx(), center = Offset(size.width / 2f, size.height), style = stroke)
        drawRect(
            color = lineColor,
            topLeft = Offset(size.width / 2f - 55.dp.toPx(), -2.dp.toPx()),
            size = Size(110.dp.toPx(), 36.dp.toPx()),
            style = stroke,
        )
    }
}
