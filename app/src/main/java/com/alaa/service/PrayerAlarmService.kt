package com.alaa.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.alaa.R
import com.alaa.presentation.azan.AzanFullScreenActivity

class PrayerAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_AZAN) { stopSelf(); return START_NOT_STICKY }

        val prayerName = intent?.getStringExtra(PRAYER_NAME_KEY) ?: "الصلاة"
        val prayerKey  = intent?.getStringExtra(PRAYER_KEY) ?: "Fajr"

        isPlaying = true
        acquireWakeLock()
        createChannel()
        startForeground(NOTIF_ID, buildNotification(prayerName))

        val fullScreenIntent = AzanFullScreenActivity.newIntent(this, prayerName)
        startActivity(fullScreenIntent)
        playAzan(prayerKey)
        return START_NOT_STICKY
    }

    private fun getAzanRes(prayerKey: String): Int {
        val prefs = getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        val qariKey = prefs.getString("prayerAdhan_$prayerKey", "makkah") ?: "makkah"
        return when (qariKey) {
            "mishary_alafasi"      -> R.raw.azan_mishary_alafasi
            "abed_albaset"         -> R.raw.azan_abed_albaset
            "al_hosary"            -> R.raw.azan_al_hosary
            "al_nakshabandy"       -> R.raw.azan_al_nakshabandy
            "mansoor_al_zahrani"   -> R.raw.azan_mansoor_al_zahrani
            "mohamed_refat"        -> R.raw.azan_mohamed_refat
            "mohammed_almenshawy"  -> R.raw.azan_mohammed_almenshawy
            "nasser_alqatami"      -> R.raw.azan_nasser_alqatami
            "suhaib_khatba"        -> R.raw.azan_suhaib_khatba
            else                   -> R.raw.azan_makkah
        }
    }

    private fun playAzan(prayerKey: String) {
        try {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    ).build()
                audioFocusRequest = req
                am.requestAudioFocus(req)
            }
            mediaPlayer = MediaPlayer.create(this, getAzanRes(prayerKey))?.apply {
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
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                audioFocusRequest?.let {
                    (getSystemService(AUDIO_SERVICE) as AudioManager).abandonAudioFocusRequest(it)
                }
        } catch (_: Exception) {}
        try { if (wakeLock?.isHeld == true) wakeLock?.release() } catch (_: Exception) {}
        sendBroadcast(Intent(ACTION_STOP_AZAN))
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alaa:AzanWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null)
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "الأذان", NotificationManager.IMPORTANCE_HIGH)
                        .apply { setSound(null, null) }
                )
        }
    }

    private fun buildNotification(prayerName: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("وقت صلاة $prayerName")
            .setContentText("حي على الصلاة")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

    companion object {
        const val ACTION_STOP_AZAN = "com.alaa.STOP_AZAN"
        const val PRAYER_NAME_KEY  = "prayer_name"
        const val PRAYER_KEY       = "prayer_key"
        const val CHANNEL_ID       = "azan_channel"
        const val NOTIF_ID         = 1001
        @Volatile var isPlaying: Boolean = false
    }
}
