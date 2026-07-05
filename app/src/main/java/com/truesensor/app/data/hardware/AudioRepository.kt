package com.truesensor.app.data.hardware

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class AudioSnapshot(val inputDeviceCount: Int, val outputDeviceCount: Int)

class AudioRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getAudioSnapshot(): AudioSnapshot {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return AudioSnapshot(
            inputDeviceCount = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).size,
            outputDeviceCount = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).size,
        )
    }
}
