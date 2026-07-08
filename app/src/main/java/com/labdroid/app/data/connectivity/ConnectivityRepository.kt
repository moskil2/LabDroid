package com.labdroid.app.data.connectivity

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.labdroid.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val NOT_AVAILABLE = "—"

data class ConnectivitySnapshot(
    val activeConnectionLabel: String,
    val isMetered: String,
    val hasValidatedInternet: String,
    val wifiSsid: String,
    val wifiSignal: String,
    val wifiFrequencyBand: String,
    val wifiLinkSpeed: String,
    val wifiStandard: String,
    val cellularOperator: String,
    val cellularGeneration: String,
    val cellularRoaming: String,
    val simState: String,
    val bluetoothAvailable: Boolean,
    val bluetoothAdapterName: String,
    val bluetoothEnabled: String,
    val bluetoothPairedCount: String,
    val nfcAvailable: Boolean,
)

class ConnectivityRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val permissionRequired get() = context.getString(R.string.connectivity_permission_required)
    private val yes get() = context.getString(R.string.common_yes)
    private val no get() = context.getString(R.string.common_no)
    private val unknown get() = context.getString(R.string.cameras_unknown)

    fun getSnapshot(): ConnectivitySnapshot {
        val capabilities = activeNetworkCapabilities()
        return ConnectivitySnapshot(
            activeConnectionLabel = activeConnectionLabel(capabilities),
            isMetered = capabilities?.let {
                if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) no else yes
            } ?: NOT_AVAILABLE,
            hasValidatedInternet = capabilities?.let {
                if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) yes else no
            } ?: NOT_AVAILABLE,
            wifiSsid = wifiSsid(),
            wifiSignal = wifiSignal(),
            wifiFrequencyBand = wifiFrequencyBand(),
            wifiLinkSpeed = wifiLinkSpeed(),
            wifiStandard = wifiStandard(),
            cellularOperator = cellularOperator(),
            cellularGeneration = cellularGeneration(),
            cellularRoaming = cellularRoaming(),
            simState = simState(),
            bluetoothAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            bluetoothAdapterName = bluetoothAdapterName(),
            bluetoothEnabled = bluetoothEnabled(),
            bluetoothPairedCount = bluetoothPairedCount(),
            nfcAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC),
        )
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun activeNetworkCapabilities(): NetworkCapabilities? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return null
        val network = connectivityManager.activeNetwork ?: return null
        return connectivityManager.getNetworkCapabilities(network)
    }

    private fun activeConnectionLabel(capabilities: NetworkCapabilities?): String {
        capabilities ?: return context.getString(R.string.connectivity_no_connection)
        return context.getString(
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> R.string.connectivity_wifi
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> R.string.connectivity_cellular
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> R.string.connectivity_ethernet
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> R.string.connectivity_vpn
                else -> R.string.connectivity_connected
            },
        )
    }

    private val wifiManager: WifiManager?
        get() = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private fun wifiSsid(): String {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return permissionRequired
        return runCatching {
            val ssid = wifiManager?.connectionInfo?.ssid
            ssid?.trim('"')?.takeIf { it.isNotBlank() && it != "<unknown ssid>" } ?: NOT_AVAILABLE
        }.getOrDefault(NOT_AVAILABLE)
    }

    private fun wifiSignal(): String = runCatching {
        val rssi = wifiManager?.connectionInfo?.rssi ?: return NOT_AVAILABLE
        val level = wifiManager?.let { WifiManager.calculateSignalLevel(rssi, 5) } ?: 0
        "$rssi dBm (level $level/4)"
    }.getOrDefault(NOT_AVAILABLE)

    private fun wifiFrequencyBand(): String = runCatching {
        val frequency = wifiManager?.connectionInfo?.frequency ?: return NOT_AVAILABLE
        val band = when {
            frequency in 2400..2500 -> "2.4 GHz"
            frequency in 4900..5900 -> "5 GHz"
            frequency in 5925..7125 -> "6 GHz"
            else -> context.getString(R.string.connectivity_unknown_band)
        }
        "$frequency MHz ($band)"
    }.getOrDefault(NOT_AVAILABLE)

    private fun wifiLinkSpeed(): String = runCatching {
        val speed = wifiManager?.connectionInfo?.linkSpeed ?: return NOT_AVAILABLE
        if (speed < 0) NOT_AVAILABLE else "$speed Mbps"
    }.getOrDefault(NOT_AVAILABLE)

    private fun wifiStandard(): String = runCatching {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return unknown
        when (wifiManager?.connectionInfo?.wifiStandard) {
            4 -> "802.11n"
            5 -> "802.11ac"
            6 -> "802.11ax"
            7 -> "802.11ad"
            1 -> "802.11a/b/g"
            else -> unknown
        }
    }.getOrDefault(unknown)

    private val telephonyManager: TelephonyManager?
        get() = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private fun cellularOperator(): String = runCatching {
        telephonyManager?.networkOperatorName?.takeIf { it.isNotBlank() } ?: NOT_AVAILABLE
    }.getOrDefault(NOT_AVAILABLE)

    private fun cellularGeneration(): String {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) return permissionRequired
        return runCatching {
            @Suppress("DEPRECATION")
            when (telephonyManager?.networkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN,
                -> "2G"

                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                -> "3G"

                TelephonyManager.NETWORK_TYPE_LTE -> "4G (LTE)"
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> NOT_AVAILABLE
                else -> NOT_AVAILABLE
            }
        }.getOrDefault(NOT_AVAILABLE)
    }

    private fun cellularRoaming(): String = runCatching {
        @Suppress("DEPRECATION")
        if (telephonyManager?.isNetworkRoaming == true) yes else no
    }.getOrDefault(NOT_AVAILABLE)

    private fun simState(): String = runCatching {
        context.getString(
            when (telephonyManager?.simState) {
                TelephonyManager.SIM_STATE_ABSENT -> R.string.connectivity_sim_absent
                TelephonyManager.SIM_STATE_READY -> R.string.connectivity_sim_ready
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> R.string.connectivity_sim_pin_required
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> R.string.connectivity_sim_puk_required
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> R.string.connectivity_sim_network_locked
                TelephonyManager.SIM_STATE_NOT_READY -> R.string.connectivity_sim_not_ready
                else -> R.string.cameras_unknown
            },
        )
    }.getOrDefault(NOT_AVAILABLE)

    private val bluetoothAdapter
        get() = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private fun bluetoothAdapterName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        ) {
            return permissionRequired
        }
        return runCatching {
            bluetoothAdapter?.name?.takeIf { it.isNotBlank() } ?: NOT_AVAILABLE
        }.getOrDefault(NOT_AVAILABLE)
    }

    private fun bluetoothEnabled(): String = runCatching {
        if (bluetoothAdapter?.isEnabled == true) yes else no
    }.getOrDefault(NOT_AVAILABLE)

    private fun bluetoothPairedCount(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        ) {
            return permissionRequired
        }
        return runCatching {
            bluetoothAdapter?.bondedDevices?.size?.toString() ?: NOT_AVAILABLE
        }.getOrDefault(NOT_AVAILABLE)
    }
}
