package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.state.MetricUiModel
import com.iamapo.timetracker.ui.theme.AppColors

object MetricCard {
    @Composable
    operator fun invoke(metric: MetricUiModel, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp),
            color = AppColors.PanelRaised,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = metric.label.uppercase(),
                    color = AppColors.Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = metric.value,
                    color = if (metric.emphasized) AppColors.Cyan else AppColors.Ink,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = metric.hint,
                    color = AppColors.Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
