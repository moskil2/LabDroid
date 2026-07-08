package com.labdroid.app.data.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.labdroid.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

data class BatterySnapshot(
    val percent: Int,
    val isCharging: Boolean,
    val chargingSource: String,
    val temperatureCelsius: Float,
    val health: String,
    val voltageVolts: Float,
    val technology: String,
    val present: Boolean,
    val currentNowMilliAmps: Float?,
    val chargeCounterMilliAmpHours: Int?,
    val designCapacityMilliAmpHours: Int?,
    val cycleCount: Int?,
)

class BatteryRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun getBatterySnapshot(): BatterySnapshot {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.battery_source_ac)
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.battery_source_usb)
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.battery_source_wireless)
            else -> context.getString(if (isCharging) R.string.battery_source_unknown else R.string.battery_source_not_plugged_in)
        }

        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltageMilliVolts = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val health = healthLabel(intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1)
        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        val present = intent?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true) ?: true

        val currentNowMicroAmps = readCurrentNowMicroAmps()
        val chargeCounterMicroAh = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val cycleCount = if (Build.VERSION.SDK_INT >= 34) {
            intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)?.takeIf { it >= 0 }
        } else {
            null
        }

        return BatterySnapshot(
            percent = percent,
            isCharging = isCharging,
            chargingSource = chargingSource,
            temperatureCelsius = tempTenths / 10f,
            health = health,
            voltageVolts = voltageMilliVolts / 1000f,
            technology = technology,
            present = present,
            currentNowMilliAmps = currentNowMicroAmps?.let { it / 1000f },
            chargeCounterMilliAmpHours = chargeCounterMicroAh
                .takeIf { it != Long.MIN_VALUE && it > 0 }
                ?.let { (it / 1000).toInt() },
            designCapacityMilliAmpHours = readDesignCapacityMah(),
            cycleCount = cycleCount,
        )
    }

    /**
     * `BatteryManager.BATTERY_PROPERTY_CURRENT_NOW` is meant to return `Long.MIN_VALUE` when
     * unsupported, but some OEM kernels (notably several Samsung/Exynos builds) instead return a
     * flat `0` at all times regardless of actual charge/discharge current. Since a genuine `0` is
     * possible but implausible as a *constant* reading, fall back to the kernel's own sysfs node
     * when the official API reports exactly zero.
     */
    private fun readCurrentNowMicroAmps(): Long? {
        val official = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            .takeIf { it != Long.MIN_VALUE }
        if (official != null && official != 0L) return official
        return readSysfsCurrentNowMicroAmps() ?: official
    }

    private fun readSysfsCurrentNowMicroAmps(): Long? = runCatching {
        File("/sys/class/power_supply/battery/current_now").readText().trim().toLong()
    }.getOrNull()

    /**
     * Uses the hidden `com.android.internal.os.PowerProfile` class via reflection to read the
     * OEM-declared rated battery capacity. This is not a public API; it can fail or return an
     * inaccurate value on some devices/Android versions, so failures are treated as "unavailable".
     */
    private fun readDesignCapacityMah(): Int? = runCatching {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val constructor = powerProfileClass.getConstructor(Context::class.java)
        val powerProfile = constructor.newInstance(context)
        val method = powerProfileClass.getMethod("getBatteryCapacity")
        (method.invoke(powerProfile) as Double).toInt().takeIf { it > 0 }
    }.getOrNull()

    private fun healthLabel(value: Int): String = context.getString(
        when (value) {
            BatteryManager.BATTERY_HEALTH_GOOD -> R.string.battery_health_good
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.battery_health_overheat
            BatteryManager.BATTERY_HEALTH_DEAD -> R.string.battery_health_dead
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.battery_health_over_voltage
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.string.battery_health_unspecified_failure
            BatteryManager.BATTERY_HEALTH_COLD -> R.string.battery_health_cold
            else -> R.string.battery_health_unknown
        },
    )
}
