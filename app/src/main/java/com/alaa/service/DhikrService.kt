package com.alaa.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.alaa.MainActivity
import com.alaa.R
import kotlinx.coroutines.*

private var pausedForAzan = false

class DhikrService : Service() {

    private var mediaPlayer:     MediaPlayer? = null
    private var wakeLock:        PowerManager.WakeLock? = null
    private var scope            = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var dhikrResIds      = intArrayOf()
    private var dhikrTexts       = arrayOf<String>()
    private var intervalMinutes  = 5
    private var volume           = 1f
    private var currentIndex     = 0
    private var running          = false
    private var audioFocusReq:   AudioFocusRequest? = null
    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var nextDhikrJob:    Job? = null

    override fun onBind(intent: Intent?) = null

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopDhikr(); return START_NOT_STICKY }
            ACTION_PAUSE_FOR_AZAN -> { pausedForAzan = true; stopCurrentPlayer(); scheduleNextDhikr(); return START_STICKY }
            ACTION_RESUME_FOR_AZAN -> { pausedForAzan = false; if (running) scheduleNextDhikr(); return START_STICKY }
            ACTION_UPDATE_VOLUME -> {
                volume = intent.getFloatExtra(EXTRA_VOLUME, volume)
                runCatching { mediaPlayer?.setVolume(logVol(volume), logVol(volume)) }
                return START_STICKY
            }
        }

        dhikrResIds    = intent?.getIntArrayExtra(EXTRA_RES_IDS) ?: return START_NOT_STICKY
        dhikrTexts     = intent.getStringArrayExtra(EXTRA_TEXTS) ?: arrayOf()
        volume         = intent.getFloatExtra(EXTRA_VOLUME, 1f)
        intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL_MINUTES, 5)
        currentIndex   = 0
        running        = true
        isRunning      = true

        if (!scope.isActive) scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        acquireWakeLock()
        createChannel()
        startForeground(NOTIF_ID, buildNotification(currentIndex))
        registerCallListener()

        if (PrayerAlarmService.isPlaying) {
            scope.launch { while (PrayerAlarmService.isPlaying) delay(5000); playCurrentDhikr() }
        } else {
            playCurrentDhikr()
        }
        return START_STICKY
    }

    private fun logVol(v: Float) =
        if (v <= 0f) 0f
        else (1 - (Math.log((1 + (1 - v) * 99).toDouble()) / Math.log(100.0))).toFloat()

    private suspend fun waitMinutes(n: Int) = delay(n * 60_000L)

    private fun isInCall() = try {
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).callState != TelephonyManager.CALL_STATE_IDLE
    } catch (_: SecurityException) { false }

    private fun abandonAudioFocus() {
        try {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                audioFocusReq?.let { am.abandonAudioFocusRequest(it) }.also { audioFocusReq = null }
            else @Suppress("DEPRECATION") am.abandonAudioFocus(null)
        } catch (_: Exception) {}
    }

    private fun scheduleNextDhikr() {
        nextDhikrJob?.cancel()
        nextDhikrJob = scope.launch {
            waitMinutes(intervalMinutes)
            if (running && !PrayerAlarmService.isPlaying && !isInCall()) {
                currentIndex = (currentIndex + 1) % dhikrResIds.size
                playCurrentDhikr()
            }
        }
    }

    private fun stopCurrentPlayer() {
        try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
        abandonAudioFocus()
    }

    @Suppress("DEPRECATION")
    private fun registerCallListener() {
        try {
            telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            phoneStateListener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    when (state) {
                        TelephonyManager.CALL_STATE_RINGING,
                        TelephonyManager.CALL_STATE_OFFHOOK -> {
                            if (running && !pausedForAzan) { stopCurrentPlayer(); scheduleNextDhikr() }
                        }
                    }
                }
            }
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (_: SecurityException) {}
    }

    @Suppress("DEPRECATION")
    private fun unregisterCallListener() {
        try { telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE) }
        catch (_: Exception) {}
        phoneStateListener = null; telephonyManager = null
    }

    @Suppress("DEPRECATION")
    private fun playCurrentDhikr() {
        if (!running || dhikrResIds.isEmpty() || isInCall() || PrayerAlarmService.isPlaying) return
        val lv = logVol(volume)
        updateNotification(currentIndex)
        try {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener {}.build()
                audioFocusReq = req
                am.requestAudioFocus(req)
            } else {
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }

            mediaPlayer = MediaPlayer.create(this, dhikrResIds[currentIndex])?.apply {
                setVolume(lv, lv)
                setWakeMode(this@DhikrService, PowerManager.PARTIAL_WAKE_LOCK)
                setOnCompletionListener { stopCurrentPlayer(); scheduleNextDhikr() }
                setOnErrorListener      { _, _, _ -> stopCurrentPlayer(); scheduleNextDhikr(); true }
                start()
            }
        } catch (_: Exception) { stopCurrentPlayer(); scheduleNextDhikr() }
    }

    private fun stopDhikr() {
        unregisterCallListener(); nextDhikrJob?.cancel()
        running = false; isRunning = false; scope.cancel()
        stopCurrentPlayer(); releaseWakeLock()
        try { stopForeground(true) } catch (_: Exception) {}
        stopSelf()
    }

    private fun acquireWakeLock() {
        try {
            wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alaa:DhikrWakeLock")
                .also { it.acquire(6 * 60 * 60 * 1000L) }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try { if (wakeLock?.isHeld == true) wakeLock?.release() } catch (_: Exception) {}
        wakeLock = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null)
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "الأذكار الصوتية", NotificationManager.IMPORTANCE_LOW)
                        .apply { setSound(null, null); enableVibration(false) }
                )
        }
    }

    private fun buildNotification(index: Int): Notification {
        val text = dhikrTexts.getOrElse(index) { "جارٍ التشغيل..." }
        val stopPi = PendingIntent.getService(this, 0,
            Intent(this, DhikrService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val openPi = PendingIntent.getActivity(this, 1,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("أذكاري — ${index + 1}/${dhikrResIds.size}")
            .setContentText(text)
            .setContentIntent(openPi)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "إيقاف", stopPi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification(index: Int) {
        try { getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(index)) }
        catch (_: Exception) {}
    }

    override fun onDestroy() {
        unregisterCallListener(); nextDhikrJob?.cancel()
        running = false; isRunning = false; scope.cancel()
        stopCurrentPlayer(); releaseWakeLock()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        startService(Intent(applicationContext, DhikrService::class.java).apply {
            putExtra(EXTRA_RES_IDS, dhikrResIds)
            putExtra(EXTRA_TEXTS, dhikrTexts)
            putExtra(EXTRA_INTERVAL_MINUTES, intervalMinutes)
            putExtra(EXTRA_VOLUME, volume)
        })
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val CHANNEL_ID             = "dhikr_channel"
        const val NOTIF_ID               = 42
        const val ACTION_STOP            = "com.alaa.STOP_DHIKR"
        const val ACTION_UPDATE_VOLUME   = "com.alaa.UPDATE_VOLUME"
        const val ACTION_PAUSE_FOR_AZAN  = "com.alaa.PAUSE_DHIKR_FOR_AZAN"
        const val ACTION_RESUME_FOR_AZAN = "com.alaa.RESUME_DHIKR_FOR_AZAN"
        const val EXTRA_RES_IDS          = "dhikr_res_ids"
        const val EXTRA_TEXTS            = "dhikr_texts"
        const val EXTRA_INTERVAL_MINUTES = "dhikr_interval_minutes"
        const val EXTRA_VOLUME           = "dhikr_volume"

        @Volatile var isRunning: Boolean = false
    }
}
