package com.fridgetracker.app

import androidx.compose.ui.graphics.Color

/**
 * Visual identity for a category — background color, text color, and a
 * short monogram shown on the card's image area when no photo exists.
 * Deliberately not an icon library: keeps this dependency-free and
 * guaranteed to compile, while still giving each category a distinct look.
 */
data class CategoryVisual(
    val background: Color,
    val onBackground: Color,
    val monogram: String
)

private val categoryVisuals = mapOf(
    "dairy" to CategoryVisual(Color(0xFFE6F1FB), Color(0xFF0C447C), "Da"),
    "produce" to CategoryVisual(Color(0xFFEAF3DE), Color(0xFF27500A), "Pr"),
    "meat" to CategoryVisual(Color(0xFFFAECE7), Color(0xFF712B13), "Me"),
    "pantry" to CategoryVisual(Color(0xFFFAEEDA), Color(0xFF633806), "Pa"),
    "frozen" to CategoryVisual(Color(0xFFEEEDFE), Color(0xFF3C3489), "Fr"),
    "bakery" to CategoryVisual(Color(0xFFFBEAF0), Color(0xFF72243E), "Ba"),
)

private val defaultVisual = CategoryVisual(Color(0xFFF1EFE8), Color(0xFF444441), "?")

fun visualFor(category: String?): CategoryVisual {
    val key = category?.trim()?.lowercase()
    return categoryVisuals[key] ?: defaultVisual.copy(
        monogram = key?.take(2)?.replaceFirstChar { it.uppercase() } ?: "?"
    )
}
