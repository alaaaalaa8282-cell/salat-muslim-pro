package com.alaa.data

import com.alaa.data.models.PrayerTime
import java.util.Calendar
import kotlin.math.*

object PrayerCalculator {

    // ── Methods ──────────────────────────────────────────────────────
    data class Method(val fajr: Double, val isha: Double, val ishaIsMin: Boolean)

    private val METHODS = mapOf(
        "MWL"      to Method(18.0, 17.0, false),
        "ISNA"     to Method(15.0, 15.0, false),
        "Egypt"    to Method(19.5, 17.5, false),
        "Makkah"   to Method(18.5, 90.0, true),
        "Karachi"  to Method(18.0, 18.0, false),
        "Gulf"     to Method(19.5, 90.0, true),
        "Kuwait"   to Method(18.0, 17.5, false),
        "Qatar"    to Method(18.0, 90.0, true),
        "Singapore" to Method(20.0, 18.0, false),
        "France"   to Method(12.0, 12.0, false),
        "Turkey"   to Method(18.0, 17.0, false),
        "Russia"   to Method(16.0, 15.0, false)
    )

    private val D2R = Math.PI / 180.0
    private val R2D = 180.0 / Math.PI

    private fun sin(d: Double) = kotlin.math.sin(d * D2R)
    private fun cos(d: Double) = kotlin.math.cos(d * D2R)
    private fun tan(d: Double) = kotlin.math.tan(d * D2R)
    private fun asin(r: Double) = kotlin.math.asin(r) * R2D
    private fun atan2(y: Double, x: Double) = kotlin.math.atan2(y, x) * R2D
    private fun fixAngle(a: Double) = a - 360.0 * floor(a / 360.0)
    private fun fixHour(h: Double) = h - 24.0 * floor(h / 24.0)

    private fun julianDay(y: Int, m: Int, d: Int): Double {
        var yr = y; var mo = m
        if (mo <= 2) { yr--; mo += 12 }
        val A = floor(yr / 100.0)
        val B = 2 - A + floor(A / 4.0)
        return floor(365.25 * (yr + 4716)) + floor(30.6001 * (mo + 1)) + d + B - 1524.5
    }

    private data class SunPos(val dec: Double, val eqt: Double)

    private fun sunPosition(jd: Double): SunPos {
        val D = jd - 2451545.0
        val g = fixAngle(357.529 + 0.9856003 * D)
        val q = fixAngle(280.459 + 0.9856474 * D)
        val L = fixAngle(q + 1.915 * sin(g) + 0.02 * sin(2 * g))
        val e = 23.439 - 0.00000036 * D
        val RA = atan2(cos(e) * sin(L), cos(L)) / 15.0
        return SunPos(
            dec = asin(sin(e) * sin(L)),
            eqt = q / 15.0 - fixHour(RA)
        )
    }

    private fun hourAngle(lat: Double, dec: Double, elev: Double): Double? {
        val n = -sin(elev) - sin(lat) * sin(dec)
        val den = cos(lat) * cos(dec)
        val cv = n / den
        if (abs(cv) > 1) return null
        return acos(cv) * D2R * R2D / 15.0
    }

    private fun asrHourAngle(lat: Double, dec: Double, factor: Int): Double? {
        val el = R2D * atan(1.0 / (factor + tan(abs(lat - dec))))
        return hourAngle(lat, dec, -el)
    }

    private fun fmtTime(t: Double, tz: Double): String {
        if (t.isNaN() || t.isInfinite()) return "--:--"
        val fixed = fixHour(t + tz)
        val h = fixed.toInt()
        val m = ((fixed - h) * 60).roundToInt().coerceIn(0, 59)
        return "%02d:%02d".format(h % 24, m)
    }

