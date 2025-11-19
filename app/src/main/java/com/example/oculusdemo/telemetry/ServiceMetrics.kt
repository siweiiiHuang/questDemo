package com.example.oculusdemo.telemetry

import android.content.Context
import androidx.core.content.edit
import com.example.oculusdemo.config.ConfigRepository

object ServiceMetrics {

    private const val PREF_NAME = "persistent_comm_metrics"
    private const val KEY_RESTART_TOTAL = "metrics_restart_total"
    private const val KEY_HEARTBEAT_STALE = "metrics_heartbeat_stale"
    private const val KEY_RECONNECT_ATTEMPTS = "metrics_reconnect_attempts"

    fun recordRestart(context: Context, reason: String) {
        if (!ConfigRepository.getConfig(context).telemetryEnabled) return
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(KEY_RESTART_TOTAL, prefs.getLong(KEY_RESTART_TOTAL, 0L) + 1)
            putLong(metricKeyForReason(reason), prefs.getLong(metricKeyForReason(reason), 0L) + 1)
        }
    }

    fun recordHeartbeatStale(context: Context) {
        if (!ConfigRepository.getConfig(context).telemetryEnabled) return
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(KEY_HEARTBEAT_STALE, prefs.getLong(KEY_HEARTBEAT_STALE, 0L) + 1)
        }
    }

    fun recordReconnectAttempt(context: Context) {
        if (!ConfigRepository.getConfig(context).telemetryEnabled) return
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(KEY_RECONNECT_ATTEMPTS, prefs.getLong(KEY_RECONNECT_ATTEMPTS, 0L) + 1)
        }
    }

    fun snapshot(context: Context): Map<String, Long> {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.all.filterValues { it is Long }.mapValues { it.value as Long }
    }

    private fun metricKeyForReason(reason: String): String = "metrics_restart_reason_$reason"
}


