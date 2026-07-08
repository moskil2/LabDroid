package com.labdroid.app.data.camera

data class CameraInfo(
    val id: String,
    val label: String,
    val facing: String,
    val lensType: String,
    val sensorSizeMm: String,
    val pixelArraySize: String,
    val focalLengthsMm: String,
    val fovDegrees: String,
    val zoomRange: String,
    val hasOis: Boolean,
    val hasFlash: Boolean,
    val supportsRaw: Boolean,
    val maxFps: String,
    val isLogicalMultiCamera: Boolean,
)
