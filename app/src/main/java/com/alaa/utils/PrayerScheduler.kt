package com.alaa.domain.usecase

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import com.alaa.data.PrayerCalculator
import com.alaa.presentation.base.Constants
import com.alaa.presentation.service.PrayerAlarmService
import java.util.Calendar

class PrayerSchedulingUseCase(private val app: Application) {

    fun rescheduleTodayPrayerAlarms() {
        val prefs = app.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 30.0f).toDouble()
        val lng = prefs.getFloat("lng", 31.0f).toDouble()
        if (lat == 30.0 && lng == 31.0) return  // no saved location yet

        val times = PrayerCalculator.calculate(lat, lng)
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        val salahKeys = listOf("الفجر", "الظهر", "العصر", "المغرب", "العشاء")
        var reqCode = 2000

        times.filter { it.nameAr in salahKeys }.forEach { prayer ->
            val parts = prayer.time.split(":")
            if (parts.size < 2) return@forEach
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, parts[0].toIntOrNull() ?: return@forEach)
                set(Calendar.MINUTE, parts[1].toIntOrNull() ?: return@forEach)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(app, PrayerAlarmService::class.java).apply {
                putExtra(Constants.PRAYER_NAME_KEY, prayer.nameAr)
                putExtra("prayer_key", prayer.nameEn)  // Fajr, Dhuhr, Asr, Maghrib, Isha
            }
            val pi = PendingIntent.getForegroundService(
                app, reqCode++, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } catch (e: Exception) {
                am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
        }
    }
}
