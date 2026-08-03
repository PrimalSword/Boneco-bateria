package com.primalsword.voltinho.model

import android.content.Intent
import android.os.BatteryManager

/** Immutable battery information consumed by both the app preview and the overlay. */
data class BatterySnapshot(
    val level: Int = 100,
    val charging: Boolean = false,
    val full: Boolean = false,
    val temperatureCelsius: Float? = null,
) {
    val mood: BatteryMood
        get() = BatteryMood.from(level = level, charging = charging, full = full)

    companion object {
        fun from(intent: Intent?): BatterySnapshot {
            if (intent == null) return BatterySnapshot()

            val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
            val level = ((rawLevel * 100f) / scale).toInt().coerceIn(0, 100)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            val full = status == BatteryManager.BATTERY_STATUS_FULL || level == 100
            val rawTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            val temperature = rawTemperature
                .takeUnless { it == Int.MIN_VALUE }
                ?.div(10f)

            return BatterySnapshot(
                level = level,
                charging = charging,
                full = full,
                temperatureCelsius = temperature,
            )
        }
    }
}

enum class BatteryMood {
    CELEBRATING,
    CHARGING,
    ENERGETIC,
    CONTENT,
    TIRED,
    CRITICAL;

    companion object {
        fun from(level: Int, charging: Boolean, full: Boolean): BatteryMood = when {
            full -> CELEBRATING
            charging -> CHARGING
            level >= 80 -> ENERGETIC
            level >= 50 -> CONTENT
            level >= 20 -> TIRED
            else -> CRITICAL
        }
    }
}

enum class MascotKind(val displayName: String) {
    PINGO("Pingo"),
    BYTE("Byte"),
    MIMO("Mimo");

    companion object {
        fun fromStored(value: String?): MascotKind = entries.firstOrNull { it.name == value } ?: PINGO
    }
}
