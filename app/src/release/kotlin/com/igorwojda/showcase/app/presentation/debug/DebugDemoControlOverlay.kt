package com.igorwojda.showcase.app.presentation.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

const val IS_DEMO_CONTROLS_AVAILABLE: Boolean = false

/** No-op demo controls overlay in release builds. */
@Suppress("UnusedParameter")
@Composable
fun DebugDemoControlOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // No-op in release builds.
}
