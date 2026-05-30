package com.alaa.data.prefs

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alaa_prefs", Context.MODE_PRIVATE)

    // Location — مخزّنة كـ String عشان نتجنب مشاكل Kotlin/Java مع Double
    var latitude:  Double
        get() = prefs.getString("lat", "30.0571")?.toDoubleOrNull() ?: 30.0571
        set(v) = prefs.edit().putString("lat", v.toString()).apply()

    var longitude: Double
        get() = prefs.getString("lon", "31.2272")?.toDoubleOrNull() ?: 31.2272
        set(v) = prefs.edit().putString("lon", v.toString()).apply()

    var cityName: String
        get() = prefs.getString("city", "القاهرة") ?: "القاهرة"
        set(v) = prefs.edit().putString("city", v).apply()

    // Dhikr
    var dhikrInterval: Int
        get() = prefs.getInt("dhikr_interval", 10)
        set(v) = prefs.edit().putInt("dhikr_interval", v).apply()

    var dhikrVolume: Float
        get() = prefs.getFloat("dhikr_volume", 1f)
        set(v) = prefs.edit().putFloat("dhikr_volume", v).apply()

    var dhikrAutoEnabled: Boolean
        get() = prefs.getBoolean("dhikr_auto", false)
        set(v) = prefs.edit().putBoolean("dhikr_auto", v).apply()

    var dhikrStartHour: Int
        get() = prefs.getInt("dhikr_start_h", 6)
        set(v) = prefs.edit().putInt("dhikr_start_h", v).apply()

    var dhikrStartMin: Int
        get() = prefs.getInt("dhikr_start_m", 0)
        set(v) = prefs.edit().putInt("dhikr_start_m", v).apply()

    var dhikrStopHour: Int
        get() = prefs.getInt("dhikr_stop_h", 22)
        set(v) = prefs.edit().putInt("dhikr_stop_h", v).apply()

    var dhikrStopMin: Int
        get() = prefs.getInt("dhikr_stop_m", 0)
        set(v) = prefs.edit().putInt("dhikr_stop_m", v).apply()

    var dhikrRepeatSingle: Boolean
        get() = prefs.getBoolean("dhikr_repeat_single", false)
        set(v) = prefs.edit().putBoolean("dhikr_repeat_single", v).apply()

    var selectedDhikrId: Int
        get() = prefs.getInt("selected_dhikr", 0)
        set(v) = prefs.edit().putInt("selected_dhikr", v).apply()

    var selectedAzanId: Int
        get() = prefs.getInt("selected_azan", 0)
        set(v) = prefs.edit().putInt("selected_azan", v).apply()

    // Azan per prayer
    fun isAzanEnabled(index: Int): Boolean = prefs.getBoolean("azan_$index", true)
    fun setAzanEnabled(index: Int, enabled: Boolean) =
        prefs.edit().putBoolean("azan_$index", enabled).apply()

    // Mesbaha
    var mesbahaCount: Int
        get() = prefs.getInt("mesbaha_count", 0)
        set(v) = prefs.edit().putInt("mesbaha_count", v).apply()

    // Challenges
    var challengesFajr: Int
        get() = prefs.getInt("ch_fajr", 0)
        set(v) = prefs.edit().putInt("ch_fajr", v).apply()

    var challengesSadaqa: String
        get() = prefs.getString("ch_sadaqa", "") ?: ""
        set(v) = prefs.edit().putString("ch_sadaqa", v).apply()

    var challengesSurah: String
        get() = prefs.getString("ch_surah", "") ?: ""
        set(v) = prefs.edit().putString("ch_surah", v).apply()

    var challengesPoints: Int
        get() = prefs.getInt("ch_points", 0)
        set(v) = prefs.edit().putInt("ch_points", v).apply()
fun saveCache(
    fajr: String, sunrise: String, dhuhr: String,
    asr: String, maghrib: String, isha: String,
    hijriDate: String, gregorianDate: String,
    nextName: String, nextTime: String,
    lat: Double, lon: Double, dateKey: String
) {
    prefs.edit()
        .putString("cache_fajr",      fajr)
        .putString("cache_sunrise",   sunrise)
        .putString("cache_dhuhr",     dhuhr)
        .putString("cache_asr",       asr)
        .putString("cache_maghrib",   maghrib)
        .putString("cache_isha",      isha)
        .putString("cache_hijri",     hijriDate)
        .putString("cache_gregorian", gregorianDate)
        .putString("cache_next_name", nextName)
        .putString("cache_next_time", nextTime)
        .putFloat("cache_lat",        lat.toFloat())
        .putFloat("cache_lon",        lon.toFloat())
        .putString("cache_date_key",  dateKey)
        .apply()
}
fun getString(key: String, default: String? = null): String? =
    prefs.getString(key, default)

fun getFloat(key: String, default: Float = 0f): Float =
    prefs.getFloat(key, default)
}

