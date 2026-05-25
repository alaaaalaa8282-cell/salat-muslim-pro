package com.alaa.presentation.screen.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.ui.theme.Gold

// ─── Local theme colors ───────────────────────────────────────────────────────
private val GoldLight    = Color(0xFFEDE0C4)
private val GoldDim      = Color(0xFF8B6914).copy(alpha = 0.4f)
private val TextPrimary  = Color(0xFFE8F4F0)
private val TextSecondary = Color(0xFF9CA3AF)
private val CardDark     = Color(0xFF0D1F3A)

data class QariOption(val key: String, val nameAr: String, val nameEn: String)

val qariList = listOf(
    QariOption("makkah",              "أذان مكة المكرمة",         "Makkah"),
    QariOption("mishary_alafasi",     "مشاري راشد العفاسي",       "Mishary Alafasi"),
    QariOption("abed_albaset",        "عبد الباسط عبد الصمد",     "Abed Albaset"),
    QariOption("al_hosary",           "محمود خليل الحصري",        "Al-Hosary"),
    QariOption("al_nakshabandy",      "مصطفى إسماعيل النقشبندي",  "Al-Nakshabandy"),
    QariOption("mansoor_al_zahrani",  "منصور الزهراني",           "Mansoor Al-Zahrani"),
    QariOption("mohamed_refat",       "محمد رفعت",                "Mohamed Refat"),
    QariOption("mohammed_almenshawy", "محمد صديق المنشاوي",       "Al-Menshawy"),
    QariOption("nasser_alqatami",     "ناصر القطامي",             "Nasser Al-Qatami"),
    QariOption("suhaib_khatba",       "شعيب خطبة",                "Suhaib Khatba")
)

val calcMethods = listOf(
    "Makkah" to "أم القرى — مكة",
    "Egypt"  to "الهيئة المصرية",
    "MWL"    to "رابطة العالم الإسلامي",
    "ISNA"   to "أمريكا الشمالية",
    "Kuwait" to "الكويت",
    "Gulf"   to "دول الخليج",
    "Qatar"  to "قطر",
    "Turkey" to "تركيا"
)

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)

    var selectedQari   by remember { mutableStateOf(prefs.getString("azan_qari",  "makkah") ?: "makkah") }
    var selectedMethod by remember { mutableStateOf(prefs.getString("calcMethod", "Makkah") ?: "Makkah") }
    var asrHanafi      by remember { mutableStateOf(prefs.getBoolean("asrHanafi", false)) }
    var showQariMenu   by remember { mutableStateOf(false) }
    var showMethodMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1628), Color(0xFF000000)))),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp)
            ) {
                Text("⚙️ الإعدادات", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gold)
                Text("Settings", fontSize = 10.sp, color = TextSecondary, letterSpacing = 2.sp)
            }
        }

        item {
            SettingSection(title = "🎙️ صوت الأذان") {
                val currentQari = qariList.find { it.key == selectedQari }
                Box {
                    SettingRow(label = "القارئ", value = currentQari?.nameAr ?: "أذان مكة",
                        onClick = { showQariMenu = true })
                    DropdownMenu(expanded = showQariMenu, onDismissRequest = { showQariMenu = false },
                        modifier = Modifier.background(CardDark)) {
                        qariList.forEach { qari ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(qari.nameAr, fontSize = 14.sp,
                                            color = if (qari.key == selectedQari) Gold else TextPrimary,
                                            fontWeight = if (qari.key == selectedQari) FontWeight.Bold else FontWeight.Normal)
                                        Text(qari.nameEn, fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    selectedQari = qari.key
                                    prefs.edit().putString("azan_qari", qari.key).apply()
                                    showQariMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingSection(title = "🕌 حساب مواقيت الصلاة") {
                Box {
                    SettingRow(
                        label   = "طريقة الحساب",
                        value   = calcMethods.find { it.first == selectedMethod }?.second ?: selectedMethod,
                        onClick = { showMethodMenu = true }
                    )
                    DropdownMenu(expanded = showMethodMenu, onDismissRequest = { showMethodMenu = false },
                        modifier = Modifier.background(CardDark)) {
                        calcMethods.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(label, fontSize = 14.sp,
                                        color = if (key == selectedMethod) Gold else TextPrimary,
                                        fontWeight = if (key == selectedMethod) FontWeight.Bold else FontWeight.Normal)
                                },
                                onClick = {
                                    selectedMethod = key
                                    prefs.edit().putString("calcMethod", key).apply()
                                    showMethodMenu = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(CardDark).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("وقت العصر الحنفي", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("يأخذ بالرأي الحنفي في حساب وقت العصر", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(checked = asrHanafi, onCheckedChange = {
                        asrHanafi = it
                        prefs.edit().putBoolean("asrHanafi", it).apply()
                    }, colors = SwitchDefaults.colors(checkedThumbColor = Gold, checkedTrackColor = GoldDim))
                }
            }
        }

        item {
            SettingSection(title = "ℹ️ عن التطبيق") {
                Card(colors = CardDefaults.cardColors(containerColor = CardDark),
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)) {
                        Text("🕌", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Salat Muslim Pro", fontSize = 18.sp, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("إهداءً إلى روح", fontSize = 13.sp, color = TextSecondary)
                        Text("محمد عبد العظيم الطويل", fontSize = 15.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                        Text("رحمه الله ورضي عنه", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = GoldDim)
                        Spacer(Modifier.height(12.dp))
                        Text("الإصدار 1.0.0", fontSize = 12.sp, color = TextSecondary)
                        Text("com.alaa", fontSize = 11.sp, color = Color(0xFF4B5563))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(title, fontSize = 13.sp, color = Gold, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    }
}

@Composable
fun SettingRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CardDark).border(1.dp, GoldDim, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(value, fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium)
            Text("›", fontSize = 18.sp, color = Gold)
        }
    }
}

