package com.igorwojda.showcase.feature.base.presentation.compose.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaColors
import com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic.ArenaTypography

@Composable
fun ShowcaseTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = getColorScheme(darkTheme, dynamicColor),
        typography = ArenaTypography,
        content = content,
    )
}

@Composable
private fun getColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
): ColorScheme {
    val context = LocalContext.current
    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> {
            ArenaColors.Dark
        }

        else -> {
            ArenaColors.Light
        }
    }
}
