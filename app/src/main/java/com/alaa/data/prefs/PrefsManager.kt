package com.alaa.data.prefs

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alaa_prefs", Context.MODE_PRIVATE)

    // Location
    var latitude:  Double get() = Double.fromBits(prefs.getLong("lat", Double.toBits(30.0571))); set(v) = prefs.edit().putLong("lat", Double.doubleToRawLongBits(v)).apply()
    var longitude: Double get() = Double.fromBits(prefs.getLong("lon", Double.toBits(31.2272))); set(v) = prefs.edit().putLong("lon", Double.doubleToRawLongBits(v)).apply()
    var cityName:  String get() = prefs.getString("city", "القاهرة") ?: "القاهرة"; set(v) = prefs.edit().putString("city", v).apply()

    // Dhikr settings
    var dhikrInterval: Int     get() = prefs.getInt("dhikr_interval", 10); set(v) = prefs.edit().putInt("dhikr_interval", v).apply()
    var dhikrVolume:   Float   get() = prefs.getFloat("dhikr_volume", 1f); set(v) = prefs.edit().putFloat("dhikr_volume", v).apply()
    var dhikrAutoEnabled: Boolean get() = prefs.getBoolean("dhikr_auto", false); set(v) = prefs.edit().putBoolean("dhikr_auto", v).apply()
    var dhikrStartHour:   Int   get() = prefs.getInt("dhikr_start_h", 6); set(v) = prefs.edit().putInt("dhikr_start_h", v).apply()
    var dhikrStartMin:    Int   get() = prefs.getInt("dhikr_start_m", 0); set(v) = prefs.edit().putInt("dhikr_start_m", v).apply()
    var dhikrStopHour:    Int   get() = prefs.getInt("dhikr_stop_h", 22); set(v) = prefs.edit().putInt("dhikr_stop_h", v).apply()
    var dhikrStopMin:     Int   get() = prefs.getInt("dhikr_stop_m", 0); set(v) = prefs.edit().putInt("dhikr_stop_m", v).apply()
    var dhikrRepeatSingle: Boolean get() = prefs.getBoolean("dhikr_repeat_single", false); set(v) = prefs.edit().putBoolean("dhikr_repeat_single", v).apply()
    var selectedDhikrId:  Int   get() = prefs.getInt("selected_dhikr", 0); set(v) = prefs.edit().putInt("selected_dhikr", v).apply()

    // Azan enabled per prayer (0=Fajr,1=Dhuhr,2=Asr,3=Maghrib,4=Isha)
    fun isAzanEnabled(prayerIndex: Int): Boolean = prefs.getBoolean("azan_$prayerIndex", true)
    fun setAzanEnabled(prayerIndex: Int, enabled: Boolean) = prefs.edit().putBoolean("azan_$prayerIndex", enabled).apply()

    // Mesbaha
    var mesbahaCount: Int get() = prefs.getInt("mesbaha_count", 0); set(v) = prefs.edit().putInt("mesbaha_count", v).apply()
    var mesbahaColor: Int get() = prefs.getInt("mesbaha_color", 0xFF2C3E50.toInt()); set(v) = prefs.edit().putInt("mesbaha_color", v).apply()

    // Challenges
    var challengesFajr: Int get() = prefs.getInt("ch_fajr", 0); set(v) = prefs.edit().putInt("ch_fajr", v).apply()
    var challengesSadaqa: String get() = prefs.getString("ch_sadaqa", "") ?: ""; set(v) = prefs.edit().putString("ch_sadaqa", v).apply()
    var challengesSurah: String get() = prefs.getString("ch_surah", "") ?: ""; set(v) = prefs.edit().putString("ch_surah", v).apply()
    var challengesPoints: Int get() = prefs.getInt("ch_points", 0); set(v) = prefs.edit().putInt("ch_points", v).apply()
}
