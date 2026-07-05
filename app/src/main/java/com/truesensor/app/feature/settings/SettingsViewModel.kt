package com.truesensor.app.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truesensor.app.core.designsystem.theme.ThemeMode
import com.truesensor.app.data.sensors.SensorDelayOption
import com.truesensor.app.data.settings.PreferencesRepository
import com.truesensor.app.data.settings.UserPreferences
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
            "Location" to Manifest.permission.ACCESS_FINE_LOCATION,
            "Camera" to Manifest.permission.CAMERA,
            "Body sensors" to Manifest.permission.BODY_SENSORS,
            "Bluetooth" to Manifest.permission.BLUETOOTH_CONNECT,
            "Phone state" to Manifest.permission.READ_PHONE_STATE,
            "Notifications" to Manifest.permission.POST_NOTIFICATIONS,
        ).map { (label, permission) ->
            PermissionStatus(
                label = label,
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
