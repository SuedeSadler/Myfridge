package com.fridgetracker.app

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Font family for the app.
 *
 * TODO swap to real Inter once font files are added:
 * 1. Download Inter (Regular/Medium/Bold) from fonts.google.com/specimen/Inter
 * 2. Save as res/font/inter_regular.ttf, inter_medium.ttf, inter_bold.ttf
 * 3. Replace FontFamily.Default below with:
 *      FontFamily(
 *          Font(R.font.inter_regular, FontWeight.Normal),
 *          Font(R.font.inter_medium, FontWeight.Medium),
 *          Font(R.font.inter_bold, FontWeight.Bold)
 *      )
 * No other code needs to change — every text style below references this
 * single family, so the whole app picks up real Inter in one edit.
 */
val AppFontFamily: FontFamily = FontFamily.Default

/**
 * Type scale per Portal+ spec: never below 14sp, 18sp for body text,
 * 24sp bold for headings. Only Normal/Medium/Bold weights are used.
 */
val AppTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)
