package com.iamapo.timetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

object TimeTrackerTheme {
    @Composable
    operator fun invoke(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = AppColors.Blue,
                secondary = AppColors.Cyan,
                tertiary = AppColors.Green,
                background = AppColors.Background,
                surface = AppColors.Panel,
                onPrimary = AppColors.Ink,
                onSecondary = AppColors.Ink,
                onBackground = AppColors.Ink,
                onSurface = AppColors.Ink
            ),
            typography = Typography(),
            content = content
        )
    }
}
