package com.truesensor.app.data.connectivity

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class ConnectivitySnapshot(
    val activeConnectionLabel: String,
    val bluetoothAvailable: Boolean,
    val nfcAvailable: Boolean,
)

class ConnectivityRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getSnapshot(): ConnectivitySnapshot = ConnectivitySnapshot(
        activeConnectionLabel = getActiveConnectionLabel(),
        bluetoothAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
        nfcAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC),
    )

    private fun getActiveConnectionLabel(): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "Unknown"
        val network = connectivityManager.activeNetwork ?: return "No connection"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No connection"
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }
    }
}
