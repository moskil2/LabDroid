package com.labdroid.app.feature.gps

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private val NeedleNorthColor = Color(0xFFD32F2F)

@Composable
fun CompassDial(headingDegrees: Float, modifier: Modifier = Modifier) {
    val dialColor = MaterialTheme.colorScheme.surfaceVariant
    val ringColor = MaterialTheme.colorScheme.outline
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val needleSouthColor = MaterialTheme.colorScheme.onSurfaceVariant
    val pivotColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(260.dp)) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val faceRadius = radius * 0.86f

            drawCircle(color = dialColor, radius = faceRadius, center = center)
            drawCircle(color = ringColor, radius = faceRadius, center = center, style = Stroke(width = 2.dp.toPx()))
            drawCircle(color = ringColor, radius = faceRadius * 0.7f, center = center, style = Stroke(width = 1.dp.toPx()))

            for (degree in 0 until 360 step 15) {
                val isCardinal = degree % 90 == 0
                val isIntercardinal = degree % 45 == 0
                val tickLength = when {
                    isCardinal -> faceRadius * 0.16f
                    isIntercardinal -> faceRadius * 0.10f
                    else -> faceRadius * 0.06f
                }
                val angleRad = Math.toRadians(degree.toDouble())
                val dirX = sin(angleRad).toFloat()
                val dirY = -cos(angleRad).toFloat()
                val outer = Offset(center.x + faceRadius * dirX, center.y + faceRadius * dirY)
                val inner = Offset(
                    center.x + (faceRadius - tickLength) * dirX,
                    center.y + (faceRadius - tickLength) * dirY,
                )
                drawLine(
                    color = tickColor,
                    start = inner,
                    end = outer,
                    strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx(),
                )
            }

            rotate(degrees = headingDegrees, pivot = center) {
                val northPath = Path().apply {
                    moveTo(center.x, center.y - faceRadius * 0.72f)
                    lineTo(center.x - faceRadius * 0.08f, center.y)
                    lineTo(center.x + faceRadius * 0.08f, center.y)
                    close()
                }
                drawPath(path = northPath, color = NeedleNorthColor)

                val southPath = Path().apply {
                    moveTo(center.x, center.y + faceRadius * 0.58f)
                    lineTo(center.x - faceRadius * 0.07f, center.y)
                    lineTo(center.x + faceRadius * 0.07f, center.y)
                    close()
                }
                drawPath(path = southPath, color = needleSouthColor)
            }

            drawCircle(color = pivotColor, radius = 5.dp.toPx(), center = center)
            drawCircle(color = dialColor, radius = 2.dp.toPx(), center = center)
        }
        Text(
            text = "N",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeedleNorthColor,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp),
        )
        Text(
            text = "S",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
        )
        Text(
            text = "W",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp),
        )
        Text(
            text = "E",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp),
        )
    }
}
