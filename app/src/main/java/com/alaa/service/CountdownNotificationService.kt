package com.alaa.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.alaa.data.prefs.PrefsManager
import com.alaa.data.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*
import android.app.Notification
import android.app.PendingIntent

class CountdownNotificationService : Service() {

    private val prefs: PrefsManager by inject()
    private val repo: PrayerRepository by inject()

    private val handler = Handler(Looper.getMainLooper())
    private var job: Job? = null

    // أوقات الصلاة المحفوظة
    private var prayerTimes: Map<String, Date> = emptyMap()
    private var lastFetchHour = -1

    companion object {
        const val CHANNEL_ID = "prayer_countdown_channel"
        const val NOTIF_ID   = 2001
        const val ACTION_STOP = "com.alaa.STOP_COUNTDOWN"

        fun start(context: Context) {
            val i = Intent(context, CountdownNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(i)
            else
                context.startService(i)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CountdownNotificationService::class.java))
        }
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        createChannel()
        // إشعار مبدئي فوري
        startForeground(NOTIF_ID, buildNotification("جارٍ التحميل...", ""))
        // جلب الأوقات ثم بدء العداد
        fetchAndStart()
        return START_STICKY
    }

    private fun fetchAndStart() {
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                prayerTimes = repo.getScheduledPrayerTimes(prefs.latitude, prefs.longitude)
                lastFetchHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                handler.post(ticker)
            } catch (_: Exception) {
                handler.postDelayed({ fetchAndStart() }, 30_000)
            }
        }
    }

    // ticker يشتغل كل ثانية
    private val ticker = object : Runnable {
        override fun run() {
            // إعادة الجلب كل يوم عند منتصف الليل
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (currentHour == 0 && lastFetchHour != 0) {
                fetchAndStart()
                return
            }

            val (name, countdown) = getNextPrayer()
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotification(name, countdown))
            handler.postDelayed(this, 1_000)
        }
    }

    private fun getNextPrayer(): Pair<String, String> {
        val now = Date()
        val upcoming = prayerTimes
            .entries
            .filter { it.value.after(now) }
            .minByOrNull { it.value.time }

        return if (upcoming != null) {
            val diff = upcoming.value.time - now.time
            val h = diff / 3_600_000
            val m = (diff % 3_600_000) / 60_000
            val s = (diff % 60_000) / 1_000
            upcoming.key to "%02d:%02d:%02d".format(h, m, s)
} else {
            val tomorrowFajr = prayerTimes["الفجر"]
            if (tomorrowFajr != null) {
                val cal = Calendar.getInstance()
                cal.time = tomorrowFajr
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val diff = cal.time.time - Date().time
                val h = diff / 3_600_000
                val m = (diff % 3_600_000) / 60_000
                val s = (diff % 60_000) / 1_000
                "الفجر" to "%02d:%02d:%02d".format(h, m, s)
            } else {
                "الفجر" to "--:--:--"
            }
        }
    }

    private fun buildNotification(prayerName: String, countdown: String): Notification {
    val openIntent = PendingIntent.getActivity(
        this, 0,
        Intent(this, com.alaa.MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
        .setContentTitle("باقي على صلاة $prayerName")
        .setContentText(countdown)
        .setOngoing(true)
        .setSilent(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(openIntent)
        .build()
}

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL_ID) == null)
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "عداد الصلاة",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        setSound(null, null)
                        enableVibration(false)
                    }
                )
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        job?.cancel()
        super.onDestroy()
    }
}
