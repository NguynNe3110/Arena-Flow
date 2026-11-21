package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors

/**
 * Top brand header matching docs/prototype/index.html:
 * - "ArenaFlow." with "." in green #146B46
 * - Right notification bell ghost button
 * - Bottom border 1px solid #DCE3D8
 */
@Composable
fun BrandHeader(
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnreadNotification: Boolean = false,
) {
    val borderColor = ArenaColors.Border

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }.padding(start = 24.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                buildAnnotatedString {
                    append("ArenaFlow")
                    withStyle(SpanStyle(color = ArenaColors.Primary)) {
                        append(".")
                    }
                },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = ArenaColors.TextPrimary,
        )

        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.size(48.dp),
        ) {
            Box {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Thông báo",
                    tint = ArenaColors.TextPrimary,
                    modifier = Modifier.size(24.dp),
                )
                if (hasUnreadNotification) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .background(ArenaColors.Primary, androidx.compose.foundation.shape.CircleShape),
                    )
                }
            }
        }
    }
}
