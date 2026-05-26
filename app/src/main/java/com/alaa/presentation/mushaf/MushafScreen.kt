package com.alaa.presentation.mushaf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import java.io.File

@Composable
fun MushafScreen() {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mushaf_prefs", android.content.Context.MODE_PRIVATE)
    }

    var pageIndex by remember {
        mutableStateOf(prefs.getInt("last_page", 0)) // ✅ حفظ آخر صفحة
    }
    var totalPages by remember { mutableStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableStateOf(1f) }
    var bookmarks by remember {
        mutableStateOf(
            prefs.getStringSet("bookmarks", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()
        )
    }
    var showBookmarks by remember { mutableStateOf(false) }

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
DisposableEffect(pageIndex) {
    onDispose {
        prefs.edit().putInt("last_page", pageIndex).apply()
    }
}
    // حفظ الصفحة الحالية تلقائياً
    LaunchedEffect(pageIndex) {
        prefs.edit().putInt("last_page", pageIndex).apply()
        val page = renderer.openPage(pageIndex)
        val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmap = bmp
        scale = 1f
    }

    if (showBookmarks) {
        AlertDialog(
            onDismissRequest = { showBookmarks = false },
            title = { Text("المرجعيات", color = Gold) },
            containerColor = DarkBg2,
            text = {
                if (bookmarks.isEmpty()) {
                    Text("لا توجد مرجعيات", color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Column {
                        bookmarks.sorted().forEach { page ->
                            TextButton(onClick = {
                                pageIndex = page
                                showBookmarks = false
                            }) {
                                Text("صفحة ${page + 1}", color = Gold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookmarks = false }) {
                    Text("إغلاق", color = Gold)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg2),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("صفحة ${pageIndex + 1} من $totalPages", color = Gold, fontSize = 16.sp)

            Row {
                // زرار bookmark
                IconButton(onClick = {
                    val updated = if (bookmarks.contains(pageIndex))
                        bookmarks - pageIndex
                    else
                        bookmarks + pageIndex
                    bookmarks = updated
                    prefs.edit().putStringSet("bookmarks", updated.map { it.toString() }.toSet()).apply()
                }) {
                    Icon(
                        if (bookmarks.contains(pageIndex)) Icons.Filled.Bookmark
                        else Icons.Filled.BookmarkBorder,
                        contentDescription = null,
                        tint = Gold
                    )
                }

                // زرار عرض المرجعيات
                TextButton(onClick = { showBookmarks = true }) {
                    Text("مرجعياتي", color = Gold, fontSize = 12.sp)
                }
            }
        }

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
                androidx.compose.foundation.Image(
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
