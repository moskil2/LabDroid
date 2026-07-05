package com.truesensor.app.feature.dashboard

data class DashboardUiState(
    val deviceModel: String = "—",
    val manufacturer: String = "—",
    val androidVersion: String = "—",
    val kernelVersion: String = "—",
    val buildNumber: String = "—",
    val cpuAbi: String = "—",
    val ramSummary: String = "—",
    val storageSummary: String = "—",
    val batterySummary: String = "—",
    val chargingSummary: String = "—",
    val gpsSummary: String = "—",
    val sensorsSummary: String = "—",
    val camerasSummary: String = "—",
    val connectivitySummary: String = "—",
) {
    val infoCards: List<Pair<String, String>>
        get() = listOf(
            "Device model" to deviceModel,
            "Manufacturer" to manufacturer,
            "Android version" to androidVersion,
            "Kernel" to kernelVersion,
            "Build number" to buildNumber,
            "CPU architecture" to cpuAbi,
            "RAM" to ramSummary,
            "Storage" to storageSummary,
            "Battery" to batterySummary,
            "Charging" to chargingSummary,
            "GPS" to gpsSummary,
            "Sensors" to sensorsSummary,
            "Cameras" to camerasSummary,
            "Connectivity" to connectivitySummary,
        )
}
