package com.iamapo.timetracker.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.PreviewFrame
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
        primaryColor: Color = AppColors.Green,
        primaryContainerColor: Color = AppColors.Green.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionSurface(
                label = primaryLabel,
                onClick = onPrimaryAction,
                modifier = Modifier
                    .weight(if (secondaryLabel == null) 1f else 1.7f)
                    .heightIn(min = 52.dp),
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
                        .heightIn(min = 50.dp),
                    contentColor = AppColors.Muted,
                    containerColor = AppColors.PanelRaised,
                    borderColor = AppColors.LineStrong
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
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = contentColor,
                    fontSize = 15.sp,
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
        PreviewFrame {
            PrimaryActionsRow(
                primaryLabel = "Pause starten",
                secondaryLabel = "Tag beenden",
                onPrimaryAction = {},
                onSecondaryAction = {}
            )
        }
    }
}
