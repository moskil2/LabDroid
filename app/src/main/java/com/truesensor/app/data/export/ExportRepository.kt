package com.truesensor.app.data.export

import android.content.Context
import android.net.Uri
import com.truesensor.app.data.recording.LocationSampleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class ExportRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun exportSensorSamples(
        uri: Uri,
        format: ExportFormat,
        sessionLabel: String,
        rows: List<SensorSampleRow>,
    ) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.writer().use { writer ->
                when (format) {
                    ExportFormat.CSV -> writeSensorCsv(writer, rows)
                    ExportFormat.JSON -> writeSensorJson(writer, sessionLabel, rows)
                    ExportFormat.XML -> writeSensorXml(writer, sessionLabel, rows)
                    ExportFormat.TXT -> writeSensorTxt(writer, sessionLabel, rows)
                    ExportFormat.GPX -> error("GPX is not supported for sensor samples")
                }
            }
        }
    }

    fun exportLocationSamples(
        uri: Uri,
        format: ExportFormat,
        samples: List<LocationSampleEntity>,
    ) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.writer().use { writer ->
                when (format) {
                    ExportFormat.GPX -> writeLocationGpx(writer, samples)
                    ExportFormat.CSV -> writeLocationCsv(writer, samples)
                    else -> error("$format is not supported for location samples")
                }
            }
        }
    }

    private fun writeSensorCsv(writer: Writer, rows: List<SensorSampleRow>) {
        writer.write("timestamp_millis,label,value\n")
        rows.forEach { writer.write("${it.timestampMillis},${csvEscape(it.label)},${it.value}\n") }
    }

    private fun writeSensorJson(writer: Writer, label: String, rows: List<SensorSampleRow>) {
        writer.write("{\"label\":\"${jsonEscape(label)}\",\"samples\":[")
        rows.forEachIndexed { index, row ->
            if (index > 0) writer.write(",")
            writer.write(
                "{\"t\":${row.timestampMillis},\"label\":\"${jsonEscape(row.label)}\",\"v\":${row.value}}",
            )
        }
        writer.write("]}")
    }

    private fun writeSensorXml(writer: Writer, label: String, rows: List<SensorSampleRow>) {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        writer.write("<samples label=\"${xmlEscape(label)}\">\n")
        rows.forEach {
            writer.write(
                "  <sample t=\"${it.timestampMillis}\" label=\"${xmlEscape(it.label)}\" v=\"${it.value}\" />\n",
            )
        }
        writer.write("</samples>\n")
    }

    private fun writeSensorTxt(writer: Writer, label: String, rows: List<SensorSampleRow>) {
        writer.write("$label\n")
        rows.forEach { writer.write("${it.timestampMillis}\t${it.label}\t${it.value}\n") }
    }

    private fun writeLocationCsv(writer: Writer, samples: List<LocationSampleEntity>) {
        writer.write("timestamp_millis,latitude,longitude,altitude,speed,bearing,accuracy\n")
        samples.forEach {
            writer.write(
                "${it.timestampMillis},${it.latitude},${it.longitude},${it.altitude},${it.speed},${it.bearing},${it.accuracy}\n",
            )
        }
    }

    private fun writeLocationGpx(writer: Writer, samples: List<LocationSampleEntity>) {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        writer.write("<gpx version=\"1.1\" creator=\"TrueSensor\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        writer.write("  <trk><name>TrueSensor Track</name><trkseg>\n")
        samples.forEach {
            writer.write(
                "    <trkpt lat=\"${it.latitude}\" lon=\"${it.longitude}\">" +
                    "<ele>${it.altitude}</ele>" +
                    "<time>${isoFormat.format(Date(it.timestampMillis))}</time></trkpt>\n",
            )
        }
        writer.write("  </trkseg></trk>\n</gpx>\n")
    }

    private fun csvEscape(value: String) = if (value.contains(",")) "\"${value.replace("\"", "\"\"")}\"" else value

    private fun jsonEscape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun xmlEscape(value: String) = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
