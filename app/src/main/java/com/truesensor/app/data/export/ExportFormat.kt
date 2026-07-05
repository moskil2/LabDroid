package com.truesensor.app.data.export

enum class ExportFormat(val label: String, val extension: String) {
    CSV("CSV", "csv"),
    JSON("JSON", "json"),
    XML("XML", "xml"),
    TXT("TXT", "txt"),
    GPX("GPX", "gpx"),
}

data class SensorSampleRow(val timestampMillis: Long, val label: String, val value: Float)
