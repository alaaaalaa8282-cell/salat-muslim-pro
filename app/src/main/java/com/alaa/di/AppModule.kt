package com.alaa.di

import com.alaa.domain.usecase.PrayerSchedulingUseCase
import com.alaa.presentation.screen.dhikr.DhikrViewModel
import com.alaa.presentation.screen.home.HomeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { PrayerSchedulingUseCase(androidApplication()) }
    viewModel { DhikrViewModel(androidApplication()) }
    viewModel { HomeViewModel(androidApplication()) }
}
