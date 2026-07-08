package com.iamapo.timetracker.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

enum class MainTab(
    val label: String,
    val icon: String
) {
    Today("Heute", "⊙"),
    Calendar("Kalender", "◫"),
    Settings("Einstellungen", "⚙")
}

object BottomNavigationBar {
    @Composable
    operator fun invoke(
        selectedTab: MainTab,
        onSelectTab: (MainTab) -> Unit
    ) {
        NavigationBar(
            containerColor = AppColors.Background.copy(alpha = 0.96f),
            tonalElevation = 0.dp
        ) {
            MainTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelectTab(tab) },
                    icon = {
                        Text(
                            text = tab.icon,
                            color = if (selected) AppColors.Green else AppColors.Subtle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            color = if (selected) AppColors.Green else AppColors.Subtle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.4.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.Green,
                        selectedTextColor = AppColors.Green,
                        indicatorColor = AppColors.Green.copy(alpha = 0.10f),
                        unselectedIconColor = AppColors.Subtle,
                        unselectedTextColor = AppColors.Subtle
                    )
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
