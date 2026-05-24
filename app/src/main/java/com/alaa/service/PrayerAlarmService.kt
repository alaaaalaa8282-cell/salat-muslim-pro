package com.alaa.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.alaa.utils.Constants

class PrayerAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP_AZAN) {
            stopSelf(); return START_NOT_STICKY
        }
        val prayerName = intent?.getStringExtra(Constants.PRAYER_NAME_KEY) ?: "الصلاة"
        isPlaying = true
        acquireWakeLock()
        createChannel()
        startForeground(Constants.AZAN_NOTIF_ID, buildNotification(prayerName))
        playAzan()
        return START_NOT_STICKY
    }

    private fun getSelectedAzanRes(): Int {
        val prefs = getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        return when (prefs.getString("azan_qari", "makkah")) {
            "abed_albaset"        -> R.raw.azan_abed_albaset
            "al_hosary"           -> R.raw.azan_al_hosary
            "al_nakshabandy"      -> R.raw.azan_al_nakshabandy
            "mansoor_al_zahrani"  -> R.raw.azan_mansoor_al_zahrani
            "mishary_alafasi"     -> R.raw.azan_mishary_alafasi
            "mohamed_refat"       -> R.raw.azan_mohamed_refat
            "mohammed_almenshawy" -> R.raw.azan_mohammed_almenshawy
            "nasser_alqatami"     -> R.raw.azan_nasser_alqatami
            "suhaib_khatba"       -> R.raw.azan_suhaib_khatba
            else                  -> R.raw.azan_makkah
        }
    }

    private fun playAzan() {
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
            mediaPlayer = MediaPlayer.create(this, getSelectedAzanRes())?.apply {
                isLooping = false
                setOnCompletionListener { stopSelf() }
                start()
            }
        } catch (e: Exception) { stopSelf() }
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
        sendBroadcast(Intent(Constants.ACTION_STOP_AZAN))
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
            if (nm.getNotificationChannel(Constants.AZAN_CHANNEL_ID) == null)
                nm.createNotificationChannel(
                    NotificationChannel(Constants.AZAN_CHANNEL_ID, "الأذان", NotificationManager.IMPORTANCE_HIGH)
                        .apply { setSound(null, null) }
                )
        }
    }

    private fun buildNotification(prayerName: String): Notification =
        NotificationCompat.Builder(this, Constants.AZAN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("وقت صلاة $prayerName")
            .setContentText("حي على الصلاة")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

    companion object {
        @Volatile var isPlaying: Boolean = false
    }
}


// ─── Receiver ─────────────────────────────────────────────────────────────────
class PrayerAlarmReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
        val prayerName = intent.getStringExtra(com.alaa.utils.Constants.PRAYER_NAME_KEY) ?: "الصلاة"
        val i = Intent(context, PrayerAlarmService::class.java).apply {
            putExtra(com.alaa.utils.Constants.PRAYER_NAME_KEY, prayerName)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(i)
        else
            context.startService(i)
    }
}
