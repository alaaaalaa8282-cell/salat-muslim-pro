package com.alaa.presentation.challenges

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.data.prefs.PrefsManager
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import org.koin.compose.koinInject

// ─── Medal data ───────────────────────────────────────────────────────────────
private data class Medal(val id: String, val icon: String, val name: String, val req: String, val pts: Int)
private val medals = listOf(
    Medal("first_fajr",   "🌅", "أول فجر",      "حضور أول صلاة فجر",    20),
    Medal("fajr7",        "⭐", "7 فجرات",       "7 صلوات فجر متتالية",  30),
    Medal("fajr30",       "🏆", "الفجر الكامل", "30 صلاة فجر",          100),
    Medal("first_sadaqa", "💛", "أول صدقة",     "أول صدقة يومية",       15),
    Medal("sadaqa7",      "💎", "أسبوع صدقة",   "7 أيام صدقة",          25),
    Medal("sadaqa30",     "👑", "الصدقة الكاملة","30 يوم صدقة",         80),
    Medal("surah_half",   "📖", "نصف السورة",   "3 مراحل من السورة",    20),
    Medal("surah_full",   "📕", "السورة كاملة", "إتمام السورة كاملة",   50),
    Medal("pts100",       "🥉", "100 نقطة",     "اجمع 100 نقطة",        10),
    Medal("pts300",       "🥈", "300 نقطة",     "اجمع 300 نقطة",        20),
    Medal("pts500",       "🥇", "500 نقطة",     "اجمع 500 نقطة",        30),
    Medal("all3",         "🌟", "البطل الكامل", "أتمم كل التحديات",     200),
)

private val surahStages = listOf("حفظ ×1", "حفظ ×2", "حفظ ×3", "تلاوة", "مراجعة", "إتقان")

