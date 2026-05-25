package com.alaa.presentation.screen.radio

import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.ui.theme.Gold
import com.alaa.ui.theme.GoldLight
import com.alaa.ui.theme.DarkBg
import com.alaa.ui.theme.DarkBg2

private val GoldDim       = Color(0xFF8B6914).copy(alpha = 0.4f)
private val TextPrimary   = Color(0xFFE8F4F0)
private val TextSecondary = Color(0xFF9CA3AF)
private val CardDark      = Color(0xFF0D1F3A)

data class RadioChannel(val id: Int, val name: String, val url: String)

private val CHANNELS = listOf(
    RadioChannel(1,  "إذاعة أبو بكر الشاطري",           "https://backup.qurango.net/radio/shaik_abu_bakr_al_shatri"),
    RadioChannel(2,  "إذاعة أحمد خضر الطرابلسي",        "https://backup.qurango.net/radio/ahmad_khader_altarabulsi"),
    RadioChannel(3,  "إذاعة إبراهيم الأخضر",            "https://backup.qurango.net/radio/ibrahim_alakdar"),
    RadioChannel(4,  "إذاعة خالد الجليل",               "https://backup.qurango.net/radio/khalid_aljileel"),
    RadioChannel(5,  "إذاعة صلاح الهاشم",               "https://backup.qurango.net/radio/salah_alhashim"),
    RadioChannel(6,  "إذاعة صلاح بو خاطر",              "https://backup.qurango.net/radio/slaah_bukhatir"),
    RadioChannel(7,  "إذاعة عبد الباسط عبد الصمد",      "https://backup.qurango.net/radio/abdulbasit_abdulsamad_mojawwad"),
    RadioChannel(8,  "إذاعة عبد العزيز سحيم",           "https://backup.qurango.net/radio/a_sheim"),
    RadioChannel(9,  "إذاعة فارس عباد",                 "https://backup.qurango.net/radio/fares_abbad"),
    RadioChannel(10, "إذاعة ماهر المعيقلي",             "https://backup.qurango.net/radio/maher"),
    RadioChannel(11, "إذاعة محمد صديق المنشاوي",         "https://backup.qurango.net/radio/mohammed_siddiq_alminshawi_mojawwad"),
    RadioChannel(12, "إذاعة محمود خليل الحصري",          "https://backup.qurango.net/radio/mahmoud_khalil_alhussary_mojawwad"),
    RadioChannel(13, "إذاعة محمود علي البنا",            "https://backup.qurango.net/radio/mahmoud_ali__albanna_mojawwad"),
    RadioChannel(14, "إذاعة علي الحذيفي",               "https://qurango.net/radio/ali_alhuthaifi"),
    RadioChannel(15, "إذاعة ناصر القطامي",              "https://backup.qurango.net/radio/nasser_alqatami"),
    RadioChannel(16, "إذاعة نبيل الرفاعي",              "https://backup.qurango.net/radio/nabil_al_rifay"),
    RadioChannel(17, "إذاعة هيثم الجدعاني",             "https://backup.qurango.net/radio/hitham_aljadani"),
    RadioChannel(18, "إذاعة ياسر الدوسري",              "https://backup.qurango.net/radio/yasser_aldosari"),
    RadioChannel(19, "إذاعة القرآن الكريم من القاهرة",   "https://stream.radiojar.com/8s5u5tpdtwzuv"),
    RadioChannel(20, "إذاعة السنة النبوية",             "https://stream.radiojar.com/x0vs2vzy6k0uv"),
    RadioChannel(21, "إذاعة تلاوات خاشعة",              "https://backup.qurango.net/radio/salma"),
    RadioChannel(22, "إذاعة الرقية الشرعية",            "https://backup.qurango.net/radio/roqiah"),
    RadioChannel(23, "إذاعة سعد الغامدي",               "https://backup.qurango.net/radio/saad_alghamdi"),
    RadioChannel(24, "المختصر في تفسير القرآن الكريم",   "https://backup.qurango.net/radio/mukhtasartafsir"),
)

@Composable
fun RadioScreen() {
    var playingId   by remember { mutableIntStateOf(-1) }
    var loadingId   by remember { mutableIntStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    fun stopPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBg, DarkBg2))),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp)
            ) {
                Text("📻 إذاعات القرآن الكريم", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gold)
                Text("Quran Radio", fontSize = 10.sp, color = TextSecondary, letterSpacing = 2.sp)
            }
        }

        items(CHANNELS, key = { it.id }) { channel ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .border(1.dp, GoldDim, RoundedCornerShape(12.dp))
                    .clickable {
                        if (playingId == channel.id) {
                            stopPlayer()
                            playingId = -1
                            loadingId = -1
                        } else {
                            stopPlayer()
                            loadingId = channel.id
                            playingId = -1
                            val mp = MediaPlayer()
                            @Suppress("DEPRECATION")
                            mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
                            try {
                                mp.setDataSource(channel.url)
                                mp.setOnPreparedListener {
                                    it.start()
                                    loadingId = -1
                                    playingId = channel.id
                                }
                                mp.setOnErrorListener { _, _, _ ->
                                    loadingId = -1
                                    playingId = -1
                                    true
                                }
                                mp.prepareAsync()
                                mediaPlayer = mp
                            } catch (e: Exception) {
                                loadingId = -1
                            }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = channel.name,
                    fontSize = 14.sp,
                    color = if (playingId == channel.id) Gold else TextPrimary,
                    fontWeight = if (playingId == channel.id) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                )
                when {
                    loadingId == channel.id -> CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Gold,
                        strokeWidth = 2.dp
                    )
                    playingId == channel.id -> Text("⏸", fontSize = 20.sp, color = Gold)
                    else -> Text("▶", fontSize = 18.sp, color = TextSecondary)
                }
            }
        }
    }
}

