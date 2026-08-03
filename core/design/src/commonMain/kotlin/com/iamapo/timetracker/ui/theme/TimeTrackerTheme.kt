package com.iamapo.timetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** Ledger typography: serif body/display text throughout, like entries in a bound book. */
private val LedgerTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.Serif),
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.Serif),
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.Serif),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif),
        headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif),
        titleSmall = base.titleSmall.copy(fontFamily = FontFamily.Serif),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.Serif),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.Serif),
        bodySmall = base.bodySmall.copy(fontFamily = FontFamily.Serif),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.Serif),
        labelMedium = base.labelMedium.copy(fontFamily = FontFamily.Serif),
        labelSmall = base.labelSmall.copy(fontFamily = FontFamily.Serif)
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
            typography = LedgerTypography,
            content = content
        )
    }
}
