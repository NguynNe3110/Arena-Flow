package com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import com.igorwojda.showcase.feature.base.presentation.compose.theme.primitive.FontTokens

/** Typography roles mapped to docs/prototype/index.html. */
val ArenaTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Bold,
                fontSize = FontTokens.Size36,
                lineHeight = FontTokens.Size40,
                letterSpacing = FontTokens.TrackingHeading,
            ),
        displayMedium =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Bold,
                fontSize = FontTokens.Size30,
                lineHeight = FontTokens.Size36,
                letterSpacing = FontTokens.TrackingHeading,
            ),
        displaySmall =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Bold,
                fontSize = FontTokens.Size26,
                lineHeight = FontTokens.Size32,
                letterSpacing = FontTokens.TrackingHeading,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Bold,
                fontSize = FontTokens.Size24,
                lineHeight = FontTokens.Size32,
                letterSpacing = FontTokens.TrackingHeading,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Bold,
                fontSize = FontTokens.Size23,
                lineHeight = FontTokens.Size28,
                letterSpacing = FontTokens.TrackingHeading,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Bold,
                fontSize = FontTokens.Size19,
                lineHeight = FontTokens.Size24,
                letterSpacing = FontTokens.TrackingHeading,
            ),
        titleLarge =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.SemiBold,
                fontSize = FontTokens.Size18,
                lineHeight = FontTokens.Size24,
                letterSpacing = FontTokens.TrackingNone,
            ),
        titleMedium =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.SemiBold,
                fontSize = FontTokens.Size16,
                lineHeight = FontTokens.Size22,
                letterSpacing = FontTokens.TrackingNone,
            ),
        titleSmall =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.SemiBold,
                fontSize = FontTokens.Size14,
                lineHeight = FontTokens.Size20,
                letterSpacing = FontTokens.TrackingNone,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Regular,
                fontSize = FontTokens.Size16,
                lineHeight = FontTokens.Size24,
                letterSpacing = FontTokens.TrackingNone,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Regular,
                fontSize = FontTokens.Size14,
                lineHeight = FontTokens.Size20,
                letterSpacing = FontTokens.TrackingNone,
            ),
        bodySmall =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Regular,
                fontSize = FontTokens.Size13,
                lineHeight = FontTokens.Size18,
                letterSpacing = FontTokens.TrackingNone,
            ),
        labelLarge =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.SemiBold,
                fontSize = FontTokens.Size14,
                lineHeight = FontTokens.Size20,
                letterSpacing = FontTokens.TrackingNone,
            ),
        labelMedium =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Bold,
                fontSize = FontTokens.Size12,
                lineHeight = FontTokens.Size16,
                letterSpacing = FontTokens.TrackingNone,
            ),
        labelSmall =
            TextStyle(
                fontFamily = FontTokens.Family,
                fontWeight = FontTokens.Medium,
                fontSize = FontTokens.Size11,
                lineHeight = FontTokens.Size14,
                letterSpacing = FontTokens.TrackingNone,
            ),
    )
