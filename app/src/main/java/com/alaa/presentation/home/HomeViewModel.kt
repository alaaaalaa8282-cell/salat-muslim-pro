package com.alaa.presentation.home

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alaa.data.model.PrayerData
import com.alaa.data.model.WeatherData
import com.alaa.data.prefs.PrefsManager
import com.alaa.data.repository.PrayerRepository
import com.alaa.data.repository.WeatherRepository
import com.alaa.utils.PrayerScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.Locale

data class HomeState(
    val prayerData:  PrayerData  = PrayerData(),
    val weatherData: WeatherData = WeatherData(),
    val cityName:    String      = "جارٍ تحديد الموقع...",
    val isLoading:   Boolean     = true,
    val lat:         Double      = 0.0,
    val lon:         Double      = 0.0,
)

class HomeViewModel(
    private val prayerRepo:  PrayerRepository,
    private val weatherRepo: WeatherRepository,
    private val prefs:       PrefsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun init(context: Context) {
        val savedLat = prefs.latitude
        val savedLon = prefs.longitude
        if (savedLat != 0.0 && savedLon != 0.0) {
            _state.update { it.copy(cityName = prefs.cityName, lat = savedLat, lon = savedLon) }
            loadData(context, savedLat, savedLon)
        }
        fetchLocation(context)
        private fun startCountdownTick() {
    viewModelScope.launch {
        while (isActive) {
            val current = _state.value.prayerData
            // لو البيانات فاضية — اجلب من الـ cache
            if (current.nextPrayerTime.isEmpty()) {
                val lat = prefs.latitude
                val lon = prefs.longitude
                if (lat != 0.0 && lon != 0.0) {
                    try {
                        val prayer = prayerRepo.getPrayerTimes(lat, lon)
                        _state.update { it.copy(prayerData = prayer) }
                    } catch (_: Exception) {}
                }
                delay(1_000)
                continue
            }
            // باقي الكود زي ما هو
    }

    @Suppress("MissingPermission")
    fun fetchLocation(context: Context) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            var lat = 0.0
            var lon = 0.0
            for (provider in providers) {
                try {
                    val loc = lm.getLastKnownLocation(provider)
                    if (loc != null) {
                        lat = loc.latitude
                        lon = loc.longitude
                        break
                    }
                } catch (_: Exception) {}
            }
            if (lat != 0.0 || lon != 0.0) {
                prefs.latitude  = lat
                prefs.longitude = lon
                try {
                    @Suppress("DEPRECATION")
                    val addr = Geocoder(context, Locale("ar")).getFromLocation(lat, lon, 1)
                    val city = addr?.firstOrNull()?.locality
                        ?: addr?.firstOrNull()?.subAdminArea
                        ?: addr?.firstOrNull()?.adminArea
                        ?: "موقعك"
                    prefs.cityName = city
                    _state.update { it.copy(cityName = city) }
                } catch (_: Exception) {}
                loadData(context, lat, lon)
            } else {
                _state.update { it.copy(cityName = "فعّل الـ GPS", isLoading = false) }
            }
        } catch (_: Exception) {
            _state.update { it.copy(cityName = "تعذّر تحديد الموقع", isLoading = false) }
        }
    }

    private fun loadData(context: Context, lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val prayer  = prayerRepo.getPrayerTimes(lat, lon)
            val weather = try { weatherRepo.getWeather(lat, lon) } catch (_: Exception) { WeatherData() }
            _state.update {
                it.copy(prayerData = prayer, weatherData = weather, isLoading = false, lat = lat, lon = lon)
            }
            try {
                val times = prayerRepo.getScheduledPrayerTimes(lat, lon)
                PrayerScheduler.scheduleAllPrayers(context, times)
            } catch (_: Exception) {}
        }
    }

    private fun startCountdownTick() {
    viewModelScope.launch {
        while (isActive) {
            val current = _state.value.prayerData
            if (current.nextPrayerTime.isNotEmpty()) {
                try {
                    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale("ar"))
                    val nextTime = sdf.parse(current.nextPrayerTime)
                    val cal = java.util.Calendar.getInstance()
                    val p = java.util.Calendar.getInstance().apply { time = nextTime!! }
                    cal.set(java.util.Calendar.HOUR_OF_DAY, p.get(java.util.Calendar.HOUR_OF_DAY))
                    cal.set(java.util.Calendar.MINUTE, p.get(java.util.Calendar.MINUTE))
                    cal.set(java.util.Calendar.SECOND, 0)
                    val diff = cal.timeInMillis - System.currentTimeMillis()
                    if (diff > 0) {
                        val h = diff / 3_600_000
                        val m = (diff % 3_600_000) / 60_000
                        val s = (diff % 60_000) / 1_000
                        _state.update { it.copy(prayerData = current.copy(
                            countdown = "%02d:%02d:%02d".format(h, m, s)
                        ))}
                    }
                } catch (_: Exception) {}
            }
            delay(1_000)
        }
    }
}
}
