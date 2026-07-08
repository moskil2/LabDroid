package com.labdroid.app.data.camera

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.labdroid.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.atan

class CameraRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getCameras(): List<CameraInfo> = try {
        cameraManager.cameraIdList
            .map(::buildRawCameraInfo)
            .groupBy { it.facing }
            .flatMap { (_, camerasForFacing) ->
                camerasForFacing
                    .sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
                    .mapIndexed { index, raw -> raw.toCameraInfo(index + 1, context) }
            }
    } catch (e: CameraAccessException) {
        emptyList()
    }

    private fun buildRawCameraInfo(id: String): RawCameraInfo {
        val characteristics = cameraManager.getCameraCharacteristics(id)

        val unknown = context.getString(R.string.cameras_unknown)
        val facing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> context.getString(R.string.cameras_facing_front)
            CameraCharacteristics.LENS_FACING_BACK -> context.getString(R.string.cameras_facing_back)
            CameraCharacteristics.LENS_FACING_EXTERNAL -> context.getString(R.string.cameras_facing_external)
            else -> unknown
        }

        val physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val sensorSizeMm = physicalSize?.let { "%.2f × %.2f mm".format(it.width, it.height) } ?: unknown

        val pixelArray = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        val pixelArraySize = pixelArray?.let { "${it.width} × ${it.height}" } ?: unknown
        val megapixels = pixelArray?.let { (it.width.toLong() * it.height.toLong()) / 1_000_000.0 }

        val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val focalLengthsMm = focalLengths
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { "%.2f".format(it) }
            ?.let { "$it mm" }
            ?: unknown

        val fovDegreesValue = if (focalLengths != null && focalLengths.isNotEmpty() && physicalSize != null) {
            2 * Math.toDegrees(atan((physicalSize.width / (2 * focalLengths[0])).toDouble()))
        } else {
            null
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
        val maxFps = fpsRanges?.maxOfOrNull { it.upper }?.let { "$it fps" } ?: unknown

        return RawCameraInfo(
            id = id,
            facing = facing,
            sensorSizeMm = sensorSizeMm,
            pixelArraySize = pixelArraySize,
            focalLengthsMm = focalLengthsMm,
            fovDegreesValue = fovDegreesValue,
            megapixels = megapixels,
            zoomRange = zoomText,
            hasOis = hasOis,
            hasFlash = hasFlash,
            supportsRaw = supportsRaw,
            maxFps = maxFps,
            isLogicalMultiCamera = isLogicalMultiCamera,
        )
    }

    private data class RawCameraInfo(
        val id: String,
        val facing: String,
        val sensorSizeMm: String,
        val pixelArraySize: String,
        val focalLengthsMm: String,
        val fovDegreesValue: Double?,
        val megapixels: Double?,
        val zoomRange: String,
        val hasOis: Boolean,
        val hasFlash: Boolean,
        val supportsRaw: Boolean,
        val maxFps: String,
        val isLogicalMultiCamera: Boolean,
    ) {
        fun toCameraInfo(index: Int, context: Context): CameraInfo {
            val lensType = classifyLensType(context, fovDegreesValue, megapixels)
            return CameraInfo(
                id = id,
                label = "$facing ${context.getString(R.string.cameras_camera_number, index)} · $lensType",
                facing = facing,
                lensType = lensType,
                sensorSizeMm = sensorSizeMm,
                pixelArraySize = pixelArraySize,
                focalLengthsMm = focalLengthsMm,
                fovDegrees = fovDegreesValue?.let { "%.0f°".format(it) } ?: context.getString(R.string.cameras_unknown),
                zoomRange = zoomRange,
                hasOis = hasOis,
                hasFlash = hasFlash,
                supportsRaw = supportsRaw,
                maxFps = maxFps,
                isLogicalMultiCamera = isLogicalMultiCamera,
            )
        }
    }

    private companion object {
        fun classifyLensType(context: Context, fovDegrees: Double?, megapixels: Double?): String = when {
            fovDegrees == null -> context.getString(R.string.cameras_lens_standard)
            fovDegrees >= 100.0 -> context.getString(R.string.cameras_lens_ultra_wide)
            fovDegrees <= 45.0 -> context.getString(R.string.cameras_lens_telephoto)
            megapixels != null && megapixels <= 5.0 -> context.getString(R.string.cameras_lens_macro)
            else -> context.getString(R.string.cameras_lens_wide)
        }
    }
}
