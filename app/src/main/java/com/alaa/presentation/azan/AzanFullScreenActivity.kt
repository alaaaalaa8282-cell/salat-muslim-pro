package com.alaa.presentation.azan

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    private var telephonyManager: TelephonyManager? = null
    private val callTimeoutHandler = Handler(Looper.getMainLooper())

    @Suppress("DEPRECATION")
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                callTimeoutHandler.removeCallbacksAndMessages(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

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
        val i = Intent(this, com.alaa.service.PrayerAlarmService::class.java).apply {
            putExtra(Constants.PRAYER_NAME_KEY, prayerName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(i)
        else
            startService(i)
    }

    private fun stopAzan() {
        startService(Intent(this, com.alaa.service.PrayerAlarmService::class.java).apply {
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

@Composable
fun AzanFullScreenContent(prayerName: String, onStop: () -> Unit) {
    val azanLines = listOf(
        "اللهُ أَكْبَر، اللهُ أَكْبَر",
        "أَشْهَدُ أَن لَّا إِلَٰهَ إِلَّا اللَّه",
        "أَشْهَدُ أَنَّ مُحَمَّدًا رَسُولُ اللَّه",
        "حَيَّ عَلَى الصَّلَاة",
        "حَيَّ عَلَى الْفَلَاح",
        "اللهُ أَكْبَر، اللهُ أَكْبَر",
        "لَا إِلَٰهَ إِلَّا اللَّه"
    )

    var currentLine by remember { mutableStateOf(0) }
    var lineVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            lineVisible = false; delay(400)
            currentLine = (currentLine + 1) % azanLines.size
            lineVisible = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        1f, 1.15f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0A1628), Color(0xFF000000)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(130.dp)
                    .background(Gold.copy(0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🕌", fontSize = 60.sp)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "أذان $prayerName",
                fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = Gold, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = lineVisible,
                enter = slideInVertically { it } + fadeIn(tween(600)),
                exit  = slideOutVertically { -it } + fadeOut(tween(400))
            ) {
                Text(
                    azanLines[currentLine],
                    fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(56.dp))

            Button(
                onClick = onStop,
                colors  = ButtonDefaults.buttonColors(containerColor = Gold),
                modifier = Modifier.fillMaxWidth(0.6f).height(52.dp),
                shape = CircleShape
            ) {
                Text("إيقاف الأذان", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
