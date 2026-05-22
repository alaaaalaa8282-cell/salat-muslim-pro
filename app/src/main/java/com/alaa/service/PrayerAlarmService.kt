package com.alaa.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.alaa.presentation.azan.AzanFullScreenActivity
import com.alaa.utils.Constants
import com.alaa.utils.PrayerScheduler

class PrayerAlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock:    PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP_AZAN) {
            stopAzan(); return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(Constants.PRAYER_NAME_KEY) ?: "الصلاة"
        acquireWakeLock()
        createChannel()
        startForeground(Constants.AZAN_NOTIF_ID, buildNotification(prayerName))

        // Play default ring (replace with actual azan file)
        try {
            isPlaying = true
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                // TODO: replace with actual azan resource e.g. R.raw.azan_makkah
                // For now use system default notification sound
                val uri = android.provider.Settings.System.DEFAULT_RINGTONE_URI
                setDataSource(applicationContext, uri)
                prepare()
                isLooping = false
                setOnCompletionListener {
                    sendBroadcast(Intent(Constants.ACTION_STOP_AZAN))
                    stopAzan()
                }
                start()
            }
        } catch (e: Exception) {
            isPlaying = false
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun stopAzan() {
        isPlaying = false
        try { mediaPlayer?.stop(); mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null
        releaseWakeLock()
        try { stopForeground(true) } catch (_: Exception) {}
        stopSelf()
    }

    private fun acquireWakeLock() {
        try {
            wakeLock = (getSystemService(POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alaa:AzanWakeLock")
                .also { it.acquire(10 * 60 * 1000L) }
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try { if (wakeLock?.isHeld == true) wakeLock?.release() } catch (_: Exception) {}
        wakeLock = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(Constants.AZAN_CHANNEL_ID) == null)
                nm.createNotificationChannel(
                    NotificationChannel(
                        Constants.AZAN_CHANNEL_ID, "الأذان",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply { setSound(null, null); enableVibration(true) }
                )
        }
    }

    private fun buildNotification(prayerName: String): Notification =
        NotificationCompat.Builder(this, Constants.AZAN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("حان وقت $prayerName")
            .setContentText("اللهُ أَكْبَر، اللهُ أَكْبَر")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

    override fun onDestroy() { stopAzan(); super.onDestroy() }

    companion object {
        @Volatile var isPlaying: Boolean = false
    }
}

// ─── Receiver ─────────────────────────────────────────────────────────────────
class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(Constants.PRAYER_NAME_KEY) ?: "الصلاة"

        // Pause dhikr
        context.startService(Intent(context, DhikrService::class.java).apply {
            action = DhikrService.ACTION_PAUSE_FOR_AZAN
        })

        // Launch full-screen azan activity
        context.startActivity(AzanFullScreenActivity.newIntent(context, prayerName))
    }
}

// ─── Boot Receiver ────────────────────────────────────────────────────────────
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = com.alaa.data.prefs.PrefsManager(context)
            val repo  = com.alaa.data.repository.PrayerRepository(prefs)
            val times = repo.getScheduledPrayerTimes(prefs.latitude, prefs.longitude)
            PrayerScheduler.scheduleAllPrayers(context, times)
        }
    }
}
