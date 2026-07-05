package com.truesensor.app.feature.sensors.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.truesensor.app.core.designsystem.component.InfoCard
import com.truesensor.app.core.designsystem.component.SectionCard
import com.truesensor.app.data.sensors.SensorDelayOption
import com.truesensor.app.data.sensors.SensorInfo
import com.truesensor.app.data.sensors.unitFor

@Composable
fun SensorDetailScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: SensorDetailViewModel = hiltViewModel(),
) {
    val sensorInfo = viewModel.sensorInfo

    if (sensorInfo == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text("Sensor not available on this device")
        }
        return
    }

    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { HeroCard(sensorInfo, uiState) }
        item { ChartCard(uiState.history) }
        item { DelaySelector(uiState.delayOption, onSelect = viewModel::selectDelay) }
        item { StatsGrid(uiState) }
        item {
            ActionsRow(
                isPaused = uiState.isPaused,
                onPause = viewModel::togglePause,
                onReset = viewModel::reset,
            )
        }
        item { MetadataCard(sensorInfo) }
    }
}

@Composable
private fun HeroCard(sensorInfo: SensorInfo, uiState: SensorDetailUiState) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = sensorInfo.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = uiState.latestValue?.let { "%.2f".format(it) } ?: "—",
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                val unit = unitFor(sensorInfo.type)
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Text(
                text = if (uiState.isPaused) "paused" else "updating live",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun ChartCard(history: List<Float>) {
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "live chart · last ${history.size} samples",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (history.size >= 2) {
                val modelProducer = remember { CartesianChartModelProducer() }
                LaunchedEffect(history) {
                    modelProducer.runTransaction {
                        lineSeries { series(history) }
                    }
                }
                val dataMin = history.min()
                val dataMax = history.max()
                val padding = ((dataMax - dataMin).takeIf { it > 0f } ?: (kotlin.math.abs(dataMax) * 0.1f + 1f)) * 0.15f
                val rangeProvider = remember(dataMin, dataMax) {
                    CartesianLayerRangeProvider.fixed(
                        minY = (dataMin - padding).toDouble(),
                        maxY = (dataMax + padding).toDouble(),
                    )
                }
                ProvideVicoTheme(rememberM3VicoTheme()) {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(rangeProvider = rangeProvider),
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis = HorizontalAxis.rememberBottom(),
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Collecting samples…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DelaySelector(selected: SensorDelayOption, onSelect: (SensorDelayOption) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SensorDelayOption.entries.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selected == option,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, SensorDelayOption.entries.size),
            ) {
                Text(option.label)
            }
        }
    }
}

@Composable
private fun StatsGrid(uiState: SensorDetailUiState) {
    val stats = listOf(
        "Minimum" to uiState.minValue,
        "Maximum" to uiState.maxValue,
        "Average" to uiState.average,
        "Std. deviation" to uiState.stdDev,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stats.chunked(2).forEach { rowPair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowPair.forEach { (label, value) ->
                    InfoCard(
                        label = label,
                        value = value?.let { "%.3f".format(it) } ?: "—",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionsRow(isPaused: Boolean, onPause: () -> Unit, onReset: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        FilledTonalButton(onClick = onPause, modifier = Modifier.weight(1f)) {
            Text(if (isPaused) "Resume" else "Pause")
        }
        FilledTonalButton(onClick = onReset, modifier = Modifier.weight(1f)) {
            Text("Reset")
        }
        Button(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
            Text("Export")
        }
    }
}

@Composable
private fun MetadataCard(sensorInfo: SensorInfo) {
    SectionCard(
        title = "Metadata",
        rows = listOf(
            "Vendor" to sensorInfo.vendor,
            "Version" to sensorInfo.version.toString(),
            "Resolution" to sensorInfo.resolution.toString(),
            "Max range" to sensorInfo.maximumRange.toString(),
            "Power" to "${sensorInfo.power} mA",
            "Min delay" to "${sensorInfo.minDelayUs} µs",
            "FIFO" to sensorInfo.fifoMaxEventCount.toString(),
            "Wake-up" to if (sensorInfo.isWakeUpSensor) "Yes" else "No",
            "Reporting mode" to sensorInfo.reportingMode,
        ),
    )
}
