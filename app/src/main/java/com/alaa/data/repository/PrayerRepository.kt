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

    // ─────────────────────────────────────────────────────────────────────
    // الدالة الرئيسية — تُرجع البيانات من الـ Cache فوراً إن وُجدت
    // ─────────────────────────────────────────────────────────────────────
    suspend fun getPrayerTimes(lat: Double, lon: Double): PrayerData {
        return withContext(Dispatchers.IO) {

            // ── 1. جرّب الـ Cache الأول ──────────────────────────────────
            val cached = loadFromCache()
            if (cached != null && isCacheValidToday()) {
                // Cache صالح لنفس اليوم → أرجعه فوراً بدون أي loading
                return@withContext cached
            }

            // ── 2. Cache قديم أو مفيش → اجلب من النت ───────────────────
            try {
                val fresh = fetchFromNetwork(lat, lon)
                saveToCache(fresh, lat, lon)
                fresh
            } catch (e: Exception) {
                // ── 3. فشل النت → أرجع الـ Cache القديم لو موجود ────────
                cached ?: PrayerData()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // جلب الأوقات من الـ API
    // ─────────────────────────────────────────────────────────────────────
    private fun fetchFromNetwork(lat: Double, lon: Double): PrayerData {
        val ts   = System.currentTimeMillis() / 1000
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

        return PrayerData(
            fajr = fajr, sunrise = sunrise, dhuhr = dhuhr,
            asr = asr, maghrib = maghrib, isha = isha,
            nextPrayerName = nextName,
            nextPrayerTime = nextTime,
            countdown      = countdown,
            hijriDate      = hijriDate,
            gregorianDate  = gregorianDate
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // حفظ في الـ SharedPreferences
    // ─────────────────────────────────────────────────────────────────────
    private fun saveToCache(data: PrayerData, lat: Double, lon: Double) {
    val todayKey = getTodayKey()
    prefs.saveCache(
        data.fajr, data.sunrise, data.dhuhr,
        data.asr, data.maghrib, data.isha,
        data.hijriDate, data.gregorianDate,
        data.nextPrayerName, data.nextPrayerTime,
        lat, lon, todayKey
    )
}

    // ─────────────────────────────────────────────────────────────────────
    // قراءة من الـ Cache
    // ─────────────────────────────────────────────────────────────────────
    private fun loadFromCache(): PrayerData? {
        val fajr = prefs.getString("cache_fajr", null) ?: return null
        return PrayerData(
            fajr          = fajr,
            sunrise       = prefs.getString("cache_sunrise",   "--:--") ?: "--:--",
            dhuhr         = prefs.getString("cache_dhuhr",     "--:--") ?: "--:--",
            asr           = prefs.getString("cache_asr",       "--:--") ?: "--:--",
            maghrib       = prefs.getString("cache_maghrib",   "--:--") ?: "--:--",
            isha          = prefs.getString("cache_isha",      "--:--") ?: "--:--",
            hijriDate     = prefs.getString("cache_hijri",     "") ?: "",
            gregorianDate = prefs.getString("cache_gregorian", getGregorianDate()) ?: getGregorianDate(),
            nextPrayerName = prefs.getString("cache_next_name", "الفجر") ?: "الفجر",
            nextPrayerTime = prefs.getString("cache_next_time", "--:--") ?: "--:--",
            countdown      = "--:--:--"   // الـ countdown يُحسب دايماً live
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // هل الـ Cache لنفس اليوم؟
    // ─────────────────────────────────────────────────────────────────────
    private fun isCacheValidToday(): Boolean {
        val cached  = prefs.getString("cache_date_key", null) ?: return false
        return cached == getTodayKey()
    }

    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    // ─────────────────────────────────────────────────────────────────────
    // آخر موقع محفوظ (لو مفيش GPS)
    // ─────────────────────────────────────────────────────────────────────
    fun getLastLocation(): Pair<Double, Double>? {
        val lat = prefs.getFloat("cache_lat", 0f)
        val lon = prefs.getFloat("cache_lon", 0f)
        return if (lat != 0f && lon != 0f) Pair(lat.toDouble(), lon.toDouble()) else null
    }

    // ─────────────────────────────────────────────────────────────────────
    // باقي الدوال (بدون تغيير)
    // ─────────────────────────────────────────────────────────────────────
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
        val now = Calendar.getInstance()
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        val prayers = listOf(
            "الفجر"  to timings.getString("Fajr"),
            "الظهر"  to timings.getString("Dhuhr"),
            "العصر"  to timings.getString("Asr"),
            "المغرب" to timings.getString("Maghrib"),
            "العشاء" to timings.getString("Isha"),
        )
        for ((name, rawTime) in prayers) {
            try {
                val parsed = sdf.parse(rawTime.substring(0, 5)) ?: continue
                val cal = Calendar.getInstance().apply {
                    time = parsed
                    set(Calendar.YEAR,         now.get(Calendar.YEAR))
                    set(Calendar.MONTH,        now.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.SECOND, 0)
                }
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
        // بعد العشاء → فجر الغد
        val fajrRaw = timings.getString("Fajr").substring(0, 5)
        val fajrCal = Calendar.getInstance().apply {
            val p = Calendar.getInstance().apply { time = sdf.parse(fajrRaw)!! }
            set(Calendar.HOUR_OF_DAY, p.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, p.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val diff = fajrCal.timeInMillis - now.timeInMillis
        val h = diff / 3_600_000
        val m = (diff % 3_600_000) / 60_000
        val s = (diff % 60_000) / 1_000
        return Triple("الفجر", fajr, "%02d:%02d:%02d".format(h, m, s))
    }

    private fun getGregorianDate(): String {
        val cal  = Calendar.getInstance()
        val days = listOf("الأحد","الاثنين","الثلاثاء","الأربعاء","الخميس","الجمعة","السبت")
        val day  = days[cal.get(Calendar.DAY_OF_WEEK) - 1]
        return "$day ${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)+1}/${cal.get(Calendar.YEAR)}"
    }

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
                            val cal = Calendar.getInstance().apply {
                                time = parsed
                                set(Calendar.YEAR,  Calendar.getInstance().get(Calendar.YEAR))
                                set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                                set(Calendar.SECOND, 0)
                            }
                            put(arName, cal.time)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) { emptyMap() }
        }
    }
}
