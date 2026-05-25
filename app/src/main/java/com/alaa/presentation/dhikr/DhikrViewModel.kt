package com.alaa.presentation.dhikr

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import com.alaa.data.model.DhikrData
import com.alaa.data.model.DhikrItem
import com.alaa.data.prefs.PrefsManager
import com.alaa.service.DhikrService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

data class DhikrUiState(
    val dhikrList:     List<DhikrItem> = DhikrData.list,
    val selectedDhikr: DhikrItem       = DhikrData.list.first(),
    val isRunning:     Boolean         = false,
    val repeatSingle:  Boolean         = false,
    val intervalMin:   Int             = 10,
    val volume:        Float           = 1f,
    val autoEnabled:   Boolean         = false,
    val startHour:     Int             = 6,
    val startMinute:   Int             = 0,
    val stopHour:      Int             = 22,
    val stopMinute:    Int             = 0,
)

class DhikrViewModel(private val prefs: PrefsManager) : ViewModel() {

    private val _uiState = MutableStateFlow(DhikrUiState())
    val uiState: StateFlow<DhikrUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                selectedDhikr = DhikrData.list.getOrElse(prefs.selectedDhikrId) { DhikrData.list.first() },
                repeatSingle  = prefs.dhikrRepeatSingle,
                intervalMin   = prefs.dhikrInterval,
                volume        = prefs.dhikrVolume,
                autoEnabled   = prefs.dhikrAutoEnabled,
                startHour     = prefs.dhikrStartHour,
                startMinute   = prefs.dhikrStartMin,
                stopHour      = prefs.dhikrStopHour,
                stopMinute    = prefs.dhikrStopMin,
            )
        }
    }

    fun syncServiceState(context: Context) {
        _uiState.update { it.copy(isRunning = DhikrService.isRunning) }
    }

    fun selectDhikr(item: DhikrItem) {
        prefs.selectedDhikrId = item.id
        _uiState.update { it.copy(selectedDhikr = item) }
    }

    fun setRepeatSingle(repeat: Boolean) {
        prefs.dhikrRepeatSingle = repeat
        _uiState.update { it.copy(repeatSingle = repeat) }
    }

    fun setInterval(min: Int) {
        prefs.dhikrInterval = min
        _uiState.update { it.copy(intervalMin = min) }
    }

    fun setVolume(value: Float, context: Context) {
        prefs.dhikrVolume = value
        _uiState.update { it.copy(volume = value) }
        if (DhikrService.isRunning) {
            context.startService(Intent(context, DhikrService::class.java).apply {
                action = DhikrService.ACTION_UPDATE_VOLUME
                putExtra(DhikrService.EXTRA_VOLUME, value)
            })
        }
    }

    fun setAutoEnabled(enabled: Boolean, context: Context) {
        prefs.dhikrAutoEnabled = enabled
        _uiState.update { it.copy(autoEnabled = enabled) }
        if (enabled) scheduleAutoAlarm(context)
        else cancelAutoAlarm(context)
    }

    fun setStartTime(hour: Int, minute: Int, context: Context) {
        prefs.dhikrStartHour = hour
        prefs.dhikrStartMin  = minute
        _uiState.update { it.copy(startHour = hour, startMinute = minute) }
        if (_uiState.value.autoEnabled) scheduleAutoAlarm(context)
    }

    fun setStopTime(hour: Int, minute: Int, context: Context) {
        prefs.dhikrStopHour = hour
        prefs.dhikrStopMin  = minute
        _uiState.update { it.copy(stopHour = hour, stopMinute = minute) }
    }

    fun start(context: Context) {
        val s = _uiState.value
        val resIds = if (s.repeatSingle) intArrayOf(s.selectedDhikr.audioResId)
                     else s.dhikrList.map { it.audioResId }.toIntArray()
        val texts  = if (s.repeatSingle) arrayOf(s.selectedDhikr.textAr)
                     else s.dhikrList.map { it.textAr }.toTypedArray()

        val intent = Intent(context, DhikrService::class.java).apply {
            putExtra(DhikrService.EXTRA_RES_IDS,          resIds)
            putExtra(DhikrService.EXTRA_TEXTS,            texts)
            putExtra(DhikrService.EXTRA_INTERVAL_MINUTES, s.intervalMin)
            putExtra(DhikrService.EXTRA_VOLUME,           s.volume)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent)
        else
            context.startService(intent)
        _uiState.update { it.copy(isRunning = true) }
    }

    fun stop(context: Context) {
        context.startService(Intent(context, DhikrService::class.java).apply {
            action = DhikrService.ACTION_STOP
        })
        _uiState.update { it.copy(isRunning = false) }
    }

    private fun scheduleAutoAlarm(context: Context) {
        val s   = _uiState.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, s.startHour)
            set(Calendar.MINUTE,      s.startMinute)
            set(Calendar.SECOND,      0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getService(
            context, 9999,
            Intent(context, DhikrService::class.java).apply {
                val resIds = if (s.repeatSingle) intArrayOf(s.selectedDhikr.audioResId)
                             else s.dhikrList.map { it.audioResId }.toIntArray()
                putExtra(DhikrService.EXTRA_RES_IDS, resIds)
                putExtra(DhikrService.EXTRA_TEXTS, s.dhikrList.map { it.textAr }.toTypedArray())
                putExtra(DhikrService.EXTRA_INTERVAL_MINUTES, s.intervalMin)
                putExtra(DhikrService.EXTRA_VOLUME, s.volume)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        } catch (_: Exception) {}
    }

    private fun cancelAutoAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getService(
            context, 9999,
            Intent(context, DhikrService::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { am.cancel(it) }
    }
}
