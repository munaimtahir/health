package pk.vexel.healthpassport.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferencesDataStore by preferencesDataStore(name = "vexel_preferences")

class PreferencesStore(private val context: Context) {
    private val darkThemeKey = booleanPreferencesKey("dark_theme")

    val preferences: Flow<UserPreferences> = context.preferencesDataStore.data.map { values ->
        UserPreferences(darkTheme = values[darkThemeKey] ?: false)
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.preferencesDataStore.edit { values -> values[darkThemeKey] = enabled }
    }
}
