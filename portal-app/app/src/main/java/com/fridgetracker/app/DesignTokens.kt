package com.fridgetracker.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Layout constants from the Meta Portal+ panel spec. */
object PortalSpec {
    /** Reserved for the system overlay — nothing interactive goes here. */
    val topSystemPadding = 64.dp
    /** Standard padding on all other edges and between major sections. */
    val screenPadding = 16.dp
    /** Minimum size for any tappable element (hand/air-pointer tracking). */
    val minTouchTarget = 52.dp
}

/** Warm palette matching the approved mockup direction. */
object AppColors {
    val background = Color(0xFFF7F4EE)
    val surface = Color(0xFFFFFFFF)
    val accent = Color(0xFF1E8E5A)
    val onAccent = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF2B2A25)
    val textSecondary = Color(0xFF8A8578)
    val danger = Color(0xFFC4472A)
    val warning = Color(0xFF8A6A2E)
}
