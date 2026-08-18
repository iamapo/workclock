package com.iamapo.timetracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** Shared Dayflow corners. The legacy name is kept to avoid churn in feature modules. */
object LedgerShapes {
    val Card = RoundedCornerShape(AppDimensions.size16)
    val CardSmall = RoundedCornerShape(AppDimensions.size11)
}

/**
 * Faint horizontal rules evenly spaced behind content, like lined ledger paper.
 * Drawn once per recomposition of the background, independent of scroll position.
 */
fun Modifier.ledgerRuledPaper(
    lineColor: Color = AppColors.Line,
    lineSpacing: Dp = AppDimensions.size28
): Modifier = drawBehind {
    val step = lineSpacing.toPx()
    if (step <= 0f) return@drawBehind
    var y = step
    while (y < size.height) {
        drawLine(
            color = lineColor.copy(alpha = 0.9f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

/** A red margin rule along the left edge, the mark of an entry in the ledger. */
fun Modifier.ledgerMargin(
    color: Color = AppColors.Margin,
    width: Dp = AppDimensions.size3
): Modifier = drawWithContent {
    drawContent()
    drawRect(color = color, size = Size(width.toPx(), size.height))
}
