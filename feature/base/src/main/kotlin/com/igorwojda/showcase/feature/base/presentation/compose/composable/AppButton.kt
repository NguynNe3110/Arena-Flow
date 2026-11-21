@file:Suppress("StringLiteralDuplication")

package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaShapes

/** Primary action button. */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = ArenaShapes.Button,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            ),
        contentPadding =
            PaddingValues(
                horizontal = 17.dp,
                vertical = 12.dp,
            ),
    ) {
        AppButtonContent(text = text, isLoading = isLoading)
    }
}

/** Secondary action button. */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = ArenaShapes.Button,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = ArenaColors.SecondaryButtonBg,
                contentColor = ArenaColors.TextPrimary,
                disabledContainerColor = ArenaColors.SecondaryButtonBg.copy(alpha = 0.55f),
                disabledContentColor = ArenaColors.TextPrimary.copy(alpha = 0.55f),
            ),
        contentPadding =
            PaddingValues(
                horizontal = 17.dp,
                vertical = 12.dp,
            ),
    ) {
        AppButtonContent(text = text, isLoading = isLoading)
    }
}

/** Ghost or text action button. */
@Composable
fun AppGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = ArenaShapes.Button,
        colors =
            ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        contentPadding =
            PaddingValues(
                horizontal = 8.dp,
                vertical = 8.dp,
            ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Secondary outlined action button. */
@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled && !isLoading,
        shape = ArenaShapes.Button,
        border =
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            ),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        contentPadding =
            PaddingValues(
                horizontal = 17.dp,
                vertical = 12.dp,
            ),
    ) {
        AppButtonContent(text = text, isLoading = isLoading)
    }
}

@Composable
private fun AppButtonContent(
    text: String,
    isLoading: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
            Spacer(modifier = Modifier.width(Dimen.spaceM))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@PreviewLightDark
@Composable
private fun AppButtonsPreview() {
    AppButtonSamples()
}

@Preview(name = "Large text", widthDp = 320, fontScale = 1.5f)
@Composable
private fun AppButtonsLargeTextPreview() {
    AppButtonSamples()
}

@Composable
private fun AppButtonSamples() {
    ShowcaseTheme(darkTheme = isSystemInDarkTheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(Dimen.spaceL),
                verticalArrangement = Arrangement.spacedBy(Dimen.spaceM),
            ) {
                AppButton(
                    text = "Gửi yêu cầu ghép kèo",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                AppSecondaryButton(
                    text = "Nhắn với đội trưởng",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                AppGhostButton(
                    text = "← Khám phá",
                    onClick = {},
                )
                AppOutlinedButton(
                    text = "Bộ lọc",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
