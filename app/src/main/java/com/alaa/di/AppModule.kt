package com.alaa.di

import com.alaa.data.prefs.PrefsManager
import com.alaa.data.repository.PrayerRepository
import com.alaa.data.repository.WeatherRepository
import com.alaa.presentation.dhikr.DhikrViewModel
import com.alaa.presentation.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PrefsManager(androidContext()) }
    single { PrayerRepository(get()) }
    single { WeatherRepository(androidContext()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { DhikrViewModel(get()) }
}
