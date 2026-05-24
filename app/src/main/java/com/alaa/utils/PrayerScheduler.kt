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

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(Constants.PRAYER_NAME_KEY, prayerName)
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.time, pi)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.time, pi)
            }
        } catch (e: SecurityException) {
            // SCHEDULE_EXACT_ALARM not granted — fall back to inexact
            alarmManager.set(AlarmManager.RTC_WAKEUP, time.time, pi)
        }
    }

    fun scheduleAllPrayers(context: Context, prayerTimes: Map<String, Date>) {
        val prayerList = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
        prayerList.forEachIndexed { index, name ->
            prayerTimes[name]?.let { time ->
                schedulePrayer(context, name, time, Constants.PRAYER_REQUEST_CODE + index)
            }
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..4) {
            val pi = PendingIntent.getBroadcast(
                context,
                Constants.PRAYER_REQUEST_CODE + i,
                Intent(context, PrayerAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }
    }
}
