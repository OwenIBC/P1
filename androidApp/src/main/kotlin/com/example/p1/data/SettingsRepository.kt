package com.example.p1.data

import android.content.Context
import androidx.core.content.edit

/** Persistencia mínima de preferencias de usuario (sobreviven entre sesiones). */
class SettingsRepository(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isSoundEnabled(): Boolean = preferences.getBoolean(KEY_SOUND_ENABLED, DEFAULT_SOUND_ENABLED)

    fun setSoundEnabled(enabled: Boolean) = preferences.edit { putBoolean(KEY_SOUND_ENABLED, enabled) }

    private companion object {
        const val PREFERENCES_NAME = "matebruticas_settings"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val DEFAULT_SOUND_ENABLED = true
    }
}

