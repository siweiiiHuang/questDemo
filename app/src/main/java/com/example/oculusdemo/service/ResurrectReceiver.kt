package com.example.oculusdemo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.oculusdemo.config.ConfigRepository
import com.example.oculusdemo.logging.LogRepository

/**
 * Handles system events (boot completed, package replaced, user unlock) to make sure the watchdog
 * and the foreground service are re-armed after the process has been evicted.
 */
class ResurrectReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        LogRepository.appendLog(TAG, "ResurrectReceiver received action=$action")
        if (!ConfigRepository.getConfig(context).watchdogEnabled) {
            LogRepository.appendLog(TAG, "守护已关闭，跳过自动拉起")
            return
        }
        ServiceResilienceManager.installWatchdog(context)
        ServiceResilienceManager.scheduleServiceRestart(context, action)
    }

    companion object {
        private const val TAG = "ResurrectReceiver"
    }
}



