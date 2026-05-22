package com.alaa.presentation.mesbaha

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.data.prefs.PrefsManager
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import org.koin.compose.koinInject

val azkarTexts = listOf(
    "سبحان الله",
    "الحمد لله",
    "الله أكبر",
    "لا إله إلا الله",
    "سبحان الله وبحمده",
    "أستغفر الله",
    "لا حول ولا قوة إلا بالله",
    "الصلاة على النبي ﷺ",
)

@Composable
fun MesbahaScreen(prefs: PrefsManager = koinInject()) {
    var count by remember { mutableStateOf(prefs.mesbahaCount) }
    var selectedAzkar by remember { mutableStateOf(azkarTexts[0]) }
    var showConfirm by remember { mutableStateOf(false) }

    // Press animation
    val scale = remember { Animatable(1f) }
    val pressInteraction = remember { MutableInteractionSource() }

    // Confirm reset dialog
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = DarkBg2,
            title = { Text("تصفير العداد؟", color = Gold, fontWeight = FontWeight.Bold) },
            text  = { Text("هل أنت متأكد من تصفير السبحة؟", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    count = 0; prefs.mesbahaCount = 0; showConfirm = false
                }) { Text("نعم", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("إلغاء", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg),
        horizontalAlignment = Alignment.CenterHorizontally
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
                Spacer(Modifier.height(4.dp))
                Text("السبحة الإلكترونية", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(selectedAzkar, color = Color.White.copy(0.7f), fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── Azkar selector ─────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("اختر الذكر:", color = Gold.copy(0.8f), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            azkarTexts.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { azkar ->
                        val isSel = azkar == selectedAzkar
                        Button(
                            onClick = { selectedAzkar = azkar; count = 0; prefs.mesbahaCount = 0 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Gold else DarkBg2,
                                contentColor   = if (isSel) Color.Black else Gold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f).height(38.dp)
                        ) {
                            Text(azkar, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    // Fill empty slots
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        // ─── Counter display ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(DarkBg2, Color(0xFF1A1A2E)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(Gold.copy(0.1f), DarkBg2))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$count",
                        color = Gold,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (count > 0 && count % 33 == 0) {
                        Text("سبحان الله ×33", color = Color.Green, fontSize = 10.sp)
                    }
                    if (count > 0 && count % 100 == 0) {
                        Text("مئة ✨", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ─── Main tasbih button ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .scale(scale.value)
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Gold.copy(0.3f), Gold))
                )
                .clickable(
                    interactionSource = pressInteraction,
                    indication = null
                ) {
                    count++
                    prefs.mesbahaCount = count
                },
            contentAlignment = Alignment.Center
        ) {
            Text("اضغط", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        // ─── Reset button ─────────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { showConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(Color(0xFFE53935), Color(0xFFE53935)))
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("تصفير", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
