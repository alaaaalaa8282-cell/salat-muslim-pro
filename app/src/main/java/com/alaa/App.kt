package com.alaa

import android.app.Application
import com.alaa.data.prefs.PrefsManager
import com.alaa.data.repository.PrayerRepository
import com.alaa.data.repository.WeatherRepository
import com.alaa.presentation.dhikr.DhikrViewModel
import com.alaa.presentation.home.HomeViewModel
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(
                module {
                    // Prefs
                    single { PrefsManager(androidContext()) }

                    // Network
                    single { GsonBuilder().create() }
                    single {
                        OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build()
                    }
                    single {
                        Retrofit.Builder()
                            .baseUrl("https://api.open-meteo.com/")
                            .client(get())
                            .addConverterFactory(GsonConverterFactory.create(get()))
                            .build()
                    }

                    // Repositories
                    singleOf(::PrayerRepository)
                    singleOf(::WeatherRepository)

                    // ViewModels
                    viewModelOf(::HomeViewModel)
                    viewModelOf(::DhikrViewModel)
                }
            )
        }
    }
}
