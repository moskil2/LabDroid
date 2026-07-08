package com.labdroid.app.data.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.labdroid.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class AudioSnapshot(
    val outputDevices: List<String>,
    val inputDevices: List<String>,
    val outputSampleRateHz: Int?,
    val outputFramesPerBuffer: Int?,
    val lowLatencySupported: Boolean,
    val proAudioSupported: Boolean,
)

class AudioRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getAudioSnapshot(): AudioSnapshot {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .map { deviceTypeLabel(it.type) }
            .distinct()
        val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .map { deviceTypeLabel(it.type) }
            .distinct()
        val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull()
        val framesPerBuffer = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)?.toIntOrNull()
        val lowLatency = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
        val proAudio = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)

        return AudioSnapshot(
            outputDevices = outputs,
            inputDevices = inputs,
            outputSampleRateHz = sampleRate,
            outputFramesPerBuffer = framesPerBuffer,
            lowLatencySupported = lowLatency,
            proAudioSupported = proAudio,
        )
    }

    private fun deviceTypeLabel(type: Int): String = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> context.getString(R.string.audio_device_builtin_speaker)
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> context.getString(R.string.audio_device_earpiece)
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> context.getString(R.string.audio_device_builtin_mic)
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> context.getString(R.string.audio_device_wired_headset)
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> context.getString(R.string.audio_device_wired_headphones)
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> context.getString(R.string.audio_device_bluetooth_sco)
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> context.getString(R.string.audio_device_bluetooth_a2dp)
        AudioDeviceInfo.TYPE_USB_DEVICE -> context.getString(R.string.audio_device_usb_device)
        AudioDeviceInfo.TYPE_USB_HEADSET -> context.getString(R.string.audio_device_usb_headset)
        AudioDeviceInfo.TYPE_USB_ACCESSORY -> context.getString(R.string.audio_device_usb_accessory)
        AudioDeviceInfo.TYPE_HDMI -> context.getString(R.string.audio_device_hdmi)
        AudioDeviceInfo.TYPE_HDMI_ARC -> context.getString(R.string.audio_device_hdmi_arc)
        AudioDeviceInfo.TYPE_TELEPHONY -> context.getString(R.string.audio_device_telephony)
        AudioDeviceInfo.TYPE_DOCK -> context.getString(R.string.audio_device_dock)
        AudioDeviceInfo.TYPE_FM -> context.getString(R.string.audio_device_fm)
        AudioDeviceInfo.TYPE_FM_TUNER -> context.getString(R.string.audio_device_fm_tuner)
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE -> context.getString(R.string.audio_device_builtin_speaker_safe)
        AudioDeviceInfo.TYPE_LINE_ANALOG -> context.getString(R.string.audio_device_line_analog)
        AudioDeviceInfo.TYPE_LINE_DIGITAL -> context.getString(R.string.audio_device_line_digital)
        else -> context.getString(R.string.audio_device_type_generic, type)
    }
}
