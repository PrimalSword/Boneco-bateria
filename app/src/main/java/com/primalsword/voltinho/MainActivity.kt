package com.primalsword.voltinho

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.primalsword.voltinho.data.AppPreferences
import com.primalsword.voltinho.model.BatteryMood
import com.primalsword.voltinho.model.BatterySnapshot
import com.primalsword.voltinho.model.MascotKind
import com.primalsword.voltinho.overlay.MascotOverlayService
import com.primalsword.voltinho.overlay.MascotView
import com.primalsword.voltinho.ui.theme.Lime
import com.primalsword.voltinho.ui.theme.Navy
import com.primalsword.voltinho.ui.theme.VoltinhoTheme

class MainActivity : ComponentActivity() {
    private var pendingEnableAfterPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VoltinhoTheme {
                VoltinhoScreen(
                    onRequestOverlayPermission = {
                        pendingEnableAfterPermission = true
                        runCatching { startActivity(MascotOverlayService.overlaySettingsIntent(this)) }
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (pendingEnableAfterPermission && Settings.canDrawOverlays(this)) {
            pendingEnableAfterPermission = false
            AppPreferences(this).overlayEnabled = true
            MascotOverlayService.start(this)
            recreate()
        }
    }
}

@Composable
private fun VoltinhoScreen(onRequestOverlayPermission: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val battery = rememberBatterySnapshot()

    var overlayEnabled by remember { mutableStateOf(preferences.overlayEnabled && Settings.canDrawOverlays(context)) }
    var mascot by remember { mutableStateOf(preferences.mascotKind) }
    var sizeDp by remember { mutableIntStateOf(preferences.mascotSizeDp) }
    var opacity by remember { mutableFloatStateOf(preferences.opacity) }
    var showPercentage by remember { mutableStateOf(preferences.showPercentage) }
    var startOnBoot by remember { mutableStateOf(preferences.startOnBoot) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { },
    )

    LaunchedEffect(Unit) {
        overlayEnabled = preferences.overlayEnabled && Settings.canDrawOverlays(context)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            HeroSection(
                battery = battery,
                mascot = mascot,
                showPercentage = showPercentage,
            )

            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                StatusCard(overlayEnabled = overlayEnabled)

                SectionCard(title = "Escolha seu mascote") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MascotKind.entries.forEach { option ->
                            FilterChip(
                                selected = mascot == option,
                                onClick = {
                                    mascot = option
                                    preferences.mascotKind = option
                                },
                                label = { Text(option.displayName) },
                            )
                        }
                    }
                    Text(
                        text = "Pingo, Byte e Mimo são mascotes originais desenhados e animados por código.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                }

                SectionCard(title = "Ajuste fino") {
                    SettingSlider(
                        label = "Tamanho",
                        valueText = "$sizeDp dp",
                        value = sizeDp.toFloat(),
                        valueRange = 56f..132f,
                        onValueChange = {
                            sizeDp = it.toInt()
                            preferences.mascotSizeDp = sizeDp
                        },
                    )
                    SettingSlider(
                        label = "Opacidade",
                        valueText = "${(opacity * 100).toInt()}%",
                        value = opacity,
                        valueRange = 0.45f..1f,
                        onValueChange = {
                            opacity = it
                            preferences.opacity = it
                        },
                    )
                    SettingSwitch(
                        title = "Mostrar porcentagem",
                        subtitle = "Exibe o nível abaixo do mascote.",
                        checked = showPercentage,
                        onCheckedChange = {
                            showPercentage = it
                            preferences.showPercentage = it
                        },
                    )
                    HorizontalDivider()
                    SettingSwitch(
                        title = "Iniciar com o celular",
                        subtitle = "Reativa o mascote após reiniciar.",
                        checked = startOnBoot,
                        onCheckedChange = {
                            startOnBoot = it
                            preferences.startOnBoot = it
                        },
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (overlayEnabled) MaterialTheme.colorScheme.error else Lime,
                        contentColor = if (overlayEnabled) Color.White else Navy,
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    onClick = {
                        if (overlayEnabled) {
                            MascotOverlayService.stop(context)
                            overlayEnabled = false
                        } else {
                            if (!Settings.canDrawOverlays(context)) {
                                onRequestOverlayPermission()
                                return@Button
                            }

                            if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }

                            preferences.overlayEnabled = true
                            MascotOverlayService.start(context)
                            overlayEnabled = true
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (overlayEnabled) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (overlayEnabled) "Desativar mascote" else "Ativar mascote",
                        fontWeight = FontWeight.Black,
                    )
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRequestOverlayPermission,
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Abrir permissão de sobreposição")
                }

                Text(
                    text = "O Voltinho não coleta dados, não usa internet e não lê o conteúdo de outros aplicativos. A permissão de sobreposição serve exclusivamente para desenhar o mascote na tela.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.66f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    battery: BatterySnapshot,
    mascot: MascotKind,
    showPercentage: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy, Color(0xFF132641)),
                ),
            )
            .padding(top = 28.dp, bottom = 26.dp, start = 22.dp, end = 22.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = Lime,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "VOLTINHO",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Sua bateria ganhou personalidade.",
                color = Color.White,
                fontSize = 28.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    AnimatedContent(targetState = battery.level, label = "battery-level") { level ->
                        Text(
                            text = "$level%",
                            color = Lime,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Text(
                        text = battery.mood.label,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }

                AndroidView(
                    modifier = Modifier.size(122.dp),
                    factory = { MascotView(it) },
                    update = { view ->
                        view.snapshot = battery
                        view.mascotKind = mascot
                        view.showPercentage = showPercentage
                    },
                )
            }
        }
    }
}

@Composable
private fun StatusCard(overlayEnabled: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (overlayEnabled) Lime.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (overlayEnabled) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (overlayEnabled) Color(0xFF4F8A00) else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (overlayEnabled) "Mascote ativo" else "Mascote desativado",
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (overlayEnabled) "Arraste o bonequinho para qualquer canto da tela." else "Ative para colocar o bonequinho perto da bateria.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp)
            content()
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(valueText, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun rememberBatterySnapshot(): BatterySnapshot {
    val context = LocalContext.current
    var snapshot by remember {
        mutableStateOf(
            BatterySnapshot.from(
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)),
            ),
        )
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                snapshot = BatterySnapshot.from(intent)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    return snapshot
}

private val BatteryMood.label: String
    get() = when (this) {
        BatteryMood.CELEBRATING -> "Carga completa. Festa liberada."
        BatteryMood.CHARGING -> "Recarregando as energias."
        BatteryMood.ENERGETIC -> "Cheio de disposição."
        BatteryMood.CONTENT -> "Tudo sob controle."
        BatteryMood.TIRED -> "Começando a cansar."
        BatteryMood.CRITICAL -> "Cadê o carregador?"
    }
