package com.labdroid.app.data.sensors

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Android has no hardware "cellular signal" Sensor type, so this listens for live signal strength
 * updates directly from TelephonyManager (dBm of the strongest measured cell), the same source the
 * Hardware screen's one-off snapshot uses.
 */
class GsmSignalRepository @Inject constructor(@ApplicationContext private val context: Context) {

    @Suppress("DEPRECATION")
    fun observeSignalDbm(): Flow<Float> = callbackFlow {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        if (telephonyManager == null) {
            close()
            return@callbackFlow
        }
        val listener = object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                val dbm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    signalStrength.cellSignalStrengths.firstOrNull()?.dbm
                } else {
                    null
                }
                trySend((dbm ?: signalStrength.level).toFloat())
            }
        }
        runCatching { telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS) }
        awaitClose { runCatching { telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE) } }
    }
}
