package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object TopBarSection {
    @Composable
    operator fun invoke(dateLabel: String, title: String, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateLabel.uppercase(),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size11,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = AppFontSizes.size0_4
                )
                Text(
                    text = title,
                    color = AppColors.Ink,
                    fontSize = AppFontSizes.size30,
                    lineHeight = AppFontSizes.size32,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Preview
@Composable
private fun TopBarSectionPreview() {
    TimeTrackerTheme {
        TopBarSection(
            dateLabel = TimeTrackerPreviewData.uiState.dateLabel,
            title = TimeTrackerPreviewData.uiState.title
        )
    }
}
