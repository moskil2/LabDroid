package com.labdroid.app.feature.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.component.SectionCard

@Composable
fun BatteryScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: BatteryViewModel = hiltViewModel(),
) {
    val battery by viewModel.snapshot.collectAsState()
    val yes = stringResource(R.string.common_yes)
    val no = stringResource(R.string.common_no)
    val unavailable = stringResource(R.string.common_unavailable)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val snapshot = battery
        if (snapshot != null) {
            item {
                SectionCard(
                    title = stringResource(R.string.battery_section_battery),
                    iconRes = R.drawable.ic_ph_battery,
                    rows = listOf(
                        stringResource(R.string.battery_level) to "${snapshot.percent}%",
                        stringResource(R.string.battery_status) to stringResource(
                            if (snapshot.isCharging) R.string.battery_charging else R.string.battery_not_charging,
                        ),
                        stringResource(R.string.battery_charging_source) to snapshot.chargingSource,
                        stringResource(R.string.battery_health) to snapshot.health,
                        stringResource(R.string.battery_present) to if (snapshot.present) yes else no,
                        stringResource(R.string.battery_technology) to snapshot.technology,
                    ),
                )
            }
            item {
                SectionCard(
                    title = stringResource(R.string.battery_section_electrical),
                    iconRes = R.drawable.ic_ph_electrical,
                    rows = listOf(
                        stringResource(R.string.battery_temperature) to "%.1f°C".format(snapshot.temperatureCelsius),
                        stringResource(R.string.battery_voltage) to "%.2f V".format(snapshot.voltageVolts),
                        stringResource(R.string.battery_current_now) to (
                            snapshot.currentNowMilliAmps?.let { "%.0f mA".format(it) } ?: unavailable
                            ),
                        stringResource(R.string.battery_remaining_charge) to (
                            snapshot.chargeCounterMilliAmpHours?.let { "$it mAh" } ?: unavailable
                            ),
                        stringResource(R.string.battery_design_capacity) to (
                            snapshot.designCapacityMilliAmpHours?.let { "$it mAh" } ?: unavailable
                            ),
                        stringResource(R.string.battery_cycle_count) to (snapshot.cycleCount?.toString() ?: unavailable),
                    ),
                )
            }
        } else {
            item { Text(stringResource(R.string.common_loading)) }
        }
    }
}
