package com.alaa.di

import com.alaa.data.prefs.PrefsManager
import com.alaa.data.repository.PrayerRepository
import com.alaa.data.repository.WeatherRepository
import com.alaa.presentation.dhikr.DhikrViewModel
import com.alaa.presentation.home.HomeViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    single { PrefsManager(androidContext()) }
}

val networkModule = module {
    single<Gson> { GsonBuilder().create() }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }
}

val repositoryModule = module {
    singleOf(::PrayerRepository)
    singleOf(::WeatherRepository)
}

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::DhikrViewModel)
}
