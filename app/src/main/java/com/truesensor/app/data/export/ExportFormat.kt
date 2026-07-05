package com.truesensor.app.data.export

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    CSV("CSV", "csv", "text/csv"),
    JSON("JSON", "json", "application/json"),
    XML("XML", "xml", "text/xml"),
    TXT("TXT", "txt", "text/plain"),
    GPX("GPX", "gpx", "application/gpx+xml"),
}

data class SensorSampleRow(val timestampMillis: Long, val label: String, val value: Float)
