@file:Suppress("LongMethod", "LongParameterList", "MagicNumber")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors

/** Standard white Top App Bar matching ArenaUI.topBar in docs/prototype/js/components.js. */
@Composable
fun ArenaTopBar(
    modifier: Modifier = Modifier,
    title: String = "",
    showBack: Boolean = false,
    userName: String = "ArenaFlow",
    unreadNotificationCount: Int = 0,
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onBrandClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val borderColor = ArenaColors.BorderSubtle

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }.height(56.dp)
                .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArenaTopBarLeading(
            title = title,
            showBack = showBack,
            onMenuClick = onMenuClick,
            onBackClick = onBackClick,
            onBrandClick = onBrandClick,
            modifier = Modifier.weight(1f),
        )

        ArenaTopBarTrailing(
            userName = userName,
            unreadNotificationCount = unreadNotificationCount,
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
        )
    }
}

@Composable
private fun ArenaTopBarLeading(
    title: String,
    showBack: Boolean,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onBrandClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick = if (showBack) onBackClick else onMenuClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = if (showBack) Icons.AutoMirrored.Outlined.ArrowBack else Icons.Outlined.Menu,
                contentDescription = if (showBack) "Quay lại" else "Menu điều khiển",
                tint = ArenaColors.TextPrimary,
            )
        }

        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ArenaColors.TextHeading,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBrandClick)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ArenaColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SportsSoccer,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = "ArenaFlow",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.PitchGreen,
                )
            }
        }
    }
}

@Composable
private fun ArenaTopBarTrailing(
    userName: String,
    unreadNotificationCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Thông báo",
                    tint = ArenaColors.TextPrimary,
                )
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(ArenaColors.ErrorBright)
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(48.dp),
        ) {
            ArenaAvatar(name = userName, size = 34.dp)
        }
    }
}
