package com.alaa.presentation.azan

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.R
import com.alaa.service.PrayerAlarmService
import com.alaa.ui.theme.AlaAppTheme
import com.alaa.ui.theme.Gold
import com.alaa.utils.Constants
import kotlinx.coroutines.delay

class AzanFullScreenActivity : ComponentActivity() {

    private val azanDoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            moveTaskToBack(true)
            finish()
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestOverlayPermissionIfNeeded()
        requestBatteryOptimizationExemption()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
                .requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val prayerName = intent.getStringExtra(Constants.PRAYER_NAME_KEY) ?: "الصلاة"
        playAzan(prayerName)

        setContent {
            AlaAppTheme {
                AzanFullScreenContent(
                    prayerName = prayerName,
                    onStop = {
                        stopAzan()
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }

    private fun playAzan(prayerName: String) {
        val i = Intent(this, PrayerAlarmService::class.java).apply {
            putExtra(Constants.PRAYER_NAME_KEY, prayerName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(i)
        else
            startService(i)
    }

    private fun stopAzan() {
        startService(Intent(this, PrayerAlarmService::class.java).apply {
            action = Constants.ACTION_STOP_AZAN
        })
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        runCatching {
            registerReceiver(azanDoneReceiver, IntentFilter(Constants.ACTION_STOP_AZAN))
        }
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        runCatching { unregisterReceiver(azanDoneReceiver) }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context, prayerName: String) =
            Intent(context, AzanFullScreenActivity::class.java).apply {
                putExtra(Constants.PRAYER_NAME_KEY, prayerName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
    }

    private fun requestOverlayPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }
}

@Composable
fun AzanFullScreenContent(prayerName: String, onStop: () -> Unit) {

    val images = listOf(
        R.drawable.mosque_bg,
        R.drawable.mosque_bg1,
        R.drawable.mosque_bg2,
        R.drawable.mosque_bg3,
        R.drawable.mosque_bg4,
        R.drawable.mosque_bg5,
        R.drawable.father_bg,
        R.drawable.father_bg1,
        R.drawable.father_bg2,
        R.drawable.father_bg3,
        R.drawable.father_bg4,
        R.drawable.father_bg5,
        R.drawable.father_bg6,
        R.drawable.father_bg7,
    )

    var currentIndex by remember { mutableStateOf(0) }
    var nextIndex by remember { mutableStateOf(1) }
    var crossfadeAlpha by remember { mutableStateOf(0f) }
    var slideFromLeft by remember { mutableStateOf(true) }

    val animCrossfade by animateFloatAsState(
        targetValue = crossfadeAlpha,
        animationSpec = tween(6000),
        label = "crossfade"
    )

    val imageOffsetX by animateIntAsState(
        targetValue = if (crossfadeAlpha == 1f) 0 else if (slideFromLeft) 300 else -300,
        animationSpec = tween(3000),
        label = "imageSlide"
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(11000)
            slideFromLeft = !slideFromLeft
            crossfadeAlpha = 1f
            delay(3000)
            currentIndex = nextIndex
            nextIndex = (nextIndex + 1) % images.size
            crossfadeAlpha = 0f
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val azanLines = listOf(
        "اللهُ أَكْبَر، اللهُ أَكْبَر",
        "اللهُ أَكْبَر، اللهُ أَكْبَر",
        "أَشْهَدُ أَن لَّا إِلَٰهَ إِلَّا اللَّه",
        "أَشْهَدُ أَن لَّا إِلَٰهَ إِلَّا اللَّه",
        "أَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ اللَّه",
        "أَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ اللَّه",
        "حَيَّ عَلَى الصَّلَاة",
        "حَيَّ عَلَى الصَّلَاة",
        "حَيَّ عَلَى الْفَلَاح",
        "حَيَّ عَلَى الْفَلَاح",
        "اللهُ أَكْبَر، اللهُ أَكْبَر",
        "لَا إِلَٰهَ إِلَّا اللَّه"
    )

    var currentLineIndex by remember { mutableStateOf(0) }
    var slideUp by remember { mutableStateOf(true) }
    var lineVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            lineVisible = false
            delay(400)
            currentLineIndex = (currentLineIndex + 1) % azanLines.size
            slideUp = !slideUp
            lineVisible = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(images[currentIndex]),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Image(
            painter = painterResource(images[nextIndex]),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = imageOffsetX.dp)
                .alpha(animCrossfade)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x66000000), Color(0xBB000000), Color(0xEE000000))
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .background(Color(0xFFC9A84C).copy(alpha = glowAlpha * 0.12f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(130.dp)
                    .background(Color(0x33C9A84C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🕌", fontSize = 60.sp)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "أذان $prayerName",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = lineVisible,
                enter = if (slideUp)
                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(600)) + fadeIn(tween(600))
                else
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(600)) + fadeIn(tween(600)),
                exit = if (slideUp)
                    slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(400)) + fadeOut(tween(400))
                else
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(tween(400))
            ) {
                Text(
                    azanLines[currentLineIndex],
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا",
                fontSize = 13.sp,
                color = Color(0xFFB0BEC5),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(56.dp))

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                modifier = Modifier.fillMaxWidth(0.6f).height(52.dp),
                shape = CircleShape
            ) {
                Text("إيقاف الأذان", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
