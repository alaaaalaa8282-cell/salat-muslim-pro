package com.alaa.data.model

// ─── Prayer Times ─────────────────────────────────────────────────────────────
data class PrayerData(
    val fajr:    String = "--:--",
    val sunrise: String = "--:--",
    val dhuhr:   String = "--:--",
    val asr:     String = "--:--",
    val maghrib: String = "--:--",
    val isha:    String = "--:--",
    val nextPrayerName: String = "الفجر",
    val nextPrayerTime: String = "--:--",
    val countdown:      String = "--:--:--",
    val hijriDate:      String = "",
    val gregorianDate:  String = "",
)

// ─── Dhikr Item ───────────────────────────────────────────────────────────────
data class DhikrItem(
    val id: Int,
    val textAr: String,
    val audioResId: Int  // R.raw.xxx — add your audio files
)

// ─── Weather ──────────────────────────────────────────────────────────────────
data class WeatherData(
    val temperature: Double = 0.0,
    val weatherCode: Int    = 0,
    val windspeed:   Double = 0.0,
    val description: String = "",
    val icon:        String = "☀️",
)

// ─── Weather API response ─────────────────────────────────────────────────────
data class OpenMeteoResponse(
    val current: CurrentWeather?
) {
    data class CurrentWeather(
        val temperature_2m: Double,
        val weathercode:    Int,
        val windspeed_10m:  Double
    )
}

// ─── Default Dhikr List ───────────────────────────────────────────────────────
// NOTE: Add your audio files to app/src/main/res/raw/
// e.g. subhanallah.mp3, alhamdulillah.mp3, etc.
// Use R.raw.your_file_name as audioResId
object DhikrData {
    // Placeholder IDs — replace with actual R.raw.xxx after adding audio files
    val list = listOf(
        DhikrItem(0, "سبحان الله",             android.R.raw.fallbackring),
        DhikrItem(1, "الحمد لله",              android.R.raw.fallbackring),
        DhikrItem(2, "الله أكبر",              android.R.raw.fallbackring),
        DhikrItem(3, "لا إله إلا الله",        android.R.raw.fallbackring),
        DhikrItem(4, "سبحان الله وبحمده",     android.R.raw.fallbackring),
        DhikrItem(5, "أستغفر الله",            android.R.raw.fallbackring),
        DhikrItem(6, "أذكار الصباح",          android.R.raw.fallbackring),
        DhikrItem(7, "أذكار المساء",           android.R.raw.fallbackring),
        DhikrItem(8, "الصلاة على النبي ﷺ",   android.R.raw.fallbackring),
    )
}
