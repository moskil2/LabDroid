package com.labdroid.app.feature.gps

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.component.ExportMenuButton
import com.labdroid.app.core.designsystem.component.InfoCard
import com.labdroid.app.data.export.ExportFormat

private enum class GpsTab(@StringRes val labelRes: Int) {
    LOCATION(R.string.gps_tab_location),
    COMPASS(R.string.gps_tab_compass),
}

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
                text = stringResource(R.string.gps_permission_rationale),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.gps_grant_permission))
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
                        Text(stringResource(tab.labelRes))
                    }
                }
            }
        }

        if (selectedTab == GpsTab.LOCATION) {
            item {
                SatelliteRadar(
                    satellites = locationState.satellites,
                    headingDegrees = compassState.trueHeading ?: compassState.magneticHeading ?: 0f,
                )
            }
            item { SatelliteSignalBarChart(satellites = locationState.satellites) }
            item { PrecisePositionCard(latitude = locationState.latitude, longitude = locationState.longitude) }
            item { LocationGrid(locationState, useImperialUnits) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(onClick = viewModel::toggleTracking, modifier = Modifier.weight(1f)) {
                        Text(stringResource(if (locationState.isTracking) R.string.gps_stop_track else R.string.gps_start_track))
                    }
                    val sessionId = lastSessionId
                    ExportMenuButton(
                        formats = listOf(ExportFormat.GPX, ExportFormat.CSV),
                        fileNameFor = { format -> "gps_track_$sessionId.${format.extension}" },
                        onFormatChosen = { uri, format ->
                            sessionId?.let { viewModel.exportSession(it, uri, format) }
                        },
                        enabled = sessionId != null,
                        label = stringResource(R.string.gps_export_gpx_csv),
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
private fun PrecisePositionCard(latitude: Double?, longitude: Double?) {
    val context = LocalContext.current
    ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.gps_precise_position),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (latitude != null && longitude != null) {
                val decimalText = String.format(java.util.Locale.US, "%.6f, %.6f", latitude, longitude)
                val dmsText = "${formatDms(latitude, "N", "S")} ${formatDms(longitude, "E", "W")}"
                SelectionContainer {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        CoordinateLine(text = decimalText, onCopy = { copyToClipboard(context, decimalText) })
                        CoordinateLine(text = dmsText, onCopy = { copyToClipboard(context, dmsText) })
                    }
                }
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CoordinateLine(text: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = stringResource(R.string.gps_copy),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Position", text))
}

private fun formatDms(decimalDegrees: Double, positiveHemisphere: String, negativeHemisphere: String): String {
    val hemisphere = if (decimalDegrees >= 0) positiveHemisphere else negativeHemisphere
    val absolute = kotlin.math.abs(decimalDegrees)
    val degrees = absolute.toInt()
    val minutesFull = (absolute - degrees) * 60
    val minutes = minutesFull.toInt()
    val seconds = (minutesFull - minutes) * 60
    return String.format(java.util.Locale.US, "%d°%02d'%05.2f\"%s", degrees, minutes, seconds, hemisphere)
}

private const val METERS_TO_FEET = 3.28084f
private const val MPS_TO_MPH = 2.23694f
private const val MPS_TO_KMH = 3.6f

@Composable
private fun LocationGrid(state: LocationUiState, useImperialUnits: Boolean) {
    val distanceUnit = if (useImperialUnits) "ft" else "m"
    val speedUnit = if (useImperialUnits) "mph" else "m/s"
    fun formatDistance(meters: Float): String =
        "%.1f %s".format(if (useImperialUnits) meters * METERS_TO_FEET else meters, distanceUnit)
    fun formatSpeed(metersPerSecond: Float): String =
        "%.1f %s".format(if (useImperialUnits) metersPerSecond * MPS_TO_MPH else metersPerSecond, speedUnit)

    val entries = listOf(
        stringResource(R.string.gps_latitude) to (state.latitude?.let { "%.6f".format(it) } ?: "—"),
        stringResource(R.string.gps_longitude) to (state.longitude?.let { "%.6f".format(it) } ?: "—"),
        stringResource(R.string.gps_altitude) to (state.altitude?.let { formatDistance(it.toFloat()) } ?: "—"),
        stringResource(R.string.gps_speed) to (state.speed?.let { formatSpeed(it) } ?: "—"),
        stringResource(R.string.gps_speed_kmh) to (state.speed?.let { "%.1f km/h".format(it * MPS_TO_KMH) } ?: "—"),
        stringResource(R.string.gps_bearing) to (state.bearing?.let { "%.0f°".format(it) } ?: "—"),
        stringResource(R.string.gps_accuracy_h) to (state.accuracyHorizontal?.let { formatDistance(it) } ?: "—"),
        stringResource(R.string.gps_accuracy_v) to (state.accuracyVertical?.let { formatDistance(it) } ?: "—"),
        stringResource(R.string.gps_satellites) to "${state.satellitesUsed}/${state.satellitesInView}",
        stringResource(R.string.gps_provider) to state.provider,
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
            text = stringResource(
                R.string.gps_magnetic_true,
                state.magneticHeading?.let { "%.0f°".format(it) } ?: "—",
                state.trueHeading?.let { "%.0f°".format(it) } ?: "—",
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                label = stringResource(R.string.gps_field_strength),
                value = state.fieldStrengthMicroTesla?.let { "%.1f µT".format(it) } ?: "—",
                modifier = Modifier.weight(1f),
            )
            InfoCard(
                label = stringResource(R.string.gps_calibration),
                value = stringResource(calibrationLabelRes(state.accuracy)),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@StringRes
private fun calibrationLabelRes(accuracy: Int?): Int = when (accuracy) {
    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> R.string.gps_calibration_high
    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> R.string.gps_calibration_medium
    android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW -> R.string.gps_calibration_low
    android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE -> R.string.gps_calibration_unreliable
    else -> R.string.gps_calibration_unknown
}
