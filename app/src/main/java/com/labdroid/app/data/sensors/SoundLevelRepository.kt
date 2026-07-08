package com.labdroid.app.data.sensors

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Android has no hardware "sound level" Sensor type, so this measures relative loudness directly
 * from the microphone via AudioRecord. The resulting dB value is derived from the raw digital
 * signal (20*log10 of the RMS amplitude), not a calibrated sound-pressure-level reading against a
 * physical reference — the same limitation every consumer sound-meter app has without an
 * externally calibrated microphone.
 */
class SoundLevelRepository @Inject constructor() {

    fun observeDecibels(intervalMs: Long): Flow<Float> = callbackFlow {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING)
        val audioRecord = if (minBufferSize > 0) {
            runCatching {
                AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, ENCODING, minBufferSize * 2)
            }.getOrNull()?.takeIf { it.state == AudioRecord.STATE_INITIALIZED }
        } else {
            null
        }

        val job = if (audioRecord != null) {
            audioRecord.startRecording()
            launch(Dispatchers.IO) {
                val buffer = ShortArray(minBufferSize)
                while (isActive) {
                    val read = runCatching { audioRecord.read(buffer, 0, buffer.size) }.getOrDefault(0)
                    if (read > 0) {
                        var sumOfSquares = 0.0
                        for (i in 0 until read) {
                            val sample = buffer[i].toDouble()
                            sumOfSquares += sample * sample
                        }
                        val rms = sqrt(sumOfSquares / read)
                        val decibels = if (rms > 1.0) 20 * log10(rms) else 0.0
                        trySend(decibels.toFloat())
                    }
                    delay(intervalMs)
                }
            }
        } else {
            null
        }

        awaitClose {
            job?.cancel()
            runCatching { audioRecord?.stop() }
            audioRecord?.release()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }
}
