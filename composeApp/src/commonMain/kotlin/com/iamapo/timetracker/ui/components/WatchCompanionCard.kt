package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iamapo.timetracker.ui.theme.AppColors

object WatchCompanionCard {
    @Composable
    operator fun invoke(
        state: String,
        remaining: String,
        caption: String,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = AppColors.Background,
            border = BorderStroke(1.dp, AppColors.Line),
            shape = RoundedCornerShape(44.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "APPLE WATCH",
                    color = AppColors.Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = AppColors.Green.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = state,
                        color = AppColors.Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Text(
                    text = remaining,
                    color = AppColors.Ink,
                    fontSize = 52.sp,
                    lineHeight = 54.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = caption,
                    color = AppColors.Muted,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    WatchActionButton("II")
                    WatchActionButton("■")
                }
            }
        }
    }
}
