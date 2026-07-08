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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.ui.PreviewFrame
import com.iamapo.timetracker.ui.theme.AppColors
import com.iamapo.timetracker.ui.theme.TimeTrackerTheme

object WatchActionButton {
    @Composable
    operator fun invoke(label: String, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.size(38.dp),
            color = AppColors.SoftMuted,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = AppColors.Muted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
private fun WatchActionButtonPreview() {
    TimeTrackerTheme {
        PreviewFrame {
            WatchActionButton("II")
        }
    }
}
