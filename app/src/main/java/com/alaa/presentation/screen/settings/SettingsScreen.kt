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
import com.alaa.ui.theme.GoldLight
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.alaa.R

private val GoldDim       = Color(0xFF8B6914).copy(alpha = 0.4f)
private val TextPrimary   = Color(0xFFE8F4F0)
private val TextSecondary = Color(0xFF9CA3AF)
private val CardDark      = Color(0xFF0D1F3A)

data class QariOption(val key: String, val nameAr: String)

val qariList = listOf(
    QariOption("silent", "🔇 صامت"),
    QariOption("makkah",               "أذان مكة المكرمة"),
    QariOption("mishary_alafasi",      "مشاري راشد العفاسي"),
    QariOption("abed_albaset",         "عبد الباسط عبد الصمد"),
    QariOption("al_hosary",            "محمود خليل الحصري"),
    QariOption("al_nakshabandy",       "مصطفى النقشبندي"),
    QariOption("mansoor_al_zahrani",   "منصور الزهراني"),
    QariOption("mohamed_refat",        "محمد رفعت"),
    QariOption("mohammed_almenshawy", "محمد المنشاوي"),
    QariOption("nasser_alqatami",      "ناصر القطامي"),
    QariOption("suhaib_khatba",        "شعيب خطبة")
)

data class PrayerAdhanRow(val key: String, val nameAr: String, val nameEn: String)
val prayerAdhanRows = listOf(
    PrayerAdhanRow("Fajr",    "الفجر",  "Fajr"),
    PrayerAdhanRow("Dhuhr",   "الظهر",  "Dhuhr"),
    PrayerAdhanRow("Asr",     "العصر",  "Asr"),
    PrayerAdhanRow("Maghrib", "المغرب", "Maghrib"),
    PrayerAdhanRow("Isha",    "العشاء", "Isha")
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

    var selectedMethod by remember { mutableStateOf(prefs.getString("calcMethod", "Makkah") ?: "Makkah") }
    var asrHanafi      by remember { mutableStateOf(prefs.getBoolean("asrHanafi", false)) }
    var showMethodMenu by remember { mutableStateOf(false) }

    val prayerAzanState = remember {
        prayerAdhanRows.associate { p ->
            p.key to mutableStateOf(prefs.getString("prayerAdhan_${p.key}", "makkah") ?: "makkah")
        }
    }
    val expandedMenuState = remember {
        prayerAdhanRows.associate { p -> p.key to mutableStateOf(false) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBg, DarkBg2))),
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
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("🎙️ أذان كل صلاة", fontSize = 13.sp, color = Gold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

                prayerAdhanRows.forEach { prayer ->
                    val selectedKey = prayerAzanState[prayer.key]!!
                    val expanded    = expandedMenuState[prayer.key]!!
                    val currentName = qariList.find { it.key == selectedKey.value }?.nameAr ?: "أذان مكة"

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .border(1.dp, GoldDim, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(prayer.nameAr, fontSize = 15.sp, color = Gold, fontWeight = FontWeight.Bold)
                            Text(prayer.nameEn, fontSize = 10.sp, color = TextSecondary)
                        }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22064E3B))
                                    .clickable { expanded.value = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(currentName, fontSize = 12.sp, color = GoldLight, fontWeight = FontWeight.Medium)
                                Text("›", fontSize = 16.sp, color = Gold)
                            }
                            DropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false },
                                modifier = Modifier.background(CardDark)
                            ) {
                                qariList.forEach { qari ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(qari.nameAr, fontSize = 13.sp,
                                                color = if (qari.key == selectedKey.value) Gold else TextPrimary,
                                                fontWeight = if (qari.key == selectedKey.value) FontWeight.Bold else FontWeight.Normal)
                                        },
                                        onClick = {
                                            selectedKey.value = qari.key
                                            prefs.edit().putString("prayerAdhan_${prayer.key}", qari.key).apply()
                                            expanded.value = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("🕌 حساب مواقيت الصلاة", fontSize = 13.sp, color = Gold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .border(1.dp, GoldDim, RoundedCornerShape(12.dp))
                            .clickable { showMethodMenu = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("طريقة الحساب", fontSize = 14.sp, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(calcMethods.find { it.first == selectedMethod }?.second ?: selectedMethod,
                                fontSize = 14.sp, color = Gold, fontWeight = FontWeight.Medium)
                            Text(" ›", fontSize = 18.sp, color = Gold)
                        }
                    }
                    DropdownMenu(
                        expanded = showMethodMenu,
                        onDismissRequest = { showMethodMenu = false },
                        modifier = Modifier.background(CardDark)
                    ) {
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

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("وقت العصر الحنفي", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("يأخذ بالرأي الحنفي في حساب وقت العصر", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = asrHanafi,
                        onCheckedChange = {
                            asrHanafi = it
                            prefs.edit().putBoolean("asrHanafi", it).apply()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Gold, checkedTrackColor = GoldDim)
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape  = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Image(
    painter = painterResource(id = R.drawable.father_bg),
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier
        .size(120.dp)
        .clip(CircleShape)
        .border(2.dp, Gold, CircleShape)
)
                    Spacer(Modifier.height(8.dp))
                    Text("Salat Muslim Pro", fontSize = 18.sp, color = Gold, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("إهداءً إلى روح", fontSize = 13.sp, color = TextSecondary)
                    Text("محمد عبد العظيم الطويل", fontSize = 15.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                    Text("رحمه الله ورضي عنه", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = GoldDim)
                    Spacer(Modifier.height(8.dp))
                    Text("الإصدار 1.0.0", fontSize = 12.sp, color = TextSecondary)
                    Text("com.alaa", fontSize = 11.sp, color = Color(0xFF4B5563))
                }
            }
        }
    }
}

