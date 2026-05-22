package com.alaa.presentation.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.alaa.navigation.Screen
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import com.alaa.ui.theme.GoldLight
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state   by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Location permission
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) viewModel.fetchLocation(context)
    }

    // Notification permission
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        viewModel.init(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        locationLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ─── Header with Islamic logo ───────────────────────────────────
        item { HomeHeader(cityName = state.cityName, onRefresh = { viewModel.fetchLocation(context) }) }

        // ─── Father memorial photo box ──────────────────────────────────
        item { FatherMemorialCard() }

        // ─── Countdown to next prayer ───────────────────────────────────
        item { NextPrayerCard(state = state) }

        // ─── Prayer times table ─────────────────────────────────────────
        item {
            PrayerTimesCard(
                state = state,
                onSeeAll = { navController.navigate(Screen.PrayerTimes.route) }
            )
        }

        // ─── Weather widget ─────────────────────────────────────────────
        item { WeatherCard(state = state) }

        // ─── Hijri date ─────────────────────────────────────────────────
        item { HijriDateCard(state = state) }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(cityName: String, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DarkBg2, DarkBg)))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // Mosque icon (use drawable or emoji fallback)
            Text("🕌", fontSize = 48.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(
                "Mohamed Abdelazim",
                color = Gold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Azan App", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Gold, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(cityName, color = Color.White.copy(0.7f), fontSize = 12.sp)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, null, tint = Gold, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─── Father memorial ─────────────────────────────────────────────────────────
@Composable
private fun FatherMemorialCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🤲", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "محمد عبد العظيم الطويل",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "رحمه الله ووسع له في قبره وأسكنه فسيح جناته",
                    color = Color.White.copy(0.75f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "اللهم اغفر له وارحمه وعافه واعف عنه",
                    color = Gold.copy(0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── Next prayer countdown ────────────────────────────────────────────────────
@Composable
private fun NextPrayerCard(state: HomeState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F3A)),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("الصلاة القادمة", color = Gold.copy(0.8f), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                state.prayerData.nextPrayerName,
                color = Gold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                state.prayerData.nextPrayerTime,
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(8.dp))
            // Countdown
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gold.copy(0.15f))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    state.prayerData.countdown,
                    color = Gold,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

// ─── Prayer times table ───────────────────────────────────────────────────────
@Composable
private fun PrayerTimesCard(state: HomeState, onSeeAll: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F3A)),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("مواقيت الصلاة", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onSeeAll) {
                    Text("عرض الكل", color = Gold.copy(0.7f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            val p = state.prayerData
            PrayerRow("🌙 الفجر",   p.fajr,    p.nextPrayerName == "الفجر")
            PrayerRow("☀️ الظهر",   p.dhuhr,   p.nextPrayerName == "الظهر")
            PrayerRow("🌤️ العصر",   p.asr,     p.nextPrayerName == "العصر")
            PrayerRow("🌅 المغرب",  p.maghrib,  p.nextPrayerName == "المغرب")
            PrayerRow("🌃 العشاء",  p.isha,    p.nextPrayerName == "العشاء")
        }
    }
}

@Composable
private fun PrayerRow(name: String, time: String, isNext: Boolean) {
    val bg = if (isNext) Gold.copy(0.1f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(name, color = if (isNext) Gold else Color.White.copy(0.8f), fontSize = 14.sp,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
        if (isNext) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Green, CircleShape)
            )
        }
        Text(time, color = if (isNext) Gold else Color.White, fontSize = 14.sp,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
    }
    if (!isNext) Divider(color = Color.White.copy(0.05f), thickness = 0.5.dp)
}

// ─── Weather card ─────────────────────────────────────────────────────────────
@Composable
private fun WeatherCard(state: HomeState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F3A)),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(state.weatherData.icon, fontSize = 36.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "${state.weatherData.temperature.toInt()}°C",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(state.weatherData.description, color = Color.White.copy(0.7f), fontSize = 13.sp)
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("💨 ${state.weatherData.windspeed.toInt()} km/h", color = Color.White.copy(0.6f), fontSize = 12.sp)
            }
        }
    }
}

// ─── Hijri date card ──────────────────────────────────────────────────────────
@Composable
private fun HijriDateCard(state: HomeState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1F3A)),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(state.prayerData.hijriDate, color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
            if (state.prayerData.gregorianDate.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(state.prayerData.gregorianDate, color = Color.White.copy(0.6f), fontSize = 12.sp,
                    textAlign = TextAlign.Center)
            }
        }
    }
}
