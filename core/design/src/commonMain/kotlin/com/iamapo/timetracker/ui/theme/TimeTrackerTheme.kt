package com.iamapo.timetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** Dayflow typography: a compact sans-serif hierarchy across all screens. */
private val DayflowTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.SansSerif),
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.SansSerif),
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.SansSerif),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.SansSerif),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
        headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.SansSerif),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.SansSerif),
        titleSmall = base.titleSmall.copy(fontFamily = FontFamily.SansSerif),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        bodySmall = base.bodySmall.copy(fontFamily = FontFamily.SansSerif),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif),
        labelMedium = base.labelMedium.copy(fontFamily = FontFamily.SansSerif),
        labelSmall = base.labelSmall.copy(fontFamily = FontFamily.SansSerif)
    )
}

/** Monospace family for eyebrow labels, timestamps and stamped tags — the typewriter ledger entries. */
val LedgerMonospace = FontFamily.Monospace

object TimeTrackerTheme {
    @Composable
    operator fun invoke(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = AppColors.Green,
                secondary = AppColors.Blue,
                tertiary = AppColors.Amber,
                background = AppColors.Background,
                surface = AppColors.Panel,
                surfaceVariant = AppColors.PanelRaised,
                outline = AppColors.Line,
                onPrimary = AppColors.Ink,
                onSecondary = AppColors.Ink,
                onBackground = AppColors.Ink,
                onSurface = AppColors.Ink,
                error = AppColors.Rose,
                scrim = Color.Black
            ),
            typography = DayflowTypography,
            content = content
        )
    }
}
