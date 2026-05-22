package com.alaa.presentation.quran

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold

@Composable
fun QuranScreen() {
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // ─── Header ──────────────────────────────────────────────────────────
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DarkBg2, DarkBg)))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📖", fontSize = 36.sp)
                Spacer(Modifier.height(4.dp))
                Text("القرآن الكريم", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("قراءة واستماع", color = Color.White.copy(0.6f), fontSize = 12.sp)
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = Gold,
                trackColor = DarkBg2
            )
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                            if (newProgress == 100) isLoading = false
                        }
                    }
                    settings.apply {
                        javaScriptEnabled          = true
                        domStorageEnabled           = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode        = true
                        useWideViewPort             = true
                        cacheMode                   = WebSettings.LOAD_DEFAULT
                    }
                    setBackgroundColor(android.graphics.Color.parseColor("#0A1628"))
                    loadUrl("https://qari.app/")
                }
            },
            update = { }
        )
    }
}
