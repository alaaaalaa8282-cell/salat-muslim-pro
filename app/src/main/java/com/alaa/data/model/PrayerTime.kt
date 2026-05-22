package com.alaa.data.models

data class PrayerTime(
    val nameAr: String,
    val nameEn: String,
    val time: String,        // "HH:mm"
    val decimalLocal: Double // for countdown calculation
)
