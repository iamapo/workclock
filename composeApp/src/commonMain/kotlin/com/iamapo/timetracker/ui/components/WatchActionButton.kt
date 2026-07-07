package com.iamapo.timetracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.ui.theme.AppColors

object WatchActionButton {
    @Composable
    operator fun invoke(label: String, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.size(52.dp),
            color = AppColors.Soft,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = AppColors.Ink,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
