package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme
import org.jetbrains.compose.resources.stringResource
import workclock.composeapp.generated.resources.*

object NotesPanel {
    @Composable
    operator fun invoke(modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(AppDimensions.size1, AppColors.Line),
            shape = RoundedCornerShape(AppDimensions.size16)
        ) {
            Column(modifier = Modifier.padding(AppDimensions.size18)) {
                Text(
                    text = stringResource(Res.string.interaction_idea),
                    color = AppColors.Subtle,
                    fontSize = AppFontSizes.size10,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = AppFontSizes.size1
                )
                Text(
                    text = stringResource(Res.string.interaction_description),
                    color = AppColors.Muted,
                    fontSize = AppFontSizes.size14,
                    lineHeight = AppFontSizes.size20,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = AppDimensions.size12)
                )
            }
        }
    }
}

@Preview
@Composable
private fun NotesPanelPreview() {
    TimeTrackerTheme {
        NotesPanel()
    }
}
