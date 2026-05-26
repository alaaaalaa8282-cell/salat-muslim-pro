package com.alaa.presentation.mushaf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import java.io.File

@Composable
fun MushafScreen() {
    val context = LocalContext.current
    var pageIndex by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableStateOf(1f) }

    val pdfFile = remember {
        val file = File(context.cacheDir, "quran.pdf")
        if (!file.exists()) {
            context.assets.open("E-Quran-PDF.pdf").use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }
        file
    }

    val renderer = remember {
        PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            .also { totalPages = it.pageCount }
    }

    DisposableEffect(Unit) { onDispose { renderer.close() } }

    LaunchedEffect(pageIndex) {
        val page = renderer.openPage(pageIndex)
        val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap = bmp
        scale = 1f
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "صفحة ${pageIndex + 1} من $totalPages",
            color = Gold,
            modifier = Modifier.padding(12.dp)
        )

        bitmap?.let {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                        }
                    }
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )
            }
        }

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(
                onClick = { if (pageIndex > 0) pageIndex-- },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = CircleShape,
                enabled = pageIndex > 0
            ) { Text("السابقة", color = DarkBg2) }

            Button(
                onClick = { if (pageIndex < totalPages - 1) pageIndex++ },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = CircleShape,
                enabled = pageIndex < totalPages - 1
            ) { Text("التالية", color = DarkBg2) }
        }
    }
}
