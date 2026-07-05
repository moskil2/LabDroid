package com.truesensor.app.feature.monitor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truesensor.app.core.designsystem.component.Sparkline
import com.truesensor.app.core.designsystem.component.StatusDot
import com.truesensor.app.data.sensors.SensorInfo

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
                    text = "Pinned sensors (${pinnedTypes.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = { showSheet = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Select")
                }
            }

            if (pinnedTypes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No sensors pinned yet",
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
                        PinnedSensorCard(sensorInfo, state)
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
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
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
private fun PinnedSensorCard(sensorInfo: SensorInfo, state: PinnedSensorState) {
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusDot(color = Color(0xFF2E7D32))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sensorInfo.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "● live",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = state.latestValue?.let { "%.2f".format(it) } ?: "—",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Sparkline(
                values = state.history,
                modifier = Modifier.size(width = 64.dp, height = 32.dp),
            )
        }
    }
}
