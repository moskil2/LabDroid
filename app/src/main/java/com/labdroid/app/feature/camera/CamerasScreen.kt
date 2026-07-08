package com.labdroid.app.feature.camera

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.component.MetricRow
import com.labdroid.app.data.camera.CameraInfo

@Composable
fun CamerasScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: CamerasViewModel = hiltViewModel(),
) {
    if (viewModel.cameras.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.cameras_none_detected),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(viewModel.cameras, key = { it.id }) { camera ->
            ExpandableCameraCard(camera)
        }
    }
}

@Composable
private fun ExpandableCameraCard(camera: CameraInfo) {
    var expanded by rememberSaveable(camera.id) { mutableStateOf(true) }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = camera.label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                AssistChip(onClick = { expanded = !expanded }, label = { Text(camera.lensType) })
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                val yes = stringResource(R.string.common_yes)
                val no = stringResource(R.string.common_no)
                val rows = listOf(
                    stringResource(R.string.cameras_sensor) to camera.sensorSizeMm,
                    stringResource(R.string.cameras_pixel_array) to camera.pixelArraySize,
                    stringResource(R.string.cameras_focal_length) to camera.focalLengthsMm,
                    stringResource(R.string.cameras_fov) to camera.fovDegrees,
                    stringResource(R.string.cameras_zoom) to camera.zoomRange,
                    stringResource(R.string.cameras_ois) to if (camera.hasOis) yes else no,
                    stringResource(R.string.cameras_flash) to if (camera.hasFlash) yes else no,
                    stringResource(R.string.cameras_raw) to stringResource(
                        if (camera.supportsRaw) R.string.common_supported else R.string.common_not_supported,
                    ),
                    stringResource(R.string.cameras_max_fps) to camera.maxFps,
                    stringResource(R.string.cameras_logical_multi_camera) to if (camera.isLogicalMultiCamera) yes else no,
                )
                rows.forEachIndexed { index, (label, value) ->
                    MetricRow(label = label, value = value)
                    if (index != rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}