    fun calculate(
        lat: Double,
        lng: Double,
        date: Calendar = Calendar.getInstance(),
        method: String = "Makkah",
        asrMethod: String = "Standard"
    ): List<PrayerTime> {
        val M = METHODS[method] ?: METHODS["Makkah"]!!
        val AF = if (asrMethod == "Hanafi") 2 else 1
        val tz = date.timeZone.getOffset(date.timeInMillis) / 3600000.0

        val jd = julianDay(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH))
        val (dec, eqt) = sunPosition(jd)
        val transit = 12.0 - eqt - lng / 15.0

        val ha_sr   = hourAngle(lat, dec, 0.833) ?: 0.0
        val ha_asr  = asrHourAngle(lat, dec, AF) ?: 0.0
        val ha_fajr = hourAngle(lat, dec, M.fajr) ?: M.fajr / 15.0 + 1.0
        val ha_isha = if (!M.ishaIsMin) hourAngle(lat, dec, M.isha) ?: M.isha / 15.0 + 1.0 else null

        val fajr_u    = transit - ha_fajr
        val sunrise_u = transit - ha_sr
        val dhuhr_u   = transit + 1.0 / 60.0
        val asr_u     = transit + ha_asr
        val maghrib_u = transit + ha_sr
        val isha_u    = if (M.ishaIsMin) maghrib_u + M.isha / 60.0 else transit + (ha_isha ?: 1.0)

        return listOf(
            PrayerTime("الفجر",   "Fajr",    fmtTime(fajr_u, tz),    fajr_u + tz),
            PrayerTime("الشروق",  "Sunrise", fmtTime(sunrise_u, tz), sunrise_u + tz),
            PrayerTime("الظهر",   "Dhuhr",   fmtTime(dhuhr_u, tz),   dhuhr_u + tz),
            PrayerTime("العصر",   "Asr",     fmtTime(asr_u, tz),     asr_u + tz),
            PrayerTime("المغرب",  "Maghrib", fmtTime(maghrib_u, tz), maghrib_u + tz),
            PrayerTime("العشاء",  "Isha",    fmtTime(isha_u, tz),    isha_u + tz)
        )
    }

    fun calcQibla(lat: Double, lng: Double): Double {
        val meccaLat = 21.4225
        val meccaLng = 39.8262
        val p1 = lat * Math.PI / 180.0
        val p2 = meccaLat * Math.PI / 180.0
        val dl = (meccaLng - lng) * Math.PI / 180.0
        val y = kotlin.math.sin(dl) * kotlin.math.cos(p2)
        val x = kotlin.math.cos(p1) * kotlin.math.sin(p2) - kotlin.math.sin(p1) * kotlin.math.cos(p2) * kotlin.math.cos(dl)
        return ((kotlin.math.atan2(y, x) * 180.0 / Math.PI) + 360.0) % 360.0
    }

    /** Returns Hijri date as Triple(day, monthNameAr, year) */
    fun toHijri(cal: Calendar = Calendar.getInstance()): Triple<Int, String, Int> {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val JDN = (1461 * (y + 4800 + (m - 14) / 12)) / 4 +
                (367 * (m - 2 - 12 * ((m - 14) / 12))) / 12 -
                (3 * ((y + 4900 + (m - 14) / 12) / 100)) / 4 + d - 32075
        val L = JDN - 1948440 + 10632
        val N = (L - 1) / 10631
        val L2 = L - 10631 * N + 354
        val J = ((10985 - L2) / 5316) * ((50 * L2) / 17719) + (L2 / 5670) * ((43 * L2) / 15238)
        val L3 = L2 - ((30 - J) / 15) * ((17719 * J) / 50) - (J / 16) * ((15238 * J) / 43) + 29
        val hy = 30 * N + J - 30
        val hm = (24 * L3) / 709
        val hd = L3 - (709 * ((24 * L3) / 709)) / 24
        val months = listOf("محرم","صفر","ربيع الأوّل","ربيع الآخر","جمادى الأولى","جمادى الآخرة",
            "رجب","شعبان","رمضان","شوال","ذو القعدة","ذو الحجة")
        return Triple(hd, months.getOrNull(hm - 1) ?: "", hy)
    }
}

