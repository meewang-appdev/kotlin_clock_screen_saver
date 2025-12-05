package com.example.clockscreensaver.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_preferences"

val Context.userPreferencesDataStore by preferencesDataStore(name = DATASTORE_NAME)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_24H = booleanPreferencesKey("is_24h_format")
        val TEXT_COLOR = stringPreferencesKey("text_color_hex")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val BRIGHTNESS = intPreferencesKey("brightness_level")
        val BURN_IN = booleanPreferencesKey("burn_in_protection")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
    }

    val preferencesFlow: Flow<UserPreferences> = context.userPreferencesDataStore.data
        .map { prefs -> prefs.toUserPreferences() }

    suspend fun update24Hour(is24h: Boolean) {
        update { it[Keys.IS_24H] = is24h }
    }

    suspend fun updateTextColor(hex: String) {
        update { it[Keys.TEXT_COLOR] = hex }
    }

    suspend fun updateFontStyle(id: String) {
        update { it[Keys.FONT_STYLE] = id }
    }

    suspend fun updateBrightness(level: Int) {
        update { it[Keys.BRIGHTNESS] = level.coerceIn(0, 100) }
    }

    suspend fun updateBurnIn(enabled: Boolean) {
        update { it[Keys.BURN_IN] = enabled }
    }

    suspend fun updateClockStyle(style: String) {
        update { it[Keys.CLOCK_STYLE] = style }
    }

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        context.userPreferencesDataStore.edit { prefs -> block(prefs) }
    }

    private fun Preferences.toUserPreferences(): UserPreferences = UserPreferences(
        is24Hour = this[Keys.IS_24H] ?: true,
        textColorHex = this[Keys.TEXT_COLOR] ?: "#E0E0E0",
        fontStyle = this[Keys.FONT_STYLE] ?: "default",
        brightnessLevel = this[Keys.BRIGHTNESS] ?: 70,
        burnInProtection = this[Keys.BURN_IN] ?: true,
        clockStyle = this[Keys.CLOCK_STYLE] ?: "basic"
    )
}
