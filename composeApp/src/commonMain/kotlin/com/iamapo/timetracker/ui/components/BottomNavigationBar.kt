package com.iamapo.timetracker.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

enum class MainTab(
    val label: String,
    val icon: String
) {
    Today("Heute", "⌂"),
    Calendar("Woche", "▦"),
    Settings("Settings", "⚙")
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
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.Soft.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, AppColors.Line.copy(alpha = 0.72f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                .heightIn(min = 50.dp)
                .clickable(onClick = onClick),
            color = if (selected) AppColors.Panel else Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tab.icon,
                    color = if (selected) AppColors.Ink else AppColors.Subtle,
                    fontSize = 16.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = tab.label,
                    color = if (selected) AppColors.Ink else AppColors.Muted,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp
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
