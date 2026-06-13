package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// ─── Cairo Local Font Family ──────────────────────────────────────────────────
// Using bundled TTF files — works offline, no Google Fonts needed
val CairoFontFamily = FontFamily(
    Font(R.font.cairo_extralight, FontWeight.ExtraLight),
    Font(R.font.cairo_light,      FontWeight.Light),
    Font(R.font.cairo_regular,    FontWeight.Normal),
    Font(R.font.cairo_medium,     FontWeight.Medium),
    Font(R.font.cairo_semibold,   FontWeight.SemiBold),
    Font(R.font.cairo_bold,       FontWeight.Bold),
    Font(R.font.cairo_extrabold,  FontWeight.ExtraBold),
    Font(R.font.cairo_black,      FontWeight.Black),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.03).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.02).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 30.sp, letterSpacing = (-0.01).sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 17.sp, lineHeight = 26.sp, letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    )
)
