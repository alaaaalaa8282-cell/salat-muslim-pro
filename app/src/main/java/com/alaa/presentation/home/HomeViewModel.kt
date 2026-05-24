package com.alaa.presentation.home

import android.content.Context
import android.location.Geocoder
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
import java.util.Locale

data class HomeState(
    val prayerData:  PrayerData  = PrayerData(),
    val weatherData: WeatherData = WeatherData(),
    val cityName:    String      = "جارٍ التحديد...",
    val isLoading:   Boolean     = true,
    val lat:         Double      = 30.0,
    val lon:         Double      = 31.0,
)

class HomeViewModel(
    private val prayerRepo:  PrayerRepository,
    private val weatherRepo: WeatherRepository,
    private val prefs:       PrefsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun init(context: Context) {
        val lat = prefs.latitude
        val lon = prefs.longitude
        _state.update { it.copy(lat = lat, lon = lon, cityName = prefs.cityName) }
        loadData(context, lat, lon)
        startCountdownTick()
    }

    fun fetchLocation(context: Context) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE)
                    as android.location.LocationManager
            val provider = android.location.LocationManager.NETWORK_PROVIDER
            @Suppress("MissingPermission")
            val loc = locationManager.getLastKnownLocation(provider)
                ?: locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            loc ?: return
            prefs.latitude  = loc.latitude
            prefs.longitude = loc.longitude
            try {
                @Suppress("DEPRECATION")
                val addresses = Geocoder(context, Locale("ar"))
                    .getFromLocation(loc.latitude, loc.longitude, 1)
                val city = addresses?.firstOrNull()?.locality
                    ?: addresses?.firstOrNull()?.adminArea ?: "موقعك"
                prefs.cityName = city
                _state.update { it.copy(cityName = city) }
            } catch (_: Exception) {}
            loadData(context, loc.latitude, loc.longitude)
        } catch (_: Exception) {}
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
                delay(1_000)
                val prayer = try {
                    prayerRepo.getPrayerTimes(prefs.latitude, prefs.longitude)
                } catch (_: Exception) { continue }
                _state.update { it.copy(prayerData = prayer) }
            }
        }
    }
}
