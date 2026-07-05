package com.truesensor.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.truesensor.app.core.designsystem.theme.ThemeMode
import com.truesensor.app.data.sensors.SensorDelayOption
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useImperialUnits: Boolean = false,
    val defaultSamplingSpeed: SensorDelayOption = SensorDelayOption.UI,
    val exportFolderUri: String? = null,
)

class PreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_IMPERIAL_UNITS = booleanPreferencesKey("use_imperial_units")
        val DEFAULT_SAMPLING_SPEED = stringPreferencesKey("default_sampling_speed")
        val EXPORT_FOLDER_URI = stringPreferencesKey("export_folder_uri")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            useImperialUnits = prefs[Keys.USE_IMPERIAL_UNITS] ?: false,
            defaultSamplingSpeed = prefs[Keys.DEFAULT_SAMPLING_SPEED]
                ?.let { runCatching { SensorDelayOption.valueOf(it) }.getOrNull() }
                ?: SensorDelayOption.UI,
            exportFolderUri = prefs[Keys.EXPORT_FOLDER_URI],
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setUseImperialUnits(value: Boolean) {
        context.dataStore.edit { it[Keys.USE_IMPERIAL_UNITS] = value }
    }

    suspend fun setDefaultSamplingSpeed(option: SensorDelayOption) {
        context.dataStore.edit { it[Keys.DEFAULT_SAMPLING_SPEED] = option.name }
    }

    suspend fun setExportFolderUri(uri: String?) {
        context.dataStore.edit {
            if (uri != null) it[Keys.EXPORT_FOLDER_URI] = uri else it.remove(Keys.EXPORT_FOLDER_URI)
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
