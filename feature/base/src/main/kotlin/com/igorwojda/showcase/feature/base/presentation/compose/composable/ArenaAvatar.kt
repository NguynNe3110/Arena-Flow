@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private val AvatarPalette =
    listOf(
        Color(0xFF14532D) to Color.White,
        Color(0xFF3D6943) to Color.White,
        Color(0xFF2D5A36) to Color(0xFFE9FFE6),
        Color(0xFF5C6330) to Color(0xFFF6FFBC),
        Color(0xFF465846) to Color(0xFFEAFFE7),
        Color(0xFF1E3A2F) to Color(0xFFBDEFBF),
    )

/** Deterministic initials avatar matching ArenaUI.avatar without external broken URLs. */
@Composable
fun ArenaAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    cornerRadius: Dp? = null,
    isDeleted: Boolean = false,
) {
    val clean = name.trim().ifBlank { "AF" }
    val parts = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
    val initials =
        when {
            isDeleted -> "?"
            parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
            else -> clean.take(2).uppercase()
        }

    val (bg, fg) =
        if (isDeleted) {
            Color(0xFFE5E7EB) to Color(0xFF6B7280)
        } else {
            AvatarPalette[abs(clean.hashCode()) % AvatarPalette.size]
        }

    val shape = if (cornerRadius != null) RoundedCornerShape(cornerRadius) else CircleShape

    Box(
        modifier =
            modifier
                .size(size)
                .clip(shape)
                .background(bg)
                .border(1.dp, Color.Black.copy(alpha = 0.08f), shape)
                .semantics { contentDescription = clean },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).coerceAtLeast(11f).sp,
        )
    }
}
