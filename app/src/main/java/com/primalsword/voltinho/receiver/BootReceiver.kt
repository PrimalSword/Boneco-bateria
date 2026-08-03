package com.primalsword.voltinho.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.primalsword.voltinho.data.AppPreferences
import com.primalsword.voltinho.overlay.MascotOverlayService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED)) return

        val preferences = AppPreferences(context)
        if (preferences.startOnBoot && preferences.overlayEnabled && Settings.canDrawOverlays(context)) {
            MascotOverlayService.start(context)
        }
    }
}
