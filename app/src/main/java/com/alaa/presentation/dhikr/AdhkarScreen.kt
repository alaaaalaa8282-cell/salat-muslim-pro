package com.alaa.presentation.dhikr

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2
import com.alaa.ui.theme.Gold
import com.alaa.ui.theme.GoldLight
import org.json.JSONObject

data class AdhkarItem(
    val id: String,
    val title: String,
    val text: String,
    val repeat: Int,
    val benefit: String,
    val type: String
)

fun loadAdhkar(context: Context, fileName: String): List<AdhkarItem> {
    return try {
        val json = context.assets.open(fileName)
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr  = root.getJSONArray("adhkar")
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            AdhkarItem(
                id      = obj.optString("id", "$i"),
                title   = obj.optString("title", ""),
                text    = obj.optString("text", ""),
                repeat  = obj.optInt("repeat", 1),
                benefit = obj.optString("benefit", ""),
                type    = obj.optString("type", "dua")
            )
        }
    } catch (e: Exception) { emptyList() }
}

@Composable
fun AdhkarScreen() {
    val ctx = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🌅 أذكار الصباح", "🌇 أذكار المساء")

    val morningList = remember { loadAdhkar(ctx, "morning_adhkar.json") }
    val eveningList = remember { loadAdhkar(ctx, "evening_adhkar.json") }
    val currentList = if (selectedTab == 0) morningList else eveningList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBg2, DarkBg)))
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp)
        ) {
            Text(
                if (selectedTab == 0) "🌅" else "🌇",
                fontSize = 40.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (selectedTab == 0) "أذكار الصباح" else "أذكار المساء",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
            Text(
                "من الثابت في السنة النبوية",
                fontSize = 11.sp,
                color = Color.White.copy(0.5f)
            )
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkBg2),
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == index) Gold else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == index) Color.Black else Gold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Counter summary
        Text(
            "${currentList.size} ذكر",
            fontSize = 12.sp,
            color = Color.White.copy(0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.End
        )

        // List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(currentList) { index, item ->
                AdhkarCard(index = index + 1, item = item)
            }
        }
    }
}

@Composable
private fun AdhkarCard(index: Int, item: AdhkarItem) {
    var expanded by remember { mutableStateOf(false) }
    var currentCount by remember { mutableStateOf(0) }
    val isDone = currentCount >= item.repeat

    val cardBg = when {
        isDone -> Gold.copy(0.15f)
        item.type == "quran" -> Color(0xFF0D2137)
        else -> DarkBg2
    }
    val borderColor = when {
        isDone -> Gold
        item.type == "quran" -> Color(0xFF1E4D6B)
        else -> Color.White.copy(0.05f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Index + title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDone) Gold else Gold.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isDone) "✓" else "$index",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) Color.Black else Gold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) Gold else GoldLight
                    )
                    if (item.type == "quran") {
                        Text(
                            "قرآن كريم",
                            fontSize = 10.sp,
                            color = Color(0xFF4FC3F7)
                        )
                    }
                }
            }
            // Repeat badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Gold.copy(0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "${currentCount}/${item.repeat}×",
                    fontSize = 12.sp,
                    color = if (isDone) Gold else GoldLight,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Text
        Text(
            item.text,
            fontSize = 18.sp,
            color = Color.White.copy(if (isDone) 0.6f else 0.9f),
            textAlign = TextAlign.Right,
            lineHeight = 30.sp,
            modifier = Modifier.fillMaxWidth()
        )

        // Benefit (expandable)
        AnimatedVisibility(expanded && item.benefit.isNotEmpty()) {
            Column {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Gold.copy(0.2f))
                Spacer(Modifier.height(8.dp))
                Text(
                    "💡 ${item.benefit}",
                    fontSize = 12.sp,
                    color = GoldLight.copy(0.8f),
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Counter button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            TextButton(onClick = { currentCount = 0 }) {
                Text("إعادة", fontSize = 12.sp, color = Color.White.copy(0.4f))
            }

            // Count button
            Button(
                onClick = { if (!isDone) currentCount++ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDone) Gold.copy(0.3f) else Gold,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    if (isDone) "✓ تم" else "سبّح  ${item.repeat - currentCount} باقي",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) Gold else Color.Black
                )
            }
        }
    }
}
