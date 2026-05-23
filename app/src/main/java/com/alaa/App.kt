package com.alaa

import android.app.Application
import com.alaa.di.appModule
import com.alaa.di.networkModule
import com.alaa.di.repositoryModule
import com.alaa.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@App)
            modules(appModule, networkModule, repositoryModule, viewModelModule)
        }
    }
}
