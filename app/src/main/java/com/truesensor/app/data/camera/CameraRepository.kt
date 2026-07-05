package com.truesensor.app.data.camera

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.atan

class CameraRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getCameras(): List<CameraInfo> = try {
        cameraManager.cameraIdList.map(::buildCameraInfo)
    } catch (e: CameraAccessException) {
        emptyList()
    }

    private fun buildCameraInfo(id: String): CameraInfo {
        val characteristics = cameraManager.getCameraCharacteristics(id)

        val facing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> "Front"
            CameraCharacteristics.LENS_FACING_BACK -> "Back"
            CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
            else -> "Unknown"
        }

        val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val sensorSizeMm = physicalSize?.let { "%.2f × %.2f mm".format(it.width, it.height) } ?: "Unknown"

        val pixelArray = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        val pixelArraySize = pixelArray?.let { "${it.width} × ${it.height}" } ?: "Unknown"

        val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val focalLengthsMm = focalLengths
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { "%.2f".format(it) }
            ?.let { "$it mm" }
            ?: "Unknown"

        val fovDegrees = if (focalLengths != null && focalLengths.isNotEmpty() && physicalSize != null) {
            val fov = 2 * Math.toDegrees(atan((physicalSize.width / (2 * focalLengths[0])).toDouble()))
            "%.0f°".format(fov)
        } else {
            "Unknown"
        }

        val zoomRange = characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
        val zoomText = zoomRange?.let { "%.1fx – %.1fx".format(it.lower, it.upper) } ?: "1.0x"

        val stabilizationModes = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
        val hasOis = stabilizationModes?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) == true

        val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

        val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: IntArray(0)
        val supportsRaw = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
        val isLogicalMultiCamera = capabilities.contains(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA,
        )

        val fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
        val maxFps = fpsRanges?.maxOfOrNull { it.upper }?.let { "$it fps" } ?: "Unknown"

        return CameraInfo(
            id = id,
            label = "$facing Camera $id",
            facing = facing,
            sensorSizeMm = sensorSizeMm,
            pixelArraySize = pixelArraySize,
            focalLengthsMm = focalLengthsMm,
            fovDegrees = fovDegrees,
            zoomRange = zoomText,
            hasOis = hasOis,
            hasFlash = hasFlash,
            supportsRaw = supportsRaw,
            maxFps = maxFps,
            isLogicalMultiCamera = isLogicalMultiCamera,
        )
    }
}
