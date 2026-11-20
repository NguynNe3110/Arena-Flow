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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.igorwojda.showcase.feature.base.common.res.Dimen
import com.igorwojda.showcase.feature.base.presentation.compose.theme.ShowcaseTheme

/** Primary action. Loading preserves the label and prevents repeated clicks. */
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
        shape = RoundedCornerShape(8.dp),
        contentPadding =
            PaddingValues(
                horizontal = Dimen.spaceL,
                vertical = 12.dp,
            ),
    ) {
        AppButtonContent(text = text, isLoading = isLoading)
    }
}

/** Secondary action using the same sizing and loading behavior as [AppButton]. */
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
        shape = RoundedCornerShape(8.dp),
        contentPadding =
            PaddingValues(
                horizontal = Dimen.spaceL,
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
                    text = "Áp dụng",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                AppButton(
                    text = "Áp dụng",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                )
                AppButton(
                    text = "Áp dụng",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = true,
                )
                AppOutlinedButton(
                    text = "Bộ lọc",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                AppOutlinedButton(
                    text = "Bộ lọc",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                )
                AppOutlinedButton(
                    text = "Bộ lọc",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = true,
                )
            }
        }
    }
}
