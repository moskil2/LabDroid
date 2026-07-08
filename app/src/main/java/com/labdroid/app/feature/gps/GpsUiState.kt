package com.labdroid.app.feature.gps

import com.labdroid.app.data.location.SatelliteInfo

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val accuracyHorizontal: Float? = null,
    val accuracyVertical: Float? = null,
    val satellitesInView: Int = 0,
    val satellitesUsed: Int = 0,
    val satellites: List<SatelliteInfo> = emptyList(),
    val provider: String = "—",
    val isTracking: Boolean = false,
)

data class CompassUiState(
    val magneticHeading: Float? = null,
    val trueHeading: Float? = null,
    val fieldStrengthMicroTesla: Float? = null,
    val accuracy: Int? = null,
)
