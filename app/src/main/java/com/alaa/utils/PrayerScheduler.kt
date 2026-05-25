package com.alaa.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alaa.service.PrayerAlarmReceiver
import java.util.Date

object PrayerScheduler {

    @SuppressLint("ScheduleExactAlarm")
    fun schedulePrayer(context: Context, prayerName: String, time: Date, requestCode: Int) {
        if (time.before(Date())) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, requestCode,
            Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(Constants.PRAYER_NAME_KEY, prayerName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.time, pi)
            else
                am.setExact(AlarmManager.RTC_WAKEUP, time.time, pi)
        } catch (_: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, time.time, pi)
        }
    }

    fun scheduleAllPrayers(context: Context, prayerTimes: Map<String, Date>) {
        listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء").forEachIndexed { i, name ->
            prayerTimes[name]?.let { schedulePrayer(context, name, it, Constants.PRAYER_REQUEST_CODE + i) }
        }
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..4) {
            PendingIntent.getBroadcast(
                context, Constants.PRAYER_REQUEST_CODE + i,
                Intent(context, PrayerAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )?.let { am.cancel(it) }
        }
    }
}
