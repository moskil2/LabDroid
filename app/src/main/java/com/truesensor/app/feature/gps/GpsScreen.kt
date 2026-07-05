package com.truesensor.app.feature.gps

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.truesensor.app.core.designsystem.component.ExportMenuButton
import com.truesensor.app.core.designsystem.component.InfoCard
import com.truesensor.app.data.export.ExportFormat

private enum class GpsTab(val label: String) { LOCATION("Location"), COMPASS("Compass") }

@Composable
fun GpsScreen(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    if (!hasPermission) {
        PermissionRationale(
            contentPadding = contentPadding,
            modifier = modifier,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        )
    } else {
        GpsContent(contentPadding = contentPadding, modifier = modifier)
    }
}

@Composable
private fun PermissionRationale(
    contentPadding: PaddingValues,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Location access is needed to show GPS position and compass true-north correction.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("Grant location permission")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GpsContent(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: GpsViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableStateOf(GpsTab.LOCATION) }
    val locationState by viewModel.locationState.collectAsState()
    val compassState by viewModel.compassState.collectAsState()
    val lastSessionId by viewModel.lastSessionId.collectAsState()
    val useImperialUnits by viewModel.useImperialUnits.collectAsState()
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
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                GpsTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(index, GpsTab.entries.size),
                    ) {
                        Text(tab.label)
                    }
                }
            }
        }

        if (selectedTab == GpsTab.LOCATION) {
            item { MapPlaceholder() }
            item { LocationGrid(locationState, useImperialUnits) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(onClick = viewModel::toggleTracking, modifier = Modifier.weight(1f)) {
                        Text(if (locationState.isTracking) "Stop Track" else "Start Track")
                    }
                    val sessionId = lastSessionId
                    ExportMenuButton(
                        formats = listOf(ExportFormat.GPX, ExportFormat.CSV),
                        fileNameFor = { format -> "gps_track_$sessionId.${format.extension}" },
                        onFormatChosen = { uri, format ->
                            sessionId?.let { viewModel.exportSession(it, uri, format) }
                        },
                        enabled = sessionId != null,
                        label = "Export GPX·CSV",
                        modifier = Modifier.weight(1f),
                        defaultFolderUri = exportFolderUri,
                    )
                }
            }
        } else {
            item { CompassContent(compassState) }
        }
    }
}

@Composable
private fun MapPlaceholder() {
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "map view · track overlay",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val METERS_TO_FEET = 3.28084f
private const val MPS_TO_MPH = 2.23694f

@Composable
private fun LocationGrid(state: LocationUiState, useImperialUnits: Boolean) {
    val distanceUnit = if (useImperialUnits) "ft" else "m"
    val speedUnit = if (useImperialUnits) "mph" else "m/s"
    fun formatDistance(meters: Float): String =
        "%.1f %s".format(if (useImperialUnits) meters * METERS_TO_FEET else meters, distanceUnit)
    fun formatSpeed(metersPerSecond: Float): String =
        "%.1f %s".format(if (useImperialUnits) metersPerSecond * MPS_TO_MPH else metersPerSecond, speedUnit)

    val entries = listOf(
        "Latitude" to (state.latitude?.let { "%.6f".format(it) } ?: "—"),
        "Longitude" to (state.longitude?.let { "%.6f".format(it) } ?: "—"),
        "Altitude" to (state.altitude?.let { formatDistance(it.toFloat()) } ?: "—"),
        "Speed" to (state.speed?.let { formatSpeed(it) } ?: "—"),
        "Bearing" to (state.bearing?.let { "%.0f°".format(it) } ?: "—"),
        "Accuracy H" to (state.accuracyHorizontal?.let { formatDistance(it) } ?: "—"),
        "Accuracy V" to (state.accuracyVertical?.let { formatDistance(it) } ?: "—"),
        "Satellites" to "${state.satellitesUsed}/${state.satellitesInView}",
        "Provider" to state.provider,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (label, value) ->
                    InfoCard(label = label, value = value, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompassContent(state: CompassUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompassDial(headingDegrees = state.magneticHeading ?: 0f)
        Text(
            text = state.trueHeading?.let { "%.0f°".format(it) } ?: "—",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = "Magnetic ${state.magneticHeading?.let { "%.0f°".format(it) } ?: "—"} · " +
                "True ${state.trueHeading?.let { "%.0f°".format(it) } ?: "—"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                label = "Field strength",
                value = state.fieldStrengthMicroTesla?.let { "%.1f µT".format(it) } ?: "—",
                modifier = Modifier.weight(1f),
            )
            InfoCard(
                label = "Calibration",
                value = calibrationLabel(state.accuracy),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun calibrationLabel(accuracy: Int?): String = when (accuracy) {
    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
    android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
    else -> "Unknown"
}
