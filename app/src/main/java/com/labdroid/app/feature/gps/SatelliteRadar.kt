package com.labdroid.app.feature.gps

import android.graphics.Paint
import android.location.GnssStatus
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.labdroid.app.R
import com.labdroid.app.data.location.SatelliteInfo
import kotlin.math.cos
import kotlin.math.sin

private val RadarBackground = Color(0xFF02120A)
private val RadarGrid = Color(0xFF1E5B3A)
private val RadarSweep = Color(0xFF00E676)
private val RadarLabel = Color(0xFF6FCF97)

private fun signalColor(cn0DbHz: Float): Color {
    val normalized = ((cn0DbHz - 12f) / (42f - 12f)).coerceIn(0f, 1f)
    return lerp(Color(0xFFE53935), Color(0xFF43A047), normalized)
}

private fun constellationPrefix(constellationType: Int): String = when (constellationType) {
    GnssStatus.CONSTELLATION_GPS -> "G"
    GnssStatus.CONSTELLATION_SBAS -> "S"
    GnssStatus.CONSTELLATION_GLONASS -> "R"
    GnssStatus.CONSTELLATION_QZSS -> "J"
    GnssStatus.CONSTELLATION_BEIDOU -> "C"
    GnssStatus.CONSTELLATION_GALILEO -> "E"
    GnssStatus.CONSTELLATION_IRNSS -> "I"
    else -> "?"
}

private fun satelliteLabel(satellite: SatelliteInfo): String =
    "${constellationPrefix(satellite.constellationType)}${satellite.svid}"

private const val SWEEP_HIGHLIGHT_WINDOW_DEGREES = 14f

private fun angularDiff(a: Float, b: Float): Float {
    val diff = ((a - b + 180f).mod(360f)) - 180f
    return kotlin.math.abs(diff)
}

@Composable
fun SatelliteRadar(satellites: List<SatelliteInfo>, headingDegrees: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar-sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweep-angle",
    )
    val labelPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = RadarBackground),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                listOf(1f, 0.66f, 0.33f).forEach { fraction ->
                    drawCircle(
                        color = RadarGrid,
                        radius = radius * fraction,
                        center = center,
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }

                // Crosshair and satellites are heading-relative: rotate opposite the
                // device heading so the plot stays earth-fixed while the phone turns.
                rotate(degrees = -headingDegrees, pivot = center) {
                    drawLine(
                        RadarGrid,
                        Offset(center.x - radius, center.y),
                        Offset(center.x + radius, center.y),
                        strokeWidth = 1.dp.toPx(),
                    )
                    drawLine(
                        RadarGrid,
                        Offset(center.x, center.y - radius),
                        Offset(center.x, center.y + radius),
                        strokeWidth = 1.dp.toPx(),
                    )

                    val effectiveSweepAzimuth = (sweepAngle + headingDegrees).mod(360f)
                    satellites.forEach { satellite ->
                        val elevationFraction = satellite.elevationDegrees.coerceIn(0f, 90f) / 90f
                        val distance = radius * (1f - elevationFraction)
                        val azimuthRad = Math.toRadians(satellite.azimuthDegrees.toDouble())
                        val dotCenter = Offset(
                            x = center.x + distance * sin(azimuthRad).toFloat(),
                            y = center.y - distance * cos(azimuthRad).toFloat(),
                        )
                        val color = signalColor(satellite.cn0DbHz)
                        val sweepProximity = angularDiff(satellite.azimuthDegrees, effectiveSweepAzimuth)
                        val highlight = (1f - sweepProximity / SWEEP_HIGHLIGHT_WINDOW_DEGREES).coerceIn(0f, 1f)
                        val dotRadiusPx = lerp(5.dp, 9.dp, highlight).toPx()
                        val highlightColor = lerp(color, Color.White, highlight * 0.7f)
                        if (highlight > 0.15f) {
                            drawCircle(
                                color = highlightColor.copy(alpha = highlight * 0.35f),
                                radius = dotRadiusPx * 2.2f,
                                center = dotCenter,
                            )
                        }
                        drawCircle(color = highlightColor, radius = dotRadiusPx, center = dotCenter)
                        if (satellite.usedInFix) {
                            drawCircle(color = highlightColor, radius = 8.dp.toPx(), center = dotCenter, style = Stroke(width = 1.dp.toPx()))
                        }
                    }
                }

                // Decorative scanning sweep: rotates continuously, independent of heading.
                val sweepBrush = Brush.sweepGradient(
                    colors = listOf(Color.Transparent, RadarSweep.copy(alpha = 0.4f)),
                    center = center,
                )
                rotate(degrees = sweepAngle, pivot = center) {
                    drawArc(
                        brush = sweepBrush,
                        startAngle = -140f,
                        sweepAngle = 50f,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                    )
                    drawLine(
                        color = RadarSweep,
                        start = center,
                        end = Offset(center.x, center.y - radius),
                        strokeWidth = 2.dp.toPx(),
                    )
                }

                // Cardinal labels orbit with heading but stay upright and legible.
                labelPaint.color = RadarLabel.toArgb()
                labelPaint.textSize = 12.sp.toPx()
                val labelDistance = radius * 0.9f
                listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f).forEach { (label, baseAngle) ->
                    val angleRad = Math.toRadians((baseAngle - headingDegrees).toDouble())
                    val x = center.x + labelDistance * sin(angleRad).toFloat()
                    val y = center.y - labelDistance * cos(angleRad).toFloat()
                    drawContext.canvas.nativeCanvas.drawText(label, x, y - (labelPaint.ascent() + labelPaint.descent()) / 2, labelPaint)
                }
            }
        }
    }
}

@Composable
fun SatelliteSignalBarChart(satellites: List<SatelliteInfo>, modifier: Modifier = Modifier) {
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.gps_satellite_signal),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (satellites.isEmpty()) {
                Text(
                    text = stringResource(R.string.gps_no_satellites),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val sorted = satellites.sortedByDescending { it.cn0DbHz }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sorted.forEach { satellite ->
                        val fraction = (satellite.cn0DbHz / 45f).coerceIn(0.04f, 1f)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.height(96.dp).width(22.dp),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(signalColor(satellite.cn0DbHz)),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = satelliteLabel(satellite),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.gps_constellation_legend),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
