package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.Res
import workclock.composeapp.generated.resources.nav_settings
import workclock.composeapp.generated.resources.nav_today
import workclock.composeapp.generated.resources.nav_week

enum class MainTab(val icon: String) {
    Today("⌂"), Calendar("▦"), Settings("⚙")
}

object BottomNavigationBar {
    @Composable
    operator fun invoke(
        selectedTab: MainTab,
        onSelectTab: (MainTab) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Background)
                .padding(horizontal = AppDimensions.size20, vertical = AppDimensions.size10)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.Soft.copy(alpha = 0.78f),
                border = BorderStroke(AppDimensions.size1, AppColors.Line.copy(alpha = 0.72f)),
                shape = RoundedCornerShape(AppDimensions.size18)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimensions.size8),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.size8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTab.entries.forEach { tab ->
                        BottomNavigationItem(
                            tab = tab,
                            selected = selectedTab == tab,
                            onClick = { onSelectTab(tab) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomNavigationItem(
        tab: MainTab,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier
                .heightIn(min = AppDimensions.size50)
                .clickable(onClick = onClick),
            color = if (selected) AppColors.Panel else Color.Transparent,
            shape = RoundedCornerShape(AppDimensions.size12)
        ) {
            Column(
                modifier = Modifier.padding(vertical = AppDimensions.size7),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tab.icon,
                    color = if (selected) AppColors.Ink else AppColors.Subtle,
                    fontSize = AppFontSizes.size16,
                    lineHeight = AppFontSizes.size17,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(when (tab) {
                        MainTab.Today -> Res.string.nav_today
                        MainTab.Calendar -> Res.string.nav_week
                        MainTab.Settings -> Res.string.nav_settings
                    }),
                    color = if (selected) AppColors.Ink else AppColors.Muted,
                    fontSize = AppFontSizes.size11,
                    lineHeight = AppFontSizes.size13,
                    fontWeight = FontWeight.Black,
                    letterSpacing = AppFontSizes.size0
                )
            }
        }
    }
}

@Preview
@Composable
private fun BottomNavigationBarPreview() {
    TimeTrackerTheme {
        BottomNavigationBar(
            selectedTab = MainTab.Today,
            onSelectTab = {}
        )
    }
}
