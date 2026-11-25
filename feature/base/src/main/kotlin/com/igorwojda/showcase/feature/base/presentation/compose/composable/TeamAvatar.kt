package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Circular team avatar aligned with Prototype v2. */
@Composable
fun TeamAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    ArenaAvatar(
        name = name,
        modifier = modifier,
        size = size,
    )
}
