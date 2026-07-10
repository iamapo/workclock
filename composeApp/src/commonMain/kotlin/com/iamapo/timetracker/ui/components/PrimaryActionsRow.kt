package com.iamapo.timetracker.ui.components

import com.iamapo.timetracker.ui.theme.AppDimensions
import com.iamapo.timetracker.ui.theme.AppFontSizes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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

object PrimaryActionsRow {
    @Composable
    operator fun invoke(
        primaryLabel: String,
        secondaryLabel: String?,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        modifier: Modifier = Modifier,
        primaryColor: Color = AppColors.Paper,
        primaryContainerColor: Color = AppColors.Night
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimensions.size10)
        ) {
            ActionSurface(
                label = primaryLabel,
                onClick = onPrimaryAction,
                modifier = Modifier
                    .weight(if (secondaryLabel == null) 1f else 1.7f)
                    .heightIn(min = AppDimensions.size52),
                contentColor = primaryColor,
                containerColor = primaryContainerColor,
                borderColor = primaryColor
            )
            if (secondaryLabel != null) {
                ActionSurface(
                    label = secondaryLabel,
                    onClick = onSecondaryAction,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = AppDimensions.size50),
                    contentColor = AppColors.Ink,
                    containerColor = AppColors.Lemon,
                    borderColor = AppColors.Lemon
                )
            }
        }
    }

    @Composable
    private fun ActionSurface(
        label: String,
        onClick: () -> Unit,
        modifier: Modifier,
        contentColor: Color,
        containerColor: Color,
        borderColor: Color
    ) {
        Surface(
            modifier = modifier.clickable(onClick = onClick),
            color = containerColor,
            border = BorderStroke(AppDimensions.size1, borderColor.copy(alpha = 0.28f)),
            shape = RoundedCornerShape(AppDimensions.size14)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = contentColor,
                    fontSize = AppFontSizes.size15,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
private fun PrimaryActionsRowPreview() {
    TimeTrackerTheme {
        PrimaryActionsRow(
            primaryLabel = "Pause starten",
            secondaryLabel = "Tag beenden",
            onPrimaryAction = {},
            onSecondaryAction = {}
        )
    }
}
