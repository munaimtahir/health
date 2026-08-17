package com.vexel.passport.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferencesDataStore by preferencesDataStore(name = "vexel_preferences")

class PreferencesStore(private val context: Context) {
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val onboardingKey = booleanPreferencesKey("onboarding_complete")
    private val pinMaterialKey = stringPreferencesKey("pin_material")
    private val lockTimeoutMinutesKey = intPreferencesKey("lock_timeout_minutes")
    private val hideRecentAppsPreviewKey = booleanPreferencesKey("hide_recent_apps_preview")

    val preferences: Flow<UserPreferences> = context.preferencesDataStore.data.map { values ->
        UserPreferences(
            darkTheme = values[darkThemeKey] ?: false,
            onboardingComplete = values[onboardingKey] ?: false,
            pinMaterial = values[pinMaterialKey] ?: "",
            lockTimeoutMinutes = values[lockTimeoutMinutesKey] ?: 0,
            hideRecentAppsPreview = values[hideRecentAppsPreviewKey] ?: false,
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.preferencesDataStore.edit { values -> values[darkThemeKey] = enabled }
    }

    suspend fun setOnboardingComplete(enabled: Boolean) {
        context.preferencesDataStore.edit { values -> values[onboardingKey] = enabled }
    }

    suspend fun setPinMaterial(material: String) {
        context.preferencesDataStore.edit { values -> values[pinMaterialKey] = material }
    }

    suspend fun clearPinMaterial() {
        context.preferencesDataStore.edit { values ->
            values.remove(pinMaterialKey)
        }
    }

    suspend fun setLockTimeoutMinutes(minutes: Int) {
        require(isSupportedLockTimeoutMinutes(minutes))
        context.preferencesDataStore.edit { values -> values[lockTimeoutMinutesKey] = minutes }
    }

    suspend fun setHideRecentAppsPreview(enabled: Boolean) {
        context.preferencesDataStore.edit { values -> values[hideRecentAppsPreviewKey] = enabled }
    }

    suspend fun clearAll() {
        context.preferencesDataStore.edit { values -> values.clear() }
    }
}