@Composable
fun ChallengesScreen(prefs: PrefsManager = koinInject()) {
    // Persist state via prefs
    var fajr   by remember { mutableStateOf(prefs.challengesFajr) }
    var points by remember { mutableStateOf(prefs.challengesPoints) }

    val sadaqaBits by remember {
        mutableStateOf(prefs.challengesSadaqa.padEnd(30, '0').map { it == '1' }.toMutableList())
    }
    val sadaqa = remember { sadaqaBits.toMutableStateList() }

    val surahBits by remember {
        mutableStateOf(prefs.challengesSurah.padEnd(6, '0').map { it == '1' }.toMutableList())
    }
    val surah = remember { surahBits.toMutableStateList() }

    fun save() {
        prefs.challengesFajr   = fajr
        prefs.challengesPoints = points
        prefs.challengesSadaqa = sadaqa.joinToString("") { if (it) "1" else "0" }
        prefs.challengesSurah  = surah.joinToString("") { if (it) "1" else "0" }
    }

    fun unlockedMedals(): Set<String> {
        val sadaqaCnt  = sadaqa.count { it }
        val surahDone  = surah.count { it }
        return buildSet {
            if (fajr >= 1)    add("first_fajr")
            if (fajr >= 7)    add("fajr7")
            if (fajr >= 30)   add("fajr30")
            if (sadaqaCnt >= 1) add("first_sadaqa")
            if (sadaqaCnt >= 7) add("sadaqa7")
            if (sadaqaCnt >= 30) add("sadaqa30")
            if (surahDone >= 3) add("surah_half")
            if (surahDone >= 6) add("surah_full")
            if (points >= 100) add("pts100")
            if (points >= 300) add("pts300")
            if (points >= 500) add("pts500")
            if (fajr >= 30 && sadaqaCnt >= 30 && surahDone >= 6) add("all3")
        }
    }

    val unlocked = unlockedMedals()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("التحديات", "الميداليات")

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // ─── Header ──────────────────────────────────────────────────────────
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkBg2, DarkBg)))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", fontSize = 40.sp)
                Spacer(Modifier.height(4.dp))
                Text("تحديات إسلامية 30 يوم", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Gold.copy(0.15f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("⭐ $points نقطة", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // ─── Tabs ─────────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = DarkBg2,
            contentColor     = Gold
        ) {
            tabs.forEachIndexed { i, t ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i },
                    text = { Text(t, fontWeight = FontWeight.Bold) })
            }
        }

        // ─── Content ──────────────────────────────────────────────────────────
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (selectedTab == 0) {
                // ─── Fajr challenge ────────────────────────────────────────
                ChallengeCard(
                    icon  = "🌅",
                    title = "صلاة الفجر في جماعة",
                    desc  = "حضور صلاة الفجر 30 يومًا متتاليًا"
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { if (fajr > 0) { fajr--; points = (points - 10).coerceAtLeast(0); save() } },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBg2, contentColor = Color.White),
                            shape  = RoundedCornerShape(30.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) { Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(16.dp))
                        Box(
                            Modifier.size(80.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                .border(3.dp, Color(0xFF00C896), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$fajr", color = Color(0xFF00C896), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Button(
                            onClick = { if (fajr < 30) { fajr++; points += 10; save() } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C896), contentColor = Color.White),
                            shape  = RoundedCornerShape(30.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.height(10.dp))
                    ProgressBar(fajr / 30f, "$fajr / 30")
                }

                Spacer(Modifier.height(12.dp))

                // ─── Sadaqa grid ───────────────────────────────────────────
                ChallengeCard(icon = "💛", title = "الصدقة اليومية", desc = "تصدّق 30 يومًا في هذا الشهر") {
                    val cnt = sadaqa.count { it }
                    ProgressBar(cnt / 30f, "$cnt / 30")
                    Spacer(Modifier.height(10.dp))
                    // 6x5 grid
                    (0 until 5).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            (0 until 6).forEach { col ->
                                val idx = row * 6 + col
                                val checked = sadaqa[idx]
                                Box(
                                    modifier = Modifier.weight(1f).aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (checked) Color(0xFF00695C) else DarkBg)
                                        .border(1.dp, if (checked) Color(0xFF00C896) else Color.White.copy(0.1f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            sadaqa[idx] = !sadaqa[idx]
                                            points += if (sadaqa[idx]) 5 else { points = (points - 5).coerceAtLeast(0); 0 }
                                            save()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (checked) "✓" else "${idx + 1}",
                                        color = if (checked) Color.White else Color.White.copy(0.4f),
                                        fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ─── Surah stages ──────────────────────────────────────────
                ChallengeCard(icon = "📖", title = "حفظ سورة الملك", desc = "اجتاز 6 مراحل من الحفظ والمراجعة") {
                    val done = surah.count { it }
                    ProgressBar(done / 6f, "$done / 6")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        surahStages.forEachIndexed { i, stage ->
                            Button(
                                onClick = {
                                    surah[i] = !surah[i]
                                    points += if (surah[i]) 9 else { points = (points - 9).coerceAtLeast(0); 0 }
                                    save()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (surah[i]) Color(0xFF00695C) else DarkBg,
                                    contentColor   = Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(
                                        listOf(if (surah[i]) Color(0xFF00C896) else Color.White.copy(0.15f),
                                               if (surah[i]) Color(0xFF00C896) else Color.White.copy(0.15f))
                                    )
                                ),
                                shape  = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) { Text(stage, fontSize = 9.sp, textAlign = TextAlign.Center) }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ─── Reset ─────────────────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        fajr   = 0; points = 0
                        sadaqa.fill(false); surah.fill(false)
                        prefs.challengesFajr   = 0
                        prefs.challengesPoints = 0
                        prefs.challengesSadaqa = ""
                        prefs.challengesSurah  = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("إعادة تعيين الكل", fontWeight = FontWeight.Bold) }

            } else {
                // ─── Medals ────────────────────────────────────────────────
                Text("ميدالياتك", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp))
                medals.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { medal ->
                            val isUnlocked = medal.id in unlocked
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUnlocked) Color(0xFF1A2A1A) else DarkBg2
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isUnlocked) BorderStroke(1.dp, Gold) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(if (isUnlocked) medal.icon else "🔒", fontSize = 28.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(medal.name, color = if (isUnlocked) Gold else Color.White.copy(0.5f),
                                        fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(2.dp))
                                    Text(medal.req, color = Color.White.copy(0.4f),
                                        fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
                                }
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(icon: String, title: String, desc: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBg2),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color.White.copy(0.5f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.weight(1f).height(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(0.08f))
        ) {
            Box(
                Modifier.fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF00C896), Gold)))
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White.copy(0.6f), fontSize = 12.sp)
    }
}
