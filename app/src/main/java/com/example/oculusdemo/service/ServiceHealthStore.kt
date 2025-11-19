package com.example.oculusdemo.service

import android.content.Context
import androidx.core.content.edit
import com.example.oculusdemo.model.ChannelType

/**
 * Persists lightweight health signals so watchdog components can decide whether to revive the
 * foreground service without having to keep the process in memory.
 */
internal object ServiceHealthStore {

    private const val PREF_NAME = "persistent_comm_health"
    private const val KEY_HEARTBEAT = "last_heartbeat"
    private const val KEY_CHANNEL = "last_channel"
    private const val KEY_LAST_REASON = "last_resurrect_reason"
    private const val DEFAULT_REASON = "none"

    fun writeHeartbeat(context: Context, channel: ChannelType) {
        val sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        sharedPreferences.edit {
            putLong(KEY_HEARTBEAT, now)
            putString(KEY_CHANNEL, channel.name)
        }
    }

    fun lastHeartbeat(context: Context): Long {
        val sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(KEY_HEARTBEAT, 0L)
    }

    fun lastChannel(context: Context): ChannelType {
        val sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val value = sharedPreferences.getString(KEY_CHANNEL, ChannelType.IDLE.name).orEmpty()
        return runCatching { ChannelType.valueOf(value) }.getOrDefault(ChannelType.IDLE)
    }

    fun writeResurrectReason(context: Context, reason: String) {
        val sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString(KEY_LAST_REASON, reason)
        }
    }

    fun lastResurrectReason(context: Context): String {
        val sharedPreferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_LAST_REASON, DEFAULT_REASON) ?: DEFAULT_REASON
    }
}



