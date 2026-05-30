package com.alaa

import android.app.Application
import com.alaa.data.prefs.PrefsManager
import com.alaa.data.repository.PrayerRepository
import com.alaa.data.repository.WeatherRepository
import com.alaa.presentation.dhikr.DhikrViewModel
import com.alaa.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(
                module {
                    single { PrefsManager(androidContext()) }
                    single { PrayerRepository(get()) }
                    single { PrayerRepository(get()) }
                    single { WeatherRepository(androidContext()) }
                    viewModel { HomeViewModel(get(), get(), get()) }
                    viewModel { DhikrViewModel(get()) }
                }
            )
        }
    }
}
