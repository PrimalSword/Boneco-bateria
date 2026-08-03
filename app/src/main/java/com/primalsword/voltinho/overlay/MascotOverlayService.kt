package com.primalsword.voltinho.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.primalsword.voltinho.MainActivity
import com.primalsword.voltinho.R
import com.primalsword.voltinho.data.AppPreferences
import com.primalsword.voltinho.model.BatterySnapshot
import kotlin.math.abs

class MascotOverlayService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var windowManager: WindowManager
    private lateinit var preferences: AppPreferences
    private var mascotView: MascotView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mascotView?.snapshot = if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                BatterySnapshot.from(intent)
            } else {
                currentBatterySnapshot()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences(this)
        preferences.registerListener(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            preferences.overlayEnabled = false
            stopSelf()
            return START_NOT_STICKY
        }

        if (!Settings.canDrawOverlays(this)) {
            preferences.overlayEnabled = false
            stopSelf()
            return START_NOT_STICKY
        }

        startAsForeground()
        preferences.overlayEnabled = true
        if (mascotView == null) addOverlay()
        registerBatteryReceiverSafely()
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(batteryReceiver) }
        preferences.unregisterListener(this)
        mascotView?.let { view -> runCatching { windowManager.removeView(view) } }
        mascotView = null
        layoutParams = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppPreferences.KEY_MASCOT,
            AppPreferences.KEY_SIZE_DP,
            AppPreferences.KEY_OPACITY,
            AppPreferences.KEY_SHOW_PERCENTAGE -> applyPreferences()

            AppPreferences.KEY_OVERLAY_ENABLED -> if (!preferences.overlayEnabled) stopSelf()
        }
    }

    private fun addOverlay() {
        val sizePx = dp(preferences.mascotSizeDp)
        val metrics = resources.displayMetrics
        val defaultX = (metrics.widthPixels - sizePx - dp(6)).coerceAtLeast(0)

        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = preferences.overlayX.takeUnless { it == Int.MIN_VALUE } ?: defaultX
            y = preferences.overlayY
        }

        val view = MascotView(this).apply {
            alpha = preferences.opacity
            mascotKind = preferences.mascotKind
            showPercentage = preferences.showPercentage
            snapshot = currentBatterySnapshot()
            contentDescription = "Mascote da bateria. Toque para abrir o Voltinho."
            setOnTouchListener(DragTouchListener(params))
        }

        windowManager.addView(view, params)
        mascotView = view
        layoutParams = params
    }

    private fun applyPreferences() {
        val view = mascotView ?: return
        val params = layoutParams ?: return
        view.mascotKind = preferences.mascotKind
        view.showPercentage = preferences.showPercentage
        view.alpha = preferences.opacity

        val sizePx = dp(preferences.mascotSizeDp)
        if (params.width != sizePx || params.height != sizePx) {
            params.width = sizePx
            params.height = sizePx
            windowManager.updateViewLayout(view, params)
        }
    }

    private fun currentBatterySnapshot(): BatterySnapshot {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return BatterySnapshot.from(intent)
    }

    private fun registerBatteryReceiverSafely() {
        runCatching { unregisterReceiver(batteryReceiver) }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(batteryReceiver, filter)
        }
    }

    private fun startAsForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val stopIntent = Intent(this, MascotOverlayService::class.java).setAction(ACTION_STOP)
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText("Toque para personalizar o mascote.")
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, getString(R.string.notification_stop), stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private inner class DragTouchListener(
        private val params: WindowManager.LayoutParams,
    ) : View.OnTouchListener {
        private var startRawX = 0f
        private var startRawY = 0f
        private var startX = 0
        private var startY = 0
        private var moved = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startRawX = event.rawX
                    startRawY = event.rawY
                    startX = params.x
                    startY = params.y
                    moved = false
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - startRawX).toInt()
                    val dy = (event.rawY - startRawY).toInt()
                    moved = moved || abs(dx) > dp(3) || abs(dy) > dp(3)

                    val maxX = (resources.displayMetrics.widthPixels - params.width).coerceAtLeast(0)
                    val maxY = (resources.displayMetrics.heightPixels - params.height).coerceAtLeast(0)
                    params.x = (startX + dx).coerceIn(0, maxX)
                    params.y = (startY + dy).coerceIn(0, maxY)
                    windowManager.updateViewLayout(view, params)
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (moved) {
                        preferences.overlayX = params.x
                        preferences.overlayY = params.y
                    } else {
                        openMainActivity()
                    }
                    view.performClick()
                    return true
                }
            }
            return false
        }
    }

    companion object {
        private const val CHANNEL_ID = "voltinho_overlay"
        private const val NOTIFICATION_ID = 3401
        private const val ACTION_STOP = "com.primalsword.voltinho.action.STOP"

        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            val intent = Intent(context, MascotOverlayService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MascotOverlayService::class.java))
            AppPreferences(context).overlayEnabled = false
        }

        fun overlaySettingsIntent(context: Context): Intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )
    }
}
