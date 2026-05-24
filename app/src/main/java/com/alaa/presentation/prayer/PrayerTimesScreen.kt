package com.alaa.presentation.prayer

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.data.model.PrayerData
import com.alaa.data.prefs.PrefsManager
import com.alaa.data.repository.PrayerRepository
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PrayerTimesScreen(
    prefs: PrefsManager     = koinInject(),
    repo:  PrayerRepository = koinInject()
) {
    var prayer by remember { mutableStateOf(PrayerData()) }
    val scope  = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            prayer = repo.getPrayerTimes(prefs.latitude, prefs.longitude)
        }
    }

    val rows = listOf(
        Triple("🌙", "الفجر",   prayer.fajr),
        Triple("🌄", "الشروق",  prayer.sunrise),
        Triple("☀️", "الظهر",   prayer.dhuhr),
        Triple("🌤️", "العصر",   prayer.asr),
        Triple("🌅", "المغرب",  prayer.maghrib),
        Triple("🌃", "العشاء",  prayer.isha),
    )

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkBg2, DarkBg)))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🕌", fontSize = 40.sp)
                Spacer(Modifier.height(4.dp))
                Text("مواقيت الصلاة", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (prayer.hijriDate.isNotEmpty())
                    Text(prayer.hijriDate, color = Color.White.copy(0.7f), fontSize = 13.sp, textAlign = TextAlign.Center)
                if (prayer.gregorianDate.isNotEmpty())
                    Text(prayer.gregorianDate, color = Color.White.copy(0.5f), fontSize = 12.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Gold.copy(0.1f)),
            shape  = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الصلاة القادمة", color = Gold.copy(0.8f), fontSize = 12.sp)
                Text(prayer.nextPrayerName, color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(prayer.nextPrayerTime, color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(prayer.countdown, color = Gold, fontSize = 28.sp,
                    fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { (icon, name, time) ->
                item {
                    val isNext = name == prayer.nextPrayerName
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNext) Gold.copy(0.1f) else DarkBg2)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(icon, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(name, color = if (isNext) Gold else Color.White,
                                fontSize = 16.sp,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
                        }
                        Text(time, color = if (isNext) Gold else Color.White.copy(0.8f),
                            fontSize = 16.sp,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}
