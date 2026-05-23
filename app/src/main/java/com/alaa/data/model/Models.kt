package com.alaa.data.model

import com.alaa.R

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

data class DhikrItem(
    val id: Int,
    val textAr: String,
    val audioResId: Int
)

data class WeatherData(
    val temperature: Double = 0.0,
    val weatherCode: Int    = 0,
    val windspeed:   Double = 0.0,
    val description: String = "",
    val icon:        String = "☀️",
)

data class OpenMeteoResponse(
    val current: CurrentWeather?
) {
    data class CurrentWeather(
        val temperature_2m: Double,
        val weathercode:    Int,
        val windspeed_10m:  Double
    )
}

object DhikrData {
    val list = listOf(
        DhikrItem(0, "سبحان الله وبحمده",        R.raw.sobhanallah_wabehamdeh),
        DhikrItem(1, "الحمد لله",                 R.raw.alhamdo_lelah),
        DhikrItem(2, "الله أكبر",                 R.raw.allah_akbar),
        DhikrItem(3, "لا إله إلا الله",           R.raw.la_ilah_ela_allah),
        DhikrItem(4, "اللهم لك الحمد",           R.raw.allahom_lk_alhamd),
        DhikrItem(5, "أستغفر الله",               R.raw.astaghfer_allah),
        DhikrItem(6, "لا حول ولا قوة إلا بالله", R.raw.lahawla_wlaqowat),
        DhikrItem(7, "الصلاة على النبي ﷺ",       R.raw.nozaker_salt_ala_habib),
        DhikrItem(8, "ربنا اغفر لي",             R.raw.rbna_ighfer_li),
        DhikrItem(9, "آية الأحزاب",              R.raw.ayah_elahzab),
    )
}

object AzanSounds {
    data class AzanSound(val id: Int, val name: String, val resId: Int)
    val list = listOf(
        AzanSound(0, "مكة المكرمة",      R.raw.azan_makkah),
        AzanSound(1, "أبو بكر الشاطري", R.raw.azan_al_hosary),
        AzanSound(2, "مشاري العفاسي",   R.raw.azan_mishary_alafasi),
        AzanSound(3, "محمد رفعت",       R.raw.azan_mohamed_refat),
        AzanSound(4, "ناصر القطامي",    R.raw.azan_nasser_alqatami),
        AzanSound(5, "عبد الباسط",      R.raw.azan_abed_albaset),
        AzanSound(6, "منصور الزهراني",  R.raw.azan_mansoor_al_zahrani),
        AzanSound(7, "سهيل الخطبة",     R.raw.azan_suhaib_khatba),
        AzanSound(8, "المنشاوي",        R.raw.azan_mohammed_almenshawy),
        AzanSound(9, "النقشبندي",        R.raw.azan_al_nakshabandy),
    )
}
