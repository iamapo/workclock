package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.LedgerShapes
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.ui.theme.ledgerRuledPaper
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.nav_settings
import com.iamapo.timetracker.resources.nav_today
import com.iamapo.timetracker.resources.nav_week
import com.iamapo.timetracker.ui.theme.AppDimensions.size8

enum class MainTab(val icon: String) {
    Today("⌂"), Calendar("▦"), Settings("⚙")
}

object BottomNavigationBar {
    @Composable
    operator fun invoke(
        selectedTab: MainTab,
        onSelectTab: (MainTab) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth().padding(bottom = size8),
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

    @Composable
    private fun BottomNavigationItem(
        tab: MainTab,
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .heightIn(min = AppDimensions.size40),
            color = if (selected) AppColors.Panel else Color.Transparent,
            shape = LedgerShapes.CardSmall
        ) {
            Column(
                modifier = Modifier.padding(vertical = AppDimensions.size7),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
