package com.alaa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.alaa.navigation.AppNavHost
import com.alaa.ui.theme.AlaAppTheme
import com.alaa.service.CountdownNotificationService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startScreen = intent.getStringExtra("start_screen") ?: "home"
      CountdownNotificationService.start(this)
        setContent {
            AlaAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(startScreen = startScreen)
                }
            }
        }
    }
}
