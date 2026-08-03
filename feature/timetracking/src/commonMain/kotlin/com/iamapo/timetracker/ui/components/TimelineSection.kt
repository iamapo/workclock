package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.presentation.TimeTrackerPreviewData
import com.iamapo.timetracker.presentation.state.TimelineItemUiModel
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.LedgerMonospace
import com.iamapo.timetracker.ui.theme.LedgerShapes
import com.iamapo.timetracker.ui.theme.ledgerMargin
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import com.iamapo.timetracker.ui.ComposePreviewContext
import org.jetbrains.compose.resources.stringResource
import com.iamapo.timetracker.resources.Res
import com.iamapo.timetracker.resources.timeline

object TimelineSection {
    @Composable
    operator fun invoke(items: List<TimelineItemUiModel>, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.fillMaxWidth().ledgerMargin(),
            color = AppColors.Panel,
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = LedgerShapes.Card
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.size18),
                verticalArrangement = Arrangement.spacedBy(AppDimensions.size14)
            ) {
                Text(
                    text = stringResource(Res.string.timeline),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Black,
                    fontFamily = LedgerMonospace,
                    letterSpacing = AppFontSizes.size0_2
                )
                items.forEach { item ->
                    TimelineRow(item)
                }
            }
        }
    }
}

@Preview
@Composable
private fun TimelineSectionPreview() {
    ComposePreviewContext()
    TimeTrackerTheme {
        TimelineSection(TimeTrackerPreviewData.uiState().timeline)
    }
}
