package com.example.oculusdemo.service

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.oculusdemo.config.ConfigRepository
import com.example.oculusdemo.logging.LogRepository
import com.example.oculusdemo.telemetry.ServiceMetrics
import java.util.concurrent.TimeUnit

/**
 * Periodically verifies that the heartbeat emitted by [PersistentCommService] is still fresh.
 * If the heartbeat is stale (e.g. process killed under memory pressure or by "ghost process"
 * governance), the worker will attempt to restart the service in foreground mode.
 */
class ServiceHeartbeatWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val config = ConfigRepository.getConfig(applicationContext)
        if (!config.watchdogEnabled) {
            LogRepository.appendLog(TAG, "守护已关闭，跳过心跳检查")
            return Result.success()
        }
        val timeoutMs = inputData.getLong(KEY_TIMEOUT_MS, DEFAULT_TIMEOUT_MS)
        val lastBeat = ServiceHealthStore.lastHeartbeat(applicationContext)
        val elapsed = System.currentTimeMillis() - lastBeat
        if (lastBeat == 0L || elapsed > timeoutMs) {
            LogRepository.appendLog(TAG, "Heartbeat stale (${elapsed}ms). Restarting foreground service.")
            ServiceMetrics.recordHeartbeatStale(applicationContext)
            ServiceHealthStore.writeResurrectReason(applicationContext, "watchdog_timeout")
            val intent = PersistentCommService.reviveIntent(applicationContext, "watchdog_timeout")
            ContextCompat.startForegroundService(applicationContext, intent)
        } else {
            LogRepository.appendLog(TAG, "Heartbeat ok (${elapsed}ms).")
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "ServiceHeartbeatWorker"
        private const val KEY_TIMEOUT_MS = "timeout_ms"
        private val DEFAULT_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5)

        fun heartbeatInput(timeoutMs: Long = DEFAULT_TIMEOUT_MS) = workDataOf(
            KEY_TIMEOUT_MS to timeoutMs
        )
    }
}



