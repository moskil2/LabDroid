package com.labdroid.app.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.theme.ThemeMode
import com.labdroid.app.data.sensors.SensorDelayOption
import com.labdroid.app.data.settings.PreferencesRepository
import com.labdroid.app.data.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionStatus(val label: String, val granted: Boolean)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    val appVersion: String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "unknown"

    val permissionStatuses: List<PermissionStatus>
        get() = listOf(
            R.string.permission_location to Manifest.permission.ACCESS_FINE_LOCATION,
            R.string.permission_camera to Manifest.permission.CAMERA,
            R.string.permission_body_sensors to Manifest.permission.BODY_SENSORS,
            R.string.permission_bluetooth to Manifest.permission.BLUETOOTH_CONNECT,
            R.string.permission_phone_state to Manifest.permission.READ_PHONE_STATE,
            R.string.permission_notifications to Manifest.permission.POST_NOTIFICATIONS,
        ).map { (labelRes, permission) ->
            PermissionStatus(
                label = context.getString(labelRes),
                granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED,
            )
        }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setUseImperialUnits(value: Boolean) {
        viewModelScope.launch { preferencesRepository.setUseImperialUnits(value) }
    }

    fun setDefaultSamplingSpeed(option: SensorDelayOption) {
        viewModelScope.launch { preferencesRepository.setDefaultSamplingSpeed(option) }
    }

    fun setExportFolderUri(uri: String?) {
        viewModelScope.launch { preferencesRepository.setExportFolderUri(uri) }
    }

    fun resetPreferences() {
        viewModelScope.launch { preferencesRepository.resetAll() }
    }
}
