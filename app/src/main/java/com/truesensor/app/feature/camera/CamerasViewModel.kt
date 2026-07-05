package com.truesensor.app.feature.camera

import androidx.lifecycle.ViewModel
import com.truesensor.app.data.camera.CameraInfo
import com.truesensor.app.data.camera.CameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CamerasViewModel @Inject constructor(
    cameraRepository: CameraRepository,
) : ViewModel() {
    val cameras: List<CameraInfo> = cameraRepository.getCameras()
}
