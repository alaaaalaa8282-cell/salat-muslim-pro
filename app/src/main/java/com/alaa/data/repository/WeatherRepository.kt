package com.alaa.data.repository

import android.content.Context
import com.alaa.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
    }

    suspend fun getWeather(lat: Double, lon: Double): WeatherData {
        return withContext(Dispatchers.IO) {

            // ── 1. Cache صالح (أقل من ساعة)؟ → أرجعه فوراً ──────────────
            val cached = loadFromCache()
            if (cached != null && isCacheValid()) {
                return@withContext cached
            }

            // ── 2. اجلب من النت ─────────────────────────────────────────
            try {
                val fresh = fetchFromNetwork(lat, lon)
                saveToCache(fresh)
                fresh
            } catch (e: Exception) {
                // ── 3. فشل النت → أرجع الـ Cache القديم لو موجود ────────
                cached ?: WeatherData()
            }
        }
    }

    private fun fetchFromNetwork(lat: Double, lon: Double): WeatherData {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,weathercode,windspeed_10m" +
            "&timezone=auto"

        val json    = JSONObject(URL(url).readText())
        val current = json.optJSONObject("current") ?: return WeatherData()
        val code    = current.optInt("weathercode", 0)

        return WeatherData(
            temperature = current.optDouble("temperature_2m", 0.0),
            weatherCode = code,
            windspeed   = current.optDouble("windspeed_10m", 0.0),
            description = weatherDescription(code),
            icon        = weatherIcon(code)
        )
    }

    // ── Cache لمدة ساعة واحدة ────────────────────────────────────────────
    private val CACHE_TTL_MS = 60 * 60 * 1000L   // 60 دقيقة

    private fun isCacheValid(): Boolean {
        val savedAt = prefs.getLong("saved_at", 0L)
        return System.currentTimeMillis() - savedAt < CACHE_TTL_MS
    }

    private fun saveToCache(data: WeatherData) {
        prefs.edit()
            .putFloat("temperature", data.temperature.toFloat())
            .putInt("weatherCode",   data.weatherCode)
            .putFloat("windspeed",   data.windspeed.toFloat())
            .putString("description", data.description)
            .putString("icon",        data.icon)
            .putLong("saved_at",      System.currentTimeMillis())
            .apply()
    }

    private fun loadFromCache(): WeatherData? {
        val icon = prefs.getString("icon", null) ?: return null
        return WeatherData(
            temperature = prefs.getFloat("temperature", 0f).toDouble(),
            weatherCode = prefs.getInt("weatherCode", 0),
            windspeed   = prefs.getFloat("windspeed", 0f).toDouble(),
            description = prefs.getString("description", "") ?: "",
            icon        = icon
        )
    }

    private fun weatherIcon(code: Int): String = when (code) {
        0          -> "☀️"
        1, 2       -> "🌤️"
        3          -> "☁️"
        45, 48     -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌧️"
        95         -> "⛈️"
        else       -> "🌡️"
    }

    private fun weatherDescription(code: Int): String = when (code) {
        0          -> "صافٍ"
        1          -> "صافٍ غالبًا"
        2          -> "غائم جزئيًا"
        3          -> "غائم"
        45, 48     -> "ضبابي"
        51, 53, 55 -> "رذاذ خفيف"
        61, 63, 65 -> "ممطر"
        71, 73, 75 -> "ثلجي"
        80, 81, 82 -> "زخات مطر"
        95         -> "عاصفة رعدية"
        else       -> "الطقس"
    }
}
