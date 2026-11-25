@file:Suppress("MagicNumber")

package com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.igorwojda.showcase.feature.base.presentation.compose.theme.primitive.ColorPalette

/** Material 3 ColorScheme and semantic tokens mapped from Prototype v2 (docs/prototype/index.html). */
object ArenaColors {
    val PitchGreen = ColorPalette.PitchGreen
    val Primary = ColorPalette.Primary
    val PrimaryDim = ColorPalette.PrimaryDim
    val PrimaryContainer = ColorPalette.PrimaryContainer
    val OnPrimary = ColorPalette.OnPrimary
    val OnPrimaryContainer = ColorPalette.OnPrimaryContainer
    val TextHeading = ColorPalette.TextHeading
    val TextPrimary = ColorPalette.TextPrimary
    val TextMuted = ColorPalette.TextMuted
    val Background = ColorPalette.Background
    val CardBg = ColorPalette.CardBg
    val SurfaceContainer = ColorPalette.SurfaceContainer
    val SurfaceContainerLow = ColorPalette.SurfaceContainerLow
    val SurfaceContainerHigh = ColorPalette.SurfaceContainerHigh
    val SurfaceContainerHighest = ColorPalette.SurfaceContainerHighest
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
    val Success = ColorPalette.Success
    val SuccessBg = ColorPalette.SuccessBg
    val SuccessBorder = ColorPalette.SuccessBorder
    val Warning = ColorPalette.Warning
    val WarningBg = ColorPalette.WarningBg
    val WarningBorder = ColorPalette.WarningBorder
    val Error = ColorPalette.Error
    val ErrorBright = ColorPalette.ErrorBright
    val ErrorBg = ColorPalette.ErrorBg
    val ErrorBorder = ColorPalette.ErrorBorder
    val Info = ColorPalette.Info
    val InfoBg = ColorPalette.InfoBg
    val InfoBorder = ColorPalette.InfoBorder
    val FacebookBlue = ColorPalette.FacebookBlue
    val NoticeBg = ColorPalette.NoticeBg
    val NavActiveBg = ColorPalette.NavActiveBg
    val TeamAvatarBg = ColorPalette.TeamAvatarBg
    val InputBorder = ColorPalette.InputBorder
    val BubbleIncoming = ColorPalette.BubbleIncoming
    val BubbleMine = ColorPalette.BubbleMine

    val Light =
        lightColorScheme(
            primary = ColorPalette.Primary,
            onPrimary = ColorPalette.OnPrimary,
            primaryContainer = ColorPalette.PrimaryContainer,
            onPrimaryContainer = ColorPalette.OnPrimaryContainer,
            inversePrimary = ColorPalette.GreenLight,
            secondary = ColorPalette.PitchGreen,
            onSecondary = ColorPalette.White,
            secondaryContainer = ColorPalette.NavActiveBg,
            onSecondaryContainer = ColorPalette.Primary,
            tertiary = ColorPalette.Warning,
            onTertiary = ColorPalette.White,
            tertiaryContainer = ColorPalette.WarningBg,
            onTertiaryContainer = ColorPalette.Warning,
            background = ColorPalette.Background,
            onBackground = ColorPalette.TextPrimary,
            surface = ColorPalette.Background,
            onSurface = ColorPalette.TextPrimary,
            surfaceVariant = ColorPalette.SurfaceContainer,
            onSurfaceVariant = ColorPalette.TextMuted,
            surfaceContainerLowest = ColorPalette.CardBg,
            surfaceContainerLow = ColorPalette.SurfaceContainerLow,
            surfaceContainer = ColorPalette.SurfaceContainer,
            surfaceContainerHigh = ColorPalette.SurfaceContainerHigh,
            surfaceContainerHighest = ColorPalette.SurfaceContainerHighest,
            surfaceBright = ColorPalette.White,
            surfaceDim = ColorPalette.BorderSubtle,
            outline = ColorPalette.Border,
            outlineVariant = ColorPalette.BorderSubtle,
            error = ColorPalette.Error,
            onError = ColorPalette.White,
            errorContainer = ColorPalette.ErrorBg,
            onErrorContainer = ColorPalette.ErrorBright,
            scrim = ColorPalette.Black,
        )

    val Dark =
        darkColorScheme(
            primary = Color(0xFF72C482),
            onPrimary = Color(0xFF0A3217),
            primaryContainer = Color(0xFF14532D),
            onPrimaryContainer = Color(0xFFBDEFBF),
            inversePrimary = Color(0xFF3D6943),
            secondary = Color(0xFFB5DDBE),
            onSecondary = Color(0xFF172E27),
            secondaryContainer = Color(0xFF284438),
            onSecondaryContainer = Color(0xFFEAF0E6),
            tertiary = Color(0xFFFFD480),
            onTertiary = Color(0xFF452B00),
            tertiaryContainer = Color(0xFF6B4A08),
            onTertiaryContainer = Color(0xFFFEF3C7),
            background = Color(0xFF111815),
            onBackground = Color(0xFFF8FAF3),
            surface = Color(0xFF111815),
            onSurface = Color(0xFFF8FAF3),
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
