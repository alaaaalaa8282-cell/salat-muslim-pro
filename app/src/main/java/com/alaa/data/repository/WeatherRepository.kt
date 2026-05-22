package com.alaa.data.repository

import com.alaa.data.model.OpenMeteoResponse
import com.alaa.data.model.WeatherData
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude")             lat: Double,
        @Query("longitude")            lon: Double,
        @Query("current")              current: String = "temperature_2m,weathercode,windspeed_10m",
        @Query("wind_speed_unit")      windUnit: String = "kmh",
        @Query("timezone")             tz: String = "auto"
    ): OpenMeteoResponse
}

class WeatherRepository(retrofit: Retrofit) {
    private val api = retrofit.create(WeatherApi::class.java)

    suspend fun getWeather(lat: Double, lon: Double): WeatherData {
        return try {
            val response = api.getWeather(lat, lon)
            val current  = response.current ?: return WeatherData()
            WeatherData(
                temperature = current.temperature_2m,
                weatherCode = current.weathercode,
                windspeed   = current.windspeed_10m,
                description = weatherDescription(current.weathercode),
                icon        = weatherIcon(current.weathercode)
            )
        } catch (e: Exception) {
            WeatherData()
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
