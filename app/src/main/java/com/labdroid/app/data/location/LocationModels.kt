package com.labdroid.app.data.location

data class SatelliteInfo(
    val svid: Int,
    val constellationType: Int,
    val azimuthDegrees: Float,
    val elevationDegrees: Float,
    val cn0DbHz: Float,
    val usedInFix: Boolean,
)

data class GnssStatusSnapshot(
    val satellitesInView: Int,
    val satellitesUsed: Int,
    val satellites: List<SatelliteInfo> = emptyList(),
)
