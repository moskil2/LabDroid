package com.labdroid.app.feature.sensors.detail

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.labdroid.app.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.labdroid.app.core.designsystem.component.ExportMenuButton
import com.labdroid.app.core.designsystem.component.InfoCard
import com.labdroid.app.core.designsystem.component.SectionCard
import com.labdroid.app.data.export.ExportFormat
import com.labdroid.app.data.sensors.SensorDelayOption
import com.labdroid.app.data.sensors.SensorInfo
import com.labdroid.app.data.sensors.unitFor

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
            Text(stringResource(R.string.sensor_detail_unavailable))
        }
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val exportFolderUri by viewModel.exportFolderUri.collectAsState()

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
                sensorName = sensorInfo.name,
                hasSamples = uiState.history.isNotEmpty(),
                onExport = viewModel::exportHistory,
                exportFolderUri = exportFolderUri,
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
                text = stringResource(if (uiState.isPaused) R.string.sensor_detail_paused else R.string.sensor_detail_updating_live),
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
                text = stringResource(R.string.sensor_detail_live_chart, history.size),
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
                        minX = 0.0,
                        maxX = (SensorDetailViewModel.HISTORY_SIZE - 1).toDouble(),
                        minY = (dataMin - padding).toDouble(),
                        maxY = (dataMax + padding).toDouble(),
                    )
                }
                val fixedZoom = Zoom.x((SensorDetailViewModel.HISTORY_SIZE - 1).toDouble())
                val zoomState = rememberVicoZoomState(
                    zoomEnabled = false,
                    initialZoom = fixedZoom,
                    minZoom = fixedZoom,
                    maxZoom = fixedZoom,
                )
                ProvideVicoTheme(rememberM3VicoTheme()) {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(rangeProvider = rangeProvider),
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis = HorizontalAxis.rememberBottom(),
                        ),
                        modelProducer = modelProducer,
                        zoomState = zoomState,
                        animationSpec = null,
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
                        text = stringResource(R.string.sensor_detail_collecting_samples),
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
                Text(stringResource(option.labelRes))
            }
        }
    }
}

@Composable
private fun StatsGrid(uiState: SensorDetailUiState) {
    val stats = listOf(
        stringResource(R.string.sensor_detail_minimum) to uiState.minValue,
        stringResource(R.string.sensor_detail_maximum) to uiState.maxValue,
        stringResource(R.string.sensor_detail_average) to uiState.average,
        stringResource(R.string.sensor_detail_std_deviation) to uiState.stdDev,
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
private fun ActionsRow(
    isPaused: Boolean,
    onPause: () -> Unit,
    onReset: () -> Unit,
    sensorName: String,
    hasSamples: Boolean,
    onExport: (android.net.Uri, ExportFormat) -> Unit,
    exportFolderUri: String?,
) {
    val slug = remember(sensorName) { sensorName.lowercase().replace(Regex("[^a-z0-9]+"), "_") }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        FilledTonalButton(onClick = onPause, modifier = Modifier.weight(1f)) {
            Text(stringResource(if (isPaused) R.string.sensor_detail_resume else R.string.sensor_detail_pause))
        }
        FilledTonalButton(onClick = onReset, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.sensor_detail_reset))
        }
        ExportMenuButton(
            formats = listOf(ExportFormat.CSV, ExportFormat.JSON, ExportFormat.XML, ExportFormat.TXT),
            fileNameFor = { format -> "$slug.${format.extension}" },
            onFormatChosen = onExport,
            enabled = hasSamples,
            modifier = Modifier.weight(1f),
            defaultFolderUri = exportFolderUri,
        )
    }
}

@Composable
private fun MetadataCard(sensorInfo: SensorInfo) {
    SectionCard(
        title = stringResource(R.string.sensor_detail_metadata),
        rows = listOf(
            stringResource(R.string.sensor_detail_vendor) to sensorInfo.vendor,
            stringResource(R.string.sensor_detail_version) to sensorInfo.version.toString(),
            stringResource(R.string.sensor_detail_resolution) to sensorInfo.resolution.toString(),
            stringResource(R.string.sensor_detail_max_range) to sensorInfo.maximumRange.toString(),
            stringResource(R.string.sensor_detail_power) to "${sensorInfo.power} mA",
            stringResource(R.string.sensor_detail_min_delay) to "${sensorInfo.minDelayUs} µs",
            stringResource(R.string.sensor_detail_fifo) to sensorInfo.fifoMaxEventCount.toString(),
            stringResource(R.string.sensor_detail_wake_up) to stringResource(
                if (sensorInfo.isWakeUpSensor) R.string.common_yes else R.string.common_no,
            ),
            stringResource(R.string.sensor_detail_reporting_mode) to sensorInfo.reportingMode,
        ),
    )
}
