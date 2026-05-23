package com.alaa.data.repository

import com.alaa.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class WeatherRepository {

    suspend fun getWeather(lat: Double, lon: Double): WeatherData {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weathercode,windspeed_10m" +
                    "&timezone=auto"

                val json = JSONObject(URL(url).readText())
                val current = json.optJSONObject("current")
                    ?: return@withContext WeatherData()

                val code = current.optInt("weathercode", 0)
                WeatherData(
                    temperature = current.optDouble("temperature_2m", 0.0),
                    weatherCode = code,
                    windspeed   = current.optDouble("windspeed_10m", 0.0),
                    description = weatherDescription(code),
                    icon        = weatherIcon(code)
                )
            } catch (e: Exception) {
                WeatherData()
            }
        }
    }

    private fun weatherIcon(code: Int): String = when (code) {
        0            -> "☀️"
        1, 2         -> "🌤️"
        3            -> "☁️"
        45, 48       -> "🌫️"
        51, 53, 55   -> "🌦️"
        61, 63, 65   -> "🌧️"
        71, 73, 75   -> "❄️"
        80, 81, 82   -> "🌧️"
        95           -> "⛈️"
        else         -> "🌡️"
    }

    private fun weatherDescription(code: Int): String = when (code) {
        0            -> "صافٍ"
        1            -> "صافٍ غالبًا"
        2            -> "غائم جزئيًا"
        3            -> "غائم"
        45, 48       -> "ضبابي"
        51, 53, 55   -> "رذاذ خفيف"
        61, 63, 65   -> "ممطر"
        71, 73, 75   -> "ثلجي"
        80, 81, 82   -> "زخات مطر"
        95           -> "عاصفة رعدية"
        else         -> "الطقس"
    }
}
