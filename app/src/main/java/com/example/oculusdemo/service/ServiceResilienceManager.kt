package com.example.oculusdemo.service

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.oculusdemo.config.ConfigRepository
import com.example.oculusdemo.model.ChannelType
import com.example.oculusdemo.telemetry.ServiceMetrics
import java.util.concurrent.TimeUnit

/**
 * Centralizes watchdog installation, heartbeat persistence and proactive restarts.
 */
object ServiceResilienceManager {

    private const val PERIODIC_WORK_NAME = "persistent_comm_watchdog_periodic"
    private const val ONE_SHOT_WORK_NAME = "persistent_comm_watchdog_immediate"
    private val HEARTBEAT_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5)

    fun installWatchdog(context: Context) {
        if (!guardEnabled(context)) return
        val appContext = context.applicationContext
        val workManager = WorkManager.getInstance(appContext)
        val periodicWork = PeriodicWorkRequestBuilder<ServiceHeartbeatWorker>(
            15, TimeUnit.MINUTES
        )
            .setInputData(ServiceHeartbeatWorker.heartbeatInput(HEARTBEAT_TIMEOUT_MS))
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }

    fun pokeWatchdog(context: Context) {
        if (!guardEnabled(context)) return
        val appContext = context.applicationContext
        val workManager = WorkManager.getInstance(appContext)
        val oneShot = OneTimeWorkRequestBuilder<ServiceHeartbeatWorker>()
            .setInputData(ServiceHeartbeatWorker.heartbeatInput(HEARTBEAT_TIMEOUT_MS))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.enqueueUniqueWork(
            ONE_SHOT_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneShot
        )
    }

    fun recordHeartbeat(context: Context, channel: ChannelType) {
        if (!guardEnabled(context)) return
        ServiceHealthStore.writeHeartbeat(context, channel)
    }

    fun scheduleServiceRestart(context: Context, reason: String) {
        if (!guardEnabled(context)) return
        ServiceHealthStore.writeResurrectReason(context, reason)
        ServiceMetrics.recordRestart(context, reason)
        val intent = PersistentCommService.reviveIntent(context, reason)
        ContextCompat.startForegroundService(context.applicationContext, intent)
    }

    fun uninstallWatchdog(context: Context) {
        val appContext = context.applicationContext
        val workManager = WorkManager.getInstance(appContext)
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(ONE_SHOT_WORK_NAME)
    }

    private fun guardEnabled(context: Context): Boolean =
        ConfigRepository.getConfig(context).watchdogEnabled
}



