package com.alaa.presentation.dhikr

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import com.alaa.ui.theme.GoldLight
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DhikrScreen(viewModel: DhikrViewModel = koinViewModel()) {
    val state  by viewModel.uiState.collectAsState()
    val ctx    = LocalContext.current
    val lcOwner = LocalLifecycleOwner.current

    var showStartPicker by remember { mutableStateOf(false) }
    var showStopPicker  by remember { mutableStateOf(false) }
    val startState = rememberTimePickerState(state.startHour, state.startMinute, false)
    val stopState  = rememberTimePickerState(state.stopHour,  state.stopMinute,  false)

    DisposableEffect(lcOwner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) viewModel.syncServiceState(ctx)
        }
        lcOwner.lifecycle.addObserver(obs)
        onDispose { lcOwner.lifecycle.removeObserver(obs) }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    // ─── Dialogs ─────────────────────────────────────────────────────────────
    if (showStartPicker) TimePickerDialog("وقت بدء الأذكار", startState,
        onConfirm = { viewModel.setStartTime(startState.hour, startState.minute, ctx); showStartPicker = false },
        onDismiss = { showStartPicker = false })

    if (showStopPicker) TimePickerDialog("وقت إيقاف الأذكار", stopState,
        onConfirm = { viewModel.setStopTime(stopState.hour, stopState.minute, ctx); showStopPicker = false },
        onDismiss = { showStopPicker = false })

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
    ) {
        // ─── Header ──────────────────────────────────────────────────────────
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkBg2, DarkBg)))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📿", fontSize = 40.sp)
                Spacer(Modifier.height(6.dp))
                Text("أذكاري", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gold)
                Text("تُشغِّل الأذكار بالتسلسل ثم تعيد من الأول",
                    fontSize = 12.sp, color = Color.White.copy(0.6f))
            }
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ─── Dhikr list ────────────────────────────────────────────────
            items(state.dhikrList) { item ->
                val selected = item.id == state.selectedDhikr.id
                val bg by animateColorAsState(
                    if (selected) Gold.copy(0.15f) else DarkBg2, label = "bg"
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable(enabled = !state.isRunning) { viewModel.selectDhikr(item) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selected) {
                        Box(
                            Modifier.size(22.dp).background(Gold, CircleShape),
                            Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(item.textAr,
                        color = if (selected) Gold else Color.White.copy(0.85f),
                        fontSize = 16.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // ─── Repeat single ─────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(DarkBg2).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("تكرار الذكر المحدد فقط", color = Color.White, fontSize = 15.sp)
                        Text(
                            if (state.repeatSingle) "يكرر: ${state.selectedDhikr.textAr}"
                            else "يشغل كل الأذكار بالتسلسل",
                            color = Color.White.copy(0.5f), fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.repeatSingle,
                        onCheckedChange = { if (!state.isRunning) viewModel.setRepeatSingle(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Gold)
                    )
                }
            }

            // ─── Interval picker ───────────────────────────────────────────
            item {
                Text("الفترة بين كل ذكر: ${state.intervalMin} دقيقة",
                    color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1 د" to 1, "5 د" to 5, "10 د" to 10, "15 د" to 15,
                           "20 د" to 20, "30 د" to 30, "45 د" to 45, "60 د" to 60,
                           "90 د" to 90, "120 د" to 120).forEach { (label, min) ->
                        val isSel = state.intervalMin == min
                        Button(
                            onClick = { if (!state.isRunning) viewModel.setInterval(min) },
                            enabled = !state.isRunning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Gold else DarkBg2,
                                contentColor   = if (isSel) Color.Black else Gold,
                                disabledContainerColor = if (isSel) Gold.copy(0.6f) else DarkBg2,
                                disabledContentColor   = if (isSel) Color.Black else Gold.copy(0.4f)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.height(40.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // ─── Volume ────────────────────────────────────────────────────
            item {
                Text("مستوى الصوت: ${(state.volume * 100).roundToInt()}%",
                    color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = state.volume,
                    onValueChange = { viewModel.setVolume(it, ctx) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = DarkBg2)
                )
            }

            // ─── Auto schedule ─────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(DarkBg2).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تشغيل تلقائي يومي", color = Color.White, fontSize = 15.sp)
                    Switch(
                        checked = state.autoEnabled,
                        onCheckedChange = { viewModel.setAutoEnabled(it, ctx) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Gold)
                    )
                }
                if (state.autoEnabled) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        TimeButton("بداية", state.startHour, state.startMinute, Modifier.weight(1f)) { showStartPicker = true }
                        TimeButton("نهاية", state.stopHour, state.stopMinute, Modifier.weight(1f)) { showStopPicker = true }
                    }
                }
            }

            // ─── Start / Stop button ───────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { if (state.isRunning) viewModel.stop(ctx) else viewModel.start(ctx) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRunning) Color(0xFFB00020) else Gold
                    ),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (state.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        null, tint = Color.Black, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isRunning) "إيقاف الأذكار" else "ابدأ الأذكار",
                        color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                if (state.isRunning) {
                    Spacer(Modifier.height(8.dp))
                    Text("✅ الأذكار تعمل في الخلفية",
                        color = Color(0xFF4CAF50), fontSize = 13.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun TimeButton(label: String, hour: Int, minute: Int, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = DarkBg2),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(56.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Gold, fontSize = 12.sp)
            Text("%d:%02d %s".format(if (hour % 12 == 0) 12 else hour % 12, minute, if (hour < 12) "ص" else "م"),
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    state: TimePickerState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF0D1F3A),
        title = { Text(title, color = Gold, fontWeight = FontWeight.Bold) },
        text  = { TimePicker(state = state, colors = TimePickerDefaults.colors(
            clockDialColor = DarkBg, selectorColor = Gold,
            timeSelectorSelectedContainerColor = Gold,
            timeSelectorUnselectedContainerColor = DarkBg2,
            timeSelectorSelectedContentColor = Color.Black,
            timeSelectorUnselectedContentColor = Color.White
        )) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("حفظ", color = Gold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) } }
    )
}
