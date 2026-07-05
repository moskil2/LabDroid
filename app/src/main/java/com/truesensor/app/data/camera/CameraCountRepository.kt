package com.truesensor.app.data.camera

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CameraCountRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getCameraCount(): Int {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            cameraManager.cameraIdList.size
        } catch (e: CameraAccessException) {
            0
        }
    }
}
