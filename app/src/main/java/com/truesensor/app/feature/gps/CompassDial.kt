package com.truesensor.app.feature.gps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun CompassDial(headingDegrees: Float, modifier: Modifier = Modifier) {
    val needleColor = MaterialTheme.colorScheme.primary
    val ringColor = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val radius = size.minDimension / 2f
            drawCircle(color = ringColor, radius = radius, style = Stroke(width = 2.dp.toPx()))
            rotate(degrees = headingDegrees, pivot = center) {
                drawLine(
                    color = needleColor,
                    start = center,
                    end = Offset(center.x, center.y - radius * 0.82f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Text(
            text = "N",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
        )
        Text(
            text = "S",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
        )
        Text(
            text = "W",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
        )
        Text(
            text = "E",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
        )
    }
}
