package com.alaa.data.repository

import com.alaa.data.model.PrayerData
import com.alaa.data.prefs.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class PrayerRepository(private val prefs: PrefsManager) {

    private val hijriMonths = listOf(
        "محرم","صفر","ربيع الأول","ربيع الثاني",
        "جمادى الأولى","جمادى الآخرة","رجب","شعبان",
        "رمضان","شوال","ذو القعدة","ذو الحجة"
    )

    suspend fun getPrayerTimes(lat: Double, lon: Double): PrayerData {
        return withContext(Dispatchers.IO) {
            try {
                val ts   = System.currentTimeMillis() / 1000
                // Method 5 = Egyptian General Authority of Survey
                val url  = "https://api.aladhan.com/v1/timings/$ts?latitude=$lat&longitude=$lon&method=5"
                val json = JSONObject(URL(url).readText())
                val data = json.getJSONObject("data")
                val timings = data.getJSONObject("timings")

                val fajr    = convert(timings.getString("Fajr"))
                val sunrise = convert(timings.getString("Sunrise"))
                val dhuhr   = convert(timings.getString("Dhuhr"))
                val asr     = convert(timings.getString("Asr"))
                val maghrib = convert(timings.getString("Maghrib"))
                val isha    = convert(timings.getString("Isha"))

                val hijri = data.getJSONObject("date").getJSONObject("hijri")
                val hijriDay   = hijri.getString("day")
                val hijriMonth = hijri.getJSONObject("month").getString("number").toIntOrNull()?.minus(1) ?: 0
                val hijriYear  = hijri.getString("year")
                val hijriDate  = "$hijriDay ${hijriMonths.getOrElse(hijriMonth) { "" }} $hijriYear هـ"

                val gregorianDate = getGregorianDate()

                val (nextName, nextTime, countdown) = getNext(fajr, dhuhr, asr, maghrib, isha, timings)

                PrayerData(
                    fajr = fajr, sunrise = sunrise, dhuhr = dhuhr,
                    asr = asr, maghrib = maghrib, isha = isha,
                    nextPrayerName = nextName,
                    nextPrayerTime = nextTime,
                    countdown      = countdown,
                    hijriDate      = hijriDate,
                    gregorianDate  = gregorianDate
                )
            } catch (e: Exception) {
                PrayerData()
            }
        }
    }

    // "04:30" → "04:30 ص"
    private fun convert(time24: String): String {
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
            val sdf12 = SimpleDateFormat("hh:mm a", Locale("ar"))
            sdf12.format(sdf24.parse(time24.substring(0, 5))!!)
        } catch (e: Exception) { time24 }
    }

    private fun getNext(
        fajr: String, dhuhr: String, asr: String, maghrib: String, isha: String,
        timings: JSONObject
    ): Triple<String, String, String> {
        val now    = Calendar.getInstance()
        val sdf    = SimpleDateFormat("HH:mm", Locale.US)
        val prayers = listOf(
            "الفجر"   to timings.getString("Fajr"),
            "الظهر"   to timings.getString("Dhuhr"),
            "العصر"   to timings.getString("Asr"),
            "المغرب"  to timings.getString("Maghrib"),
            "العشاء"  to timings.getString("Isha"),
        )
        for ((name, rawTime) in prayers) {
            try {
                val cal = Calendar.getInstance()
                val parsed = sdf.parse(rawTime.substring(0, 5)) ?: continue
                val p = Calendar.getInstance().apply { time = parsed }
                cal.set(Calendar.HOUR_OF_DAY, p.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, p.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                if (cal.after(now)) {
                    val diff = cal.timeInMillis - now.timeInMillis
                    val h = diff / 3_600_000
                    val m = (diff % 3_600_000) / 60_000
                    val s = (diff % 60_000) / 1_000
                    val displayTime = when (name) {
                        "الفجر"  -> fajr
                        "الظهر"  -> dhuhr
                        "العصر"  -> asr
                        "المغرب" -> maghrib
                        else     -> isha
                    }
                    return Triple(name, displayTime, "%02d:%02d:%02d".format(h, m, s))
                }
            } catch (_: Exception) {}
        }
        return Triple("الفجر", fajr, "--:--:--")
    }

    private fun getGregorianDate(): String {
        val cal  = Calendar.getInstance()
        val days = listOf("الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت")
        val day  = days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        return "$day ${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)+1}/${cal.get(Calendar.YEAR)}"
    }

    // للجدولة — يرجع أوقات الصلاة كـ Date objects
    suspend fun getScheduledPrayerTimes(lat: Double, lon: Double): Map<String, Date> {
        return withContext(Dispatchers.IO) {
            try {
                val ts  = System.currentTimeMillis() / 1000
                val url = "https://api.aladhan.com/v1/timings/$ts?latitude=$lat&longitude=$lon&method=5"
                val json = JSONObject(URL(url).readText())
                val timings = json.getJSONObject("data").getJSONObject("timings")
                val sdf = SimpleDateFormat("HH:mm", Locale.US)
                val names = mapOf(
                    "الفجر"  to "Fajr",
                    "الظهر"  to "Dhuhr",
                    "العصر"  to "Asr",
                    "المغرب" to "Maghrib",
                    "العشاء" to "Isha"
                )
                buildMap {
                    names.forEach { (arName, enKey) ->
                        try {
                            val raw = timings.getString(enKey).substring(0, 5)
                            val parsed = sdf.parse(raw) ?: return@forEach
                            val cal = Calendar.getInstance()
                            val p   = Calendar.getInstance().apply { time = parsed }
                            cal.set(Calendar.HOUR_OF_DAY, p.get(Calendar.HOUR_OF_DAY))
                            cal.set(Calendar.MINUTE, p.get(Calendar.MINUTE))
                            cal.set(Calendar.SECOND, 0)
                            put(arName, cal.time)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) { emptyMap() }
        }
    }
}
