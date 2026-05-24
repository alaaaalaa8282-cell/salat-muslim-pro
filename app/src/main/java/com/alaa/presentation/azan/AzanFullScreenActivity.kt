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
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.WindowManager
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
import com.alaa.presentation.base.Constants
import com.alaa.presentation.service.DhikrService
import com.alaa.presentation.service.PrayerAlarmService
import com.alaa.presentation.theme.Gold
import com.alaa.presentation.theme.NoorTheme
import kotlinx.coroutines.delay

// ── خلفيات شاشة الأذان ────────────────────────────────────────────
private val bgImages = listOf(
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
    R.drawable.image_mosque_dark,
    R.drawable.night_mosque
)

class AzanFullScreenActivity : ComponentActivity() {

    private val azanDoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (PrayerAlarmService.isPlaying) return
            startService(Intent(this@AzanFullScreenActivity, DhikrService::class.java).apply {
                action = DhikrService.ACTION_RESUME_FOR_AZAN
            })
            moveTaskToBack(true)
            finish()
        }
    }

    private var telephonyManager: TelephonyManager? = null
    private var athanPlayed = false
    private val callTimeoutHandler = Handler(Looper.getMainLooper())

    @Suppress("DEPRECATION")
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_IDLE && !athanPlayed) {
                athanPlayed = true
                callTimeoutHandler.removeCallbacksAndMessages(null)
                val prayerName = intent.getStringExtra(Constants.PRAYER_NAME_KEY) ?: "الصلاة"
                playAzan(prayerName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true); setTurnScreenOn(true)
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
        val bgRes = bgImages.random()

        if (isInCall()) {
            registerPhoneStateListener()
            callTimeoutHandler.postDelayed({
                if (!athanPlayed) { athanPlayed = true; moveTaskToBack(true); finish() }
            }, 4 * 60 * 1000L)
        } else {
            athanPlayed = true; playAzan(prayerName)
        }

        setContent {
            NoorTheme {
                AzanFullScreenContent(
                    prayerName = prayerName,
                    bgResId    = bgRes,
                    onStop = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            setShowWhenLocked(false); setTurnScreenOn(false)
                        }
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        stopAzan(); finishAndRemoveTask()
                    }
                )
            }
        }
    }

    private fun isInCall() = try {
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).callState != TelephonyManager.CALL_STATE_IDLE
    } catch (_: SecurityException) { false }

    private fun playAzan(prayerName: String) {
        if (PrayerAlarmService.isPlaying) return
        val i = Intent(this, PrayerAlarmService::class.java)
            .apply { putExtra(Constants.PRAYER_NAME_KEY, prayerName) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i) else startService(i)
    }

    private fun stopAzan() {
        startService(Intent(this, PrayerAlarmService::class.java)
            .apply { action = Constants.ACTION_STOP_AZAN })
    }

    @Suppress("DEPRECATION")
    private fun registerPhoneStateListener() {
        try {
            telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (_: SecurityException) {}
    }

    @Suppress("DEPRECATION")
    private fun unregisterPhoneStateListener() {
        try { telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE) } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        registerReceiver(azanDoneReceiver, IntentFilter(Constants.ACTION_STOP_AZAN))
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        runCatching { unregisterReceiver(azanDoneReceiver) }
    }

    override fun onDestroy() {
        unregisterPhoneStateListener()
        callTimeoutHandler.removeCallbacksAndMessages(null)
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
}

// ── Composable ─────────────────────────────────────────────────────
@Composable
fun AzanFullScreenContent(prayerName: String, bgResId: Int, onStop: () -> Unit) {
    val azanLines = listOf(
        "اللهُ أَكْبَر، اللهُ أَكْبَر",
        "أَشْهَدُ أَن لَّا إِلَٰهَ إِلَّا اللَّه",
        "أَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ اللَّه",
        "حَيَّ عَلَى الصَّلَاة",
        "حَيَّ عَلَى الْفَلَاح",
        "اللهُ أَكْبَر، اللهُ أَكْبَر",
        "لَا إِلَٰهَ إِلَّا اللَّه"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )

    var currentLineIndex by remember { mutableStateOf(0) }
    var lineVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            lineVisible = false; delay(400)
            currentLineIndex = (currentLineIndex + 1) % azanLines.size
            lineVisible = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── خلفية حقيقية ────────────────────────────────────────
        Image(
            painter = painterResource(id = bgResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // ── طبقة شفافة داكنة ────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xCC000000), Color(0xEE010F0A)))))

        // ── المحتوى ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            Box(
                modifier = Modifier.scale(scale).size(120.dp)
                    .background(Color(0x33C9A84C), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("🕌", fontSize = 54.sp) }

            Spacer(Modifier.height(24.dp))
            Text("أذان $prayerName", fontSize = 30.sp, fontWeight = FontWeight.Black,
                color = Gold, textAlign = TextAlign.Center)

            Text("محمد عبد العظيم الطويل", fontSize = 13.sp,
                color = Color(0xFFD4AF37).copy(alpha = 0.8f), textAlign = TextAlign.Center)

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = lineVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it },
                exit  = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it }
            ) {
                Text(azanLines[currentLineIndex], fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(12.dp))
            Text("﴿ إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا ﴾",
                fontSize = 13.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center)

            Spacer(Modifier.height(56.dp))
            Button(
                onClick  = onStop,
                colors   = ButtonDefaults.buttonColors(containerColor = Gold),
                modifier = Modifier.fillMaxWidth(0.6f).height(52.dp),
                shape    = CircleShape
            ) {
                Text("إيقاف الأذان", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

