package com.primalsword.voltinho.data

import android.content.Context
import android.content.SharedPreferences
import com.primalsword.voltinho.model.MascotKind

class AppPreferences(context: Context) {
    private val preferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var overlayEnabled: Boolean
        get() = preferences.getBoolean(KEY_OVERLAY_ENABLED, false)
        set(value) = edit { putBoolean(KEY_OVERLAY_ENABLED, value) }

    var mascotKind: MascotKind
        get() = MascotKind.fromStored(preferences.getString(KEY_MASCOT, MascotKind.PINGO.name))
        set(value) = edit { putString(KEY_MASCOT, value.name) }

    var mascotSizeDp: Int
        get() = preferences.getInt(KEY_SIZE_DP, 82).coerceIn(56, 132)
        set(value) = edit { putInt(KEY_SIZE_DP, value.coerceIn(56, 132)) }

    var opacity: Float
        get() = preferences.getFloat(KEY_OPACITY, 1f).coerceIn(0.45f, 1f)
        set(value) = edit { putFloat(KEY_OPACITY, value.coerceIn(0.45f, 1f)) }

    var showPercentage: Boolean
        get() = preferences.getBoolean(KEY_SHOW_PERCENTAGE, true)
        set(value) = edit { putBoolean(KEY_SHOW_PERCENTAGE, value) }

    var startOnBoot: Boolean
        get() = preferences.getBoolean(KEY_START_ON_BOOT, true)
        set(value) = edit { putBoolean(KEY_START_ON_BOOT, value) }

    var overlayX: Int
        get() = preferences.getInt(KEY_OVERLAY_X, Int.MIN_VALUE)
        set(value) = edit { putInt(KEY_OVERLAY_X, value) }

    var overlayY: Int
        get() = preferences.getInt(KEY_OVERLAY_Y, 0)
        set(value) = edit { putInt(KEY_OVERLAY_Y, value.coerceAtLeast(0)) }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private inline fun edit(block: SharedPreferences.Editor.() -> Unit) {
        preferences.edit().apply(block).apply()
    }

    companion object {
        const val FILE_NAME = "voltinho_preferences"
        const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        const val KEY_MASCOT = "mascot"
        const val KEY_SIZE_DP = "size_dp"
        const val KEY_OPACITY = "opacity"
        const val KEY_SHOW_PERCENTAGE = "show_percentage"
        const val KEY_START_ON_BOOT = "start_on_boot"
        const val KEY_OVERLAY_X = "overlay_x"
        const val KEY_OVERLAY_Y = "overlay_y"
    }
}
