package com.alaa.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alaa.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ── 1. إعادة جدولة الصلوات عند البوت أو تغيير الوقت ──────
                if (action == Intent.ACTION_BOOT_COMPLETED
                    || action == Intent.ACTION_TIME_CHANGED
                    || action == Intent.ACTION_TIMEZONE_CHANGED
                ) {
                    val alarmIntent = Intent(context, PrayerAlarmService::class.java)
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(alarmIntent)
          } else {
             context.startService(alarmIntent)
              }
                }

                // ── 2. فقط عند البوت — إرجاع الخدمات وفتح التطبيق ─────────
                if (action == Intent.ACTION_BOOT_COMPLETED) {

                    val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)

                    // إرجاع PrayerAlarmService لو كان شغال
                    val alarmWasRunning = prefs.getBoolean("alarm_service_running", false)
                    if (alarmWasRunning) {
                        val alarmIntent = Intent(context, PrayerAlarmService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(alarmIntent)
                        } else {
                            context.startService(alarmIntent)
                        }
                    }

                    // ── 3. فتح التطبيق من حيث وقف ──────────────────────────
                    val lastScreen = prefs.getString("last_screen", null)

                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        lastScreen?.let { putExtra("start_screen", it) }
                    }
                    context.startActivity(launchIntent)
                }

            } finally {
                pendingResult.finish()
            }
        }
    }
}

