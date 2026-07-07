package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.state.TimeTrackerUiState
import com.iamapo.timetracker.ui.theme.AppColors

object StatusCard {
    @Composable
    operator fun invoke(
        state: TimeTrackerUiState,
        onPrimaryAction: () -> Unit,
        onSecondaryAction: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Panel,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape),
                        color = AppColors.Green,
                        content = {}
                    )
                    Text(
                        text = state.statusLabel,
                        color = AppColors.Ink,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = state.workedTime,
                    color = AppColors.Ink,
                    fontSize = 54.sp,
                    lineHeight = 56.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Noch ca. ${state.remainingTime} bis zum Ziel",
                    color = AppColors.Muted,
                    fontWeight = FontWeight.SemiBold
                )
                EndTimeCard(state.endTime, state.breakRequirementLabel)
                TimeProgressBar(state.progress)
                PrimaryActionsRow(
                    primaryLabel = state.primaryActionLabel,
                    secondaryLabel = state.secondaryActionLabel,
                    onPrimaryAction = onPrimaryAction,
                    onSecondaryAction = onSecondaryAction
                )
            }
        }
    }
}
