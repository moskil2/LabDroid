package com.labdroid.app.feature.monitor

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.component.ExportMenuButton
import com.labdroid.app.core.designsystem.component.Sparkline
import com.labdroid.app.core.designsystem.component.StatusDot
import com.labdroid.app.data.export.ExportFormat
import com.labdroid.app.data.sensors.SensorInfo
import com.labdroid.app.data.sensors.friendlyNameResFor
import com.labdroid.app.data.sensors.unitFor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: MonitorViewModel = hiltViewModel(),
) {
    val pinnedTypes by viewModel.pinnedTypes.collectAsState()
    val pinnedStates by viewModel.pinnedStates.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val lastSessionId by viewModel.lastSessionId.collectAsState()
    val exportFolderUri by viewModel.exportFolderUri.collectAsState()
    var showSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.monitor_pinned_sensors, pinnedTypes.size),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = { showSheet = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.monitor_select))
                }
            }

            lastSessionId?.let { sessionId ->
                ExportMenuButton(
                    formats = listOf(ExportFormat.CSV, ExportFormat.JSON, ExportFormat.XML, ExportFormat.TXT),
                    fileNameFor = { format -> "live_monitor_session_$sessionId.${format.extension}" },
                    onFormatChosen = { uri, format -> viewModel.exportSession(sessionId, uri, format) },
                    label = stringResource(R.string.monitor_export_recording),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    defaultFolderUri = exportFolderUri,
                )
            }

            if (pinnedTypes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.monitor_no_sensors_pinned),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 88.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        viewModel.allSensors.filter { it.type in pinnedTypes },
                        key = { it.type },
                    ) { sensorInfo ->
                        val state = pinnedStates[sensorInfo.type] ?: PinnedSensorState()
                        PinnedSensorCard(
                            sensorInfo = sensorInfo,
                            state = state,
                            onRemove = { viewModel.togglePinned(sensorInfo) },
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = viewModel::toggleRecording,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = contentPadding.calculateBottomPadding()),
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Outlined.Stop else Icons.Outlined.FiberManualRecord,
                contentDescription = stringResource(
                    if (isRecording) R.string.monitor_stop_recording else R.string.monitor_start_recording,
                ),
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            LazyColumn(modifier = Modifier.padding(bottom = 24.dp)) {
                items(viewModel.allSensors, key = { it.type }) { sensorInfo ->
                    val checked = sensorInfo.type in pinnedTypes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePinned(sensorInfo) }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { viewModel.togglePinned(sensorInfo) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sensorInfo.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun PinnedSensorCard(sensorInfo: SensorInfo, state: PinnedSensorState, onRemove: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatusDot(color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(friendlyNameResFor(sensorInfo.type)), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = sensorInfo.stringType,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.monitor_remove_sensor, sensorInfo.name),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = state.latestValue?.let { "%.2f".format(it) } ?: "—",
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                    val unit = unitFor(sensorInfo.type)
                    if (unit.isNotEmpty()) {
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Sparkline(
                        values = state.history,
                        modifier = Modifier.fillMaxSize(),
                        maxSamples = MonitorViewModel.HISTORY_SIZE,
                    )
                    if (state.history.isNotEmpty()) {
                        Text(
                            text = "%.2f".format(state.history.max()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                        Text(
                            text = "%.2f".format(state.history.min()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.BottomEnd),
                        )
                    }
                }
            }
        }
    }
}
