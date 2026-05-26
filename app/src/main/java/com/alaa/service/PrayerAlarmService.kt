package com.alaa.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.alaa.utils.Constants

class PrayerAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var prayerName = "الصلاة"

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP_AZAN) {
            stopSelf()
            return START_NOT_STICKY
        }
        prayerName = intent?.getStringExtra(Constants.PRAYER_NAME_KEY) ?: "الصلاة"
        isPlaying = true
        acquireWakeLock()
        createChannel()
        startForeground(Constants.AZAN_NOTIF_ID, buildNotification(prayerName))
        playAzan()
        return START_NOT_STICKY
    }

    private fun getSelectedAzanRes(): Int {
    val prefs = getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
    val enKey = when (prayerName) {
        "الفجر"  -> "Fajr"
        "الظهر"  -> "Dhuhr"
        "العصر"  -> "Asr"
        "المغرب" -> "Maghrib"
        "العشاء" -> "Isha"
        else     -> prayerName
    }
    val key = prefs.getString("prayerAdhan_$enKey", "makkah")
    if (key == "silent") return -1
    return when (key) {
        "abed_albaset"        -> com.alaa.R.raw.azan_abed_albaset
        "al_hosary"           -> com.alaa.R.raw.azan_al_hosary
        "al_nakshabandy"      -> com.alaa.R.raw.azan_al_nakshabandy
        "mansoor_al_zahrani"  -> com.alaa.R.raw.azan_mansoor_al_zahrani
        "mishary_alafasi"     -> com.alaa.R.raw.azan_mishary_alafasi
        "mohamed_refat"       -> com.alaa.R.raw.azan_mohamed_refat
        "mohammed_almenshawy" -> com.alaa.R.raw.azan_mohammed_almenshawy
        "nasser_alqatami"     -> com.alaa.R.raw.azan_nasser_alqatami
        "suhaib_khatba"       -> com.alaa.R.raw.azan_suhaib_khatba
        else                  -> com.alaa.R.raw.azan_makkah
    }
}

    private fun playAzan() {
        val res = getSelectedAzanRes()
        if (res == -1) { stopSelf(); return }
        try {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                am.requestAudioFocus(
                    android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        ).build()
                )
            }
            mediaPlayer = MediaPlayer.create(this, res)?.apply {
                isLooping = false
                setOnCompletionListener { stopSelf() }
                start()
            }
        } catch (_: Exception) { stopSelf() }
    }

    override fun onDestroy() {
        isPlaying = false
        try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
        try { if (wakeLock?.isHeld == true) wakeLock?.release() } catch (_: Exception) {}
        sendBroadcast(Intent(Constants.ACTION_STOP_AZAN))
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alaa:AzanWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(Constants.AZAN_CHANNEL_ID) == null)
                nm.createNotificationChannel(
                    NotificationChannel(
                        Constants.AZAN_CHANNEL_ID, "الأذان",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply { setSound(null, null) }
                )
        }
    }

    private fun buildNotification(prayerName: String): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, PrayerAlarmService::class.java).apply {
                action = Constants.ACTION_STOP_AZAN
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = PendingIntent.getActivity(
    this, 1,
    Intent(this, com.alaa.MainActivity::class.java),
    PendingIntent.FLAG_IMMUTABLE
)
        return NotificationCompat.Builder(this, Constants.AZAN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("وقت صلاة $prayerName")
            .setContentText("حي على الصلاة")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openIntent)
  .addAction(android.R.drawable.ic_media_pause, "إيقاف الأذان", stopIntent)
            .build()
      }

    companion object {
        @Volatile var isPlaying: Boolean = false
     }
    }

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(Constants.PRAYER_NAME_KEY) ?: "الصلاة"

        // ✅ فتح شاشة الأذان
    val screenIntent = com.alaa.presentation.azan.AzanFullScreenActivity.newIntent(context, prayerName)
        context.startActivity(screenIntent)
    }
}
