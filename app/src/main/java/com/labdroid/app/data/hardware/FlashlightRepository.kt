package com.labdroid.app.data.hardware

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class FlashlightSnapshot(
    val available: Boolean,
    val maxStrengthLevel: Int?,
    val defaultStrengthLevel: Int?,
)

class FlashlightRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getFlashlightSnapshot(): FlashlightSnapshot {
        val characteristics = cameraManager.cameraIdList
            .firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
            }
            ?.let { cameraManager.getCameraCharacteristics(it) }

        val available = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        val maxStrength = if (Build.VERSION.SDK_INT >= 33) {
            characteristics?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL)
        } else {
            null
        }
        val defaultStrength = if (Build.VERSION.SDK_INT >= 33) {
            characteristics?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL)
        } else {
            null
        }

        return FlashlightSnapshot(
            available = available,
            maxStrengthLevel = maxStrength?.takeIf { it > 0 },
            defaultStrengthLevel = defaultStrength?.takeIf { it > 0 },
        )
    }
}
