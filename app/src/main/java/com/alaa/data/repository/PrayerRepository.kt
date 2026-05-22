package com.alaa.data.repository

import com.alaa.data.model.PrayerData
import com.alaa.data.prefs.PrefsManager
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerRepository(private val prefs: PrefsManager) {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
    private val hijriMonths = listOf(
        "محرم","صفر","ربيع الأول","ربيع الثاني",
        "جمادى الأولى","جمادى الآخرة","رجب","شعبان",
        "رمضان","شوال","ذو القعدة","ذو الحجة"
    )

    fun getPrayerTimes(lat: Double, lon: Double): PrayerData {
        return try {
            val coordinates = Coordinates(lat, lon)
            val date        = DateComponents.from(Date())
            val params      = CalculationMethod.EGYPTIAN.parameters
            val times       = PrayerTimes(coordinates, date, params)

            val fajr    = fmt(times.fajr)
            val sunrise = fmt(times.sunrise)
            val dhuhr   = fmt(times.dhuhr)
            val asr     = fmt(times.asr)
            val maghrib = fmt(times.maghrib)
            val isha    = fmt(times.isha)

            val (nextName, nextTime, countdown) = getNextPrayer(times)

            PrayerData(
                fajr    = fajr,
                sunrise = sunrise,
                dhuhr   = dhuhr,
                asr     = asr,
                maghrib = maghrib,
                isha    = isha,
                nextPrayerName = nextName,
                nextPrayerTime = nextTime,
                countdown      = countdown,
                hijriDate      = getHijriDate(),
                gregorianDate  = getGregorianDate()
            )
        } catch (e: Exception) {
            PrayerData()
        }
    }

    private fun fmt(date: Date?): String = date?.let { timeFormat.format(it) } ?: "--:--"

    private fun getNextPrayer(times: PrayerTimes): Triple<String, String, String> {
        val now = Date()
        val prayers = listOf(
            "الفجر"   to times.fajr,
            "الظهر"   to times.dhuhr,
            "العصر"   to times.asr,
            "المغرب"  to times.maghrib,
            "العشاء"  to times.isha,
        )
        val next = prayers.firstOrNull { (_, t) -> t != null && t.after(now) }
            ?: prayers.first() // if all passed, show Fajr (tomorrow)
        val diff = (next.second?.time ?: 0L) - now.time
        val h = (diff / 3_600_000).coerceAtLeast(0)
        val m = ((diff % 3_600_000) / 60_000).coerceAtLeast(0)
        val s = ((diff % 60_000) / 1_000).coerceAtLeast(0)
        return Triple(next.first, fmt(next.second), "%02d:%02d:%02d".format(h, m, s))
    }

    private fun getGregorianDate(): String {
        val cal  = Calendar.getInstance()
        val days = listOf("الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت")
        val day  = days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        return "$day ${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)+1}/${cal.get(Calendar.YEAR)}"
    }

    private fun getHijriDate(): String {
        return try {
            val islamicCal = android.icu.util.IslamicCalendar()
            val month = islamicCal.get(android.icu.util.Calendar.MONTH)
            val day   = islamicCal.get(android.icu.util.Calendar.DAY_OF_MONTH)
            val year  = islamicCal.get(android.icu.util.Calendar.YEAR)
            "$day ${hijriMonths[month]} $year هـ"
        } catch (e: Exception) { "" }
    }

    fun getScheduledPrayerTimes(lat: Double, lon: Double): Map<String, Date> {
        return try {
            val coordinates = Coordinates(lat, lon)
            val date        = DateComponents.from(Date())
            val params      = CalculationMethod.EGYPTIAN.parameters
            val times       = PrayerTimes(coordinates, date, params)
            mapOf(
                "الفجر"   to times.fajr,
                "الظهر"   to times.dhuhr,
                "العصر"   to times.asr,
                "المغرب"  to times.maghrib,
                "العشاء"  to times.isha,
            ).filterValues { it != null }.mapValues { it.value!! }
        } catch (e: Exception) { emptyMap() }
    }
}
