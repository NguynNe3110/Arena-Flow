package com.igorwojda.showcase.feature.base.presentation.compose.theme.primitive

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.igorwojda.showcase.feature.base.R

/** Bundled Be Vietnam Pro family and the source prototype's type scale. */
internal object FontTokens {
    val Family =
        FontFamily(
            Font(R.font.be_vietnam_pro_thin, FontWeight.Thin),
            Font(R.font.be_vietnam_pro_thin_italic, FontWeight.Thin, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_extra_light, FontWeight.ExtraLight),
            Font(R.font.be_vietnam_pro_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_light, FontWeight.Light),
            Font(R.font.be_vietnam_pro_light_italic, FontWeight.Light, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
            Font(R.font.be_vietnam_pro_italic, FontWeight.Normal, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
            Font(R.font.be_vietnam_pro_medium_italic, FontWeight.Medium, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_semi_bold, FontWeight.SemiBold),
            Font(R.font.be_vietnam_pro_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_bold, FontWeight.Bold),
            Font(R.font.be_vietnam_pro_bold_italic, FontWeight.Bold, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_extra_bold, FontWeight.ExtraBold),
            Font(R.font.be_vietnam_pro_extra_bold_italic, FontWeight.ExtraBold, FontStyle.Italic),
            Font(R.font.be_vietnam_pro_black, FontWeight.Black),
            Font(R.font.be_vietnam_pro_black_italic, FontWeight.Black, FontStyle.Italic),
        )

    val Regular = FontWeight.Normal
    val Medium = FontWeight.Medium
    val SemiBold = FontWeight.SemiBold
    val Bold = FontWeight.Bold

    val Size11 = 11.sp
    val Size12 = 12.sp
    val Size14 = 14.sp
    val Size16 = 16.sp
    val Size18 = 18.sp
    val Size20 = 20.sp
    val Size24 = 24.sp
    val Size26 = 26.sp
    val Size28 = 28.sp
    val Size30 = 30.sp
    val Size32 = 32.sp
    val Size36 = 36.sp
    val Size40 = 40.sp

    val TrackingNone = 0.sp
    val TrackingHeading = (-0.5).sp
}
