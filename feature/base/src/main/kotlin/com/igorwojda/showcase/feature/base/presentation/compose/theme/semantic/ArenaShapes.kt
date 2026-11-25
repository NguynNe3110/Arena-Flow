package com.igorwojda.showcase.feature.base.presentation.compose.theme.semantic

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.igorwojda.showcase.feature.base.presentation.compose.theme.primitive.ShapeTokens

/** Semantic shapes aligned with Prototype v2. */
object ArenaShapes {
    val Badge = RoundedCornerShape(ShapeTokens.RadiusBadge)
    val Input = RoundedCornerShape(ShapeTokens.RadiusInput)
    val Button = RoundedCornerShape(ShapeTokens.RadiusButton)
    val Notice = RoundedCornerShape(ShapeTokens.RadiusNotice)
    val Chip = RoundedCornerShape(ShapeTokens.RadiusChip)
    val Card = RoundedCornerShape(ShapeTokens.RadiusCard)
    val Sheet = RoundedCornerShape(topStart = ShapeTokens.RadiusSheet, topEnd = ShapeTokens.RadiusSheet)
    val Avatar = CircleShape

    val MaterialShapes =
        Shapes(
            extraSmall = Badge,
            small = Input,
            medium = Button,
            large = Card,
            extraLarge = Card,
        )
}
