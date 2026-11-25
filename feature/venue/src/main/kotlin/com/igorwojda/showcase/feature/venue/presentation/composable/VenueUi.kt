@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.venue.presentation.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.igorwojda.showcase.feature.base.domain.model.PitchFormat
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

internal fun PitchFormat?.venueLabel(): String =
    when (this) {
        PitchFormat.FIVE_A_SIDE -> "Sân 5 người"
        PitchFormat.SEVEN_A_SIDE -> "Sân 7 người"
        PitchFormat.ELEVEN_A_SIDE -> "Sân 11 người"
        null -> "Sân cỏ nhân tạo"
    }

@Composable
internal fun VenuePhotoThumbnail(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(ArenaShapes.Card)
                .background(ArenaColors.PitchGreen),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http", ignoreCase = true)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Stadium,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
internal fun VenueInactiveBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ArenaShapes.Card,
        color = ArenaColors.ErrorBg,
        border = BorderStroke(1.dp, ArenaColors.ErrorBorder),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.Block,
                contentDescription = null,
                tint = ArenaColors.ErrorBright,
                modifier = Modifier.size(20.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Sân ngừng hoạt động",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ArenaColors.ErrorBright,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = ArenaColors.ErrorBright,
                )
            }
        }
    }
}
