@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.igorwojda.showcase.feature.base.presentation.compose.theme.primitive.ColorPalette

/** Material 3 ColorScheme and semantic tokens mapped from docs/prototype/index.html & IMPLEMENTATION_PLAN.md. */
object ArenaColors {
    val Primary = ColorPalette.Primary
    val OnPrimary = ColorPalette.OnPrimary
    val TextPrimary = ColorPalette.TextPrimary
    val TextMuted = ColorPalette.TextMuted
    val Background = ColorPalette.Background
    val CardBg = ColorPalette.CardBg
    val Border = ColorPalette.Border
    val BorderSubtle = ColorPalette.BorderSubtle
    val SecondaryButtonBg = ColorPalette.SecondaryButtonBg
    val ChipActiveBg = ColorPalette.ChipActiveBg
    val ChipActiveText = ColorPalette.ChipActiveText
    val ChipInactiveBg = ColorPalette.ChipInactiveBg
    val ChipInactiveText = ColorPalette.ChipInactiveText
    val BadgeGreenBg = ColorPalette.BadgeGreenBg
    val BadgeGreenText = ColorPalette.BadgeGreenText
    val BadgeAmberBg = ColorPalette.BadgeAmberBg
    val BadgeAmberText = ColorPalette.BadgeAmberText
    val NoticeBg = ColorPalette.NoticeBg
    val NavActiveBg = ColorPalette.NavActiveBg
    val TeamAvatarBg = ColorPalette.TeamAvatarBg
    val InputBorder = ColorPalette.InputBorder

    val Light =
        lightColorScheme(
            primary = ColorPalette.Primary,
            onPrimary = ColorPalette.OnPrimary,
            primaryContainer = ColorPalette.BadgeGreenBg,
            onPrimaryContainer = ColorPalette.BadgeGreenText,
            inversePrimary = ColorPalette.GreenLight,
            secondary = ColorPalette.TextPrimary,
            onSecondary = ColorPalette.White,
            secondaryContainer = ColorPalette.NavActiveBg,
            onSecondaryContainer = ColorPalette.Primary,
            tertiary = ColorPalette.BadgeAmberText,
            onTertiary = ColorPalette.White,
            tertiaryContainer = ColorPalette.BadgeAmberBg,
            onTertiaryContainer = ColorPalette.BadgeAmberText,
            background = ColorPalette.Background,
            onBackground = ColorPalette.TextPrimary,
            surface = ColorPalette.Background,
            onSurface = ColorPalette.TextPrimary,
            surfaceVariant = ColorPalette.SecondaryButtonBg,
            onSurfaceVariant = ColorPalette.TextMuted,
            surfaceContainerLowest = ColorPalette.CardBg,
            surfaceContainerLow = ColorPalette.CardBg,
            surfaceContainer = ColorPalette.NoticeBg,
            surfaceContainerHigh = ColorPalette.NavActiveBg,
            surfaceContainerHighest = ColorPalette.SecondaryButtonBg,
            surfaceBright = ColorPalette.White,
            surfaceDim = ColorPalette.BorderSubtle,
            outline = ColorPalette.Border,
            outlineVariant = ColorPalette.Border,
            error = ColorPalette.Error,
            onError = ColorPalette.White,
            errorContainer = ColorPalette.ErrorContainer,
            onErrorContainer = ColorPalette.OnErrorContainer,
            scrim = ColorPalette.Black,
        )

    val Dark =
        darkColorScheme(
            primary = Color(0xFF68DC99),
            onPrimary = Color(0xFF003920),
            primaryContainer = Color(0xFF146B46),
            onPrimaryContainer = Color(0xFFDFEFDF),
            inversePrimary = Color(0xFF146B46),
            secondary = Color(0xFFB5DDBE),
            onSecondary = Color(0xFF172E27),
            secondaryContainer = Color(0xFF284438),
            onSecondaryContainer = Color(0xFFE5EEE1),
            tertiary = Color(0xFFFFD480),
            onTertiary = Color(0xFF452B00),
            tertiaryContainer = Color(0xFF6B4A08),
            onTertiaryContainer = Color(0xFFFFF0CE),
            background = Color(0xFF111815),
            onBackground = Color(0xFFFAFBF7),
            surface = Color(0xFF111815),
            onSurface = Color(0xFFFAFBF7),
            surfaceVariant = Color(0xFF1D2823),
            onSurfaceVariant = Color(0xFFA5B7AC),
            surfaceContainerLowest = Color(0xFF0C120F),
            surfaceContainerLow = Color(0xFF18221D),
            surfaceContainer = Color(0xFF1D2823),
            surfaceContainerHigh = Color(0xFF23302A),
            surfaceContainerHighest = Color(0xFF2E3D36),
            surfaceBright = Color(0xFF35463E),
            surfaceDim = Color(0xFF0C120F),
            outline = Color(0xFF3E5047),
            outlineVariant = Color(0xFF2E3D36),
            error = Color(0xFFFFB4A0),
            onError = Color(0xFF6E1400),
            errorContainer = Color(0xFF7D260F),
            onErrorContainer = Color(0xFFFFDAD4),
            scrim = Color(0xFF000000),
        )
}

// Semantic convenience accessors
val ColorScheme.badgeGreenBg: Color get() = ColorPalette.BadgeGreenBg
val ColorScheme.badgeGreenText: Color get() = ColorPalette.BadgeGreenText
val ColorScheme.badgeAmberBg: Color get() = ColorPalette.BadgeAmberBg
val ColorScheme.badgeAmberText: Color get() = ColorPalette.BadgeAmberText
val ColorScheme.noticeBg: Color get() = ColorPalette.NoticeBg
val ColorScheme.secondaryButtonBg: Color get() = ColorPalette.SecondaryButtonBg
val ColorScheme.textPrimary: Color get() = ColorPalette.TextPrimary
val ColorScheme.textMuted: Color get() = ColorPalette.TextMuted
val ColorScheme.cardBg: Color get() = ColorPalette.CardBg
val ColorScheme.teamAvatarBg: Color get() = ColorPalette.TeamAvatarBg
val ColorScheme.inputBorder: Color get() = ColorPalette.InputBorder
val ColorScheme.chipActiveBg: Color get() = ColorPalette.ChipActiveBg
val ColorScheme.chipActiveText: Color get() = ColorPalette.ChipActiveText
val ColorScheme.chipInactiveBg: Color get() = ColorPalette.ChipInactiveBg
val ColorScheme.chipInactiveText: Color get() = ColorPalette.ChipInactiveText
