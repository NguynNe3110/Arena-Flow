package com.igorwojda.showcase.feature.base.presentation.compose.theme.primitive

import androidx.compose.ui.graphics.Color

/**
 * Raw design tokens from docs/prototype/index.html and docs/android-ui/IMPLEMENTATION_PLAN.md:
 * - Primary: #146B46, onPrimary: #FFFFFF
 * - Text Primary: #172E27, Text Muted/Secondary: #52675D
 * - Background: #FAFBF7, Card: #FFFFFF
 * - Border / Secondary button: #DCE3D8 / #E7EEE6
 * - Chip active: bg #172E27, text #FFFFFF
 * - Badge green: bg #DFEFDF, text #155B39
 * - Badge amber: bg #FFF0CE, text #6B4A08
 * - Notice / Nav active: #F1F0DF / #E5EEE1
 * - Radii: Button 12dp, Card 14dp, Input 10dp, Badge 7dp, Avatar Circle.
 */
object ColorPalette {
    // Primary Brand
    val Primary = Color(0xFF146B46)
    val OnPrimary = Color(0xFFFFFFFF)
    val GreenLight = Color(0xFFDFEFDF)
    val GreenDark = Color(0xFF155B39)
    val GreenDeep = Color(0xFF203D31)

    // Text & Neutrals
    val TextPrimary = Color(0xFF172E27)
    val TextMuted = Color(0xFF52675D)
    val Background = Color(0xFFFAFBF7)
    val CardBg = Color(0xFFFFFFFF)
    val Border = Color(0xFFDCE3D8)
    val BorderSubtle = Color(0xFFE2E7DE)
    val InputBorder = Color(0xFFB2C2B5)

    // Actions & Interactive
    val SecondaryButtonBg = Color(0xFFE7EEE6)
    val ChipActiveBg = Color(0xFF172E27)
    val ChipActiveText = Color(0xFFFFFFFF)
    val ChipInactiveBg = Color(0xFFE7EEE6)
    val ChipInactiveText = Color(0xFF172E27)

    // Badges
    val BadgeGreenBg = Color(0xFFDFEFDF)
    val BadgeGreenText = Color(0xFF155B39)
    val BadgeAmberBg = Color(0xFFFFF0CE)
    val BadgeAmberText = Color(0xFF6B4A08)

    // Callouts & Navigation
    val NoticeBg = Color(0xFFF1F0DF)
    val NavActiveBg = Color(0xFFE5EEE1)
    val TeamAvatarBg = Color(0xFF203D31)

    // Chat Bubbles
    val BubbleIncoming = Color(0xFFE4EBE2)
    val BubbleMine = Color(0xFFD6ECDB)

    // Highlights & Errors
    val Selection = Color(0xFFB5DDBE)
    val Error = Color(0xFFA73B21)
    val ErrorContainer = Color(0xFFFFB4A0)
    val OnErrorContainer = Color(0xFF6E1400)

    // Legacy Stitch compatibility aliases
    val Green20 = Color(0xFF155B39)
    val Green30 = Color(0xFF146B46)
    val Green35 = Color(0xFF146B46)
    val Green40 = Color(0xFF146B46)
    val Green80 = Color(0xFF68DC99)
    val Green90 = Color(0xFFDFEFDF)
    val Green95 = Color(0xFFEAF5EB)
    val Green99 = Color(0xFFFFFFFF)

    val Sage20 = Color(0xFF172E27)
    val Sage30 = Color(0xFF284438)
    val Sage35 = Color(0xFF385549)
    val Sage40 = Color(0xFF52675D)
    val Sage80 = Color(0xFFB5DDBE)
    val Sage90 = Color(0xFFE5EEE1)
    val Sage99 = Color(0xFFFAFBF7)
    val SageDark = Color(0xFF1D2823)

    val Olive20 = Color(0xFF452B00)
    val Olive35 = Color(0xFF5A3C05)
    val Olive40 = Color(0xFF6B4A08)
    val Olive45 = Color(0xFF7E570A)
    val Olive80 = Color(0xFFFFD480)
    val Olive90 = Color(0xFFFFF0CE)
    val Olive99 = Color(0xFFFFFBEF)
    val OliveDark = Color(0xFF2F1D00)

    val Red10 = Color(0xFF410002)
    val Red20 = Color(0xFF6E1400)
    val Red30 = Color(0xFF7D260F)
    val Red40 = Color(0xFFA73B21)
    val Red70 = Color(0xFFFD795A)
    val Red80 = Color(0xFFFFB4A0)
    val Red99 = Color(0xFFFFF7F6)

    val Neutral4 = Color(0xFF0C120F)
    val Neutral6 = Color(0xFF111815)
    val Neutral10 = Color(0xFF18221D)
    val Neutral12 = Color(0xFF1D2823)
    val Neutral17 = Color(0xFF23302A)
    val Neutral20 = Color(0xFF172E27)
    val Neutral24 = Color(0xFF2E3D36)
    val Neutral60 = Color(0xFF52675D)
    val Neutral85 = Color(0xFFDCE3D8)
    val Neutral90 = Color(0xFFE2E7DE)
    val Neutral92 = Color(0xFFE5EEE1)
    val Neutral94 = Color(0xFFE7EEE6)
    val Neutral96 = Color(0xFFF1F5EC)
    val Neutral98 = Color(0xFFFAFBF7)
    val NeutralLight = Color(0xFFFAFBF7)

    val NeutralVariant40 = Color(0xFF52675D)
    val NeutralVariant50 = Color(0xFF757D73)
    val NeutralVariant70 = Color(0xFFDCE3D8)

    // Base
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val Transparent = Color(0x00000000)
}
