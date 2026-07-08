package com.labdroid.app.feature.sensors

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.component.StatusDot
import com.labdroid.app.data.sensors.SensorIconMap
import com.labdroid.app.data.sensors.SensorInfo
import com.labdroid.app.data.sensors.friendlyNameResFor
import com.labdroid.app.data.sensors.representativeValue

@Composable
fun SensorsScreen(
    contentPadding: PaddingValues,
    onSensorClick: (Int) -> Unit,
    onSoundLevelClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SensorsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val bodySensorsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            bodySensorsLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
        ),
    ) {
        item {
            SoundLevelRow(onClick = onSoundLevelClick, viewModel = viewModel)
        }
        items(viewModel.sensors, key = { it.type }) { sensorInfo ->
            SensorRow(
                sensorInfo = sensorInfo,
                onClick = { onSensorClick(sensorInfo.type) },
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun SoundLevelRow(onClick: () -> Unit, viewModel: SensorsViewModel) {
    val reading by produceState(initialValue = null as Float?, key1 = Unit) {
        viewModel.observeSoundLevel().collect { value = it }
    }
    val valueText = remember(reading) { reading?.let { "%.0f".format(it) } ?: "—" }
    val hasReading = reading != null

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
                imageVector = Icons.Outlined.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.sensor_sound_level_name),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.sensor_sound_level_subtitle),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$valueText dB",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusDot(color = if (hasReading) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline)
                Text(
                    text = stringResource(if (hasReading) R.string.sensors_status_ok else R.string.sensors_status_waiting),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val reading by produceState(initialValue = null as com.labdroid.app.data.sensors.SensorReading?, key1 = sensorInfo.type) {
        viewModel.observeReadings(sensorInfo).collect { value = it }
    }
    val valueText = remember(reading) {
        reading?.let { "%.2f".format(representativeValue(it.values)) } ?: "—"
    }
    val hasReading = reading != null

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
                text = stringResource(friendlyNameResFor(sensorInfo.type)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = sensorInfo.name,
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
                StatusDot(color = if (hasReading) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline)
                Text(
                    text = stringResource(if (hasReading) R.string.sensors_status_ok else R.string.sensors_status_waiting),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
