package com.iamapo.timetracker.ui.theme

import androidx.compose.ui.graphics.Color

/** Shared Dayflow palette used by every WorkClock surface. */
object AppColors {
    val Background = Color(0xFFFFFAF2)
    val Panel = Color(0xFFFFFFFF)
    val PanelRaised = Color(0xFFFFF5E7)
    val Soft = Color(0xFFF7EBDD)
    val SoftMuted = Color(0xFFECE1D3)
    val Line = Color(0xFFEADFCC)
    val LineStrong = Color(0xFFD8C8B2)

    // Four ink roles on Background, one navy ramp. Every piece of text picks one of
    // these instead of fading the ink with an alpha, so the same role reads the same
    // on every screen. Contrast on Background: Ink 14.5:1, Muted 8.3:1, Subtle 5.1:1.
    // Disabled is below 4.5:1 by intent and only marks what cannot be read or used,
    // never content.
    val Ink = Color(0xFF102544)
    val Muted = Color(0xFF3C4C62)
    val Subtle = Color(0xFF5E6E82)
    val Disabled = Color(0xFFA8B0BC)

    val Paper = Color(0xFFFFFFFF)
    val Margin = Color(0xFFFF6B8F)
    val Coral = Color(0xFFFF5D50)
    val Success = Color(0xFF3F7B58)
    val Sand = Color(0xFFF4D7A4)
    val CalendarLine = Color(0xFFE2DED6)
    val Lemon = Color(0xFFFFD84D)
    val Blue = Color(0xFF58A7FF)
    val Green = Color(0xFF66D8B7)
    val Rose = Color(0xFFFF6B8F)
    val Purple = Color(0xFF9C70FF)
    val Cyan = Color(0xFF28C7D8)
    val Amber = Lemon
}
