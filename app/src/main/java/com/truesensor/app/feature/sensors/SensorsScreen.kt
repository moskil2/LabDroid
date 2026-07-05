package com.truesensor.app.feature.sensors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truesensor.app.core.designsystem.component.StatusDot
import com.truesensor.app.data.sensors.SensorIconMap
import com.truesensor.app.data.sensors.SensorInfo
import com.truesensor.app.data.sensors.representativeValue

@Composable
fun SensorsScreen(
    contentPadding: PaddingValues,
    onSensorClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SensorsViewModel = hiltViewModel(),
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
        ),
    ) {
        viewModel.groupedSensors.forEach { group ->
            item(key = "header_${group.category}") {
                Text(
                    text = group.category.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                )
            }
            items(group.sensors, key = { it.type }) { sensorInfo ->
                SensorRow(
                    sensorInfo = sensorInfo,
                    onClick = { onSensorClick(sensorInfo.type) },
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun SensorRow(
    sensorInfo: SensorInfo,
    onClick: () -> Unit,
    viewModel: SensorsViewModel,
) {
    val reading by produceState(initialValue = null as com.truesensor.app.data.sensors.SensorReading?, key1 = sensorInfo.type) {
        viewModel.observeReadings(sensorInfo).collect { value = it }
    }
    val valueText = remember(reading) {
        reading?.let { "%.2f".format(representativeValue(it.values)) } ?: "—"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = SensorIconMap.iconFor(sensorInfo.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sensorInfo.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = sensorInfo.stringType,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusDot(color = Color(0xFF2E7D32))
                Text(
                    text = "ok",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
