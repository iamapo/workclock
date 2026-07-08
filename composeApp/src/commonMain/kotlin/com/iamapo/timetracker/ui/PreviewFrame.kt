package com.iamapo.timetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iamapo.timetracker.ui.theme.AppColors

@Composable
internal fun PreviewFrame(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .width(420.dp)
            .background(AppColors.Background)
            .padding(20.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
