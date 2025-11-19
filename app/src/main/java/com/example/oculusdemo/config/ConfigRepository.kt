package com.example.oculusdemo.config

import android.content.Context
import androidx.core.content.edit

object ConfigRepository {

    private const val PREF_NAME = "persistent_comm_config"
    private const val KEY_WIFI_ENDPOINT = "wifi_endpoint"
    private const val KEY_WIFI_HEARTBEAT = "wifi_heartbeat_seconds"
    private const val KEY_WIFI_RECONNECT_ATTEMPTS = "wifi_reconnect_attempts"
    private const val KEY_WIFI_RECONNECT_INITIAL = "wifi_reconnect_initial_delay"
    private const val KEY_WIFI_RECONNECT_MAX = "wifi_reconnect_max_delay"
    private const val KEY_BLE_DEVICE = "ble_device"
    private const val KEY_BLE_SERVICE = "ble_service_uuid"
    private const val KEY_BLE_WRITE = "ble_write_uuid"
    private const val KEY_BLE_NOTIFY = "ble_notify_uuid"
    private const val KEY_WATCHDOG_ENABLED = "watchdog_enabled"
    private const val KEY_TELEMETRY_ENABLED = "telemetry_enabled"

    fun getConfig(context: Context): ConnectionConfig {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return ConnectionConfig(
            wifiEndpoint = prefs.getString(KEY_WIFI_ENDPOINT, ConnectionConfig.DEFAULT_WIFI_ENDPOINT)
                ?: ConnectionConfig.DEFAULT_WIFI_ENDPOINT,
            wifiHeartbeatSeconds = prefs.getInt(KEY_WIFI_HEARTBEAT, 30),
            wifiReconnectMaxAttempts = prefs.getInt(KEY_WIFI_RECONNECT_ATTEMPTS, 5),
            wifiReconnectInitialDelayMs = prefs.getLong(KEY_WIFI_RECONNECT_INITIAL, 1000),
            wifiReconnectMaxDelayMs = prefs.getLong(KEY_WIFI_RECONNECT_MAX, 15000),
            bleDeviceName = prefs.getString(KEY_BLE_DEVICE, ConnectionConfig.DEFAULT_BLE_DEVICE_NAME)
                ?: ConnectionConfig.DEFAULT_BLE_DEVICE_NAME,
            bleServiceUuid = prefs.getString(KEY_BLE_SERVICE, ConnectionConfig.DEFAULT_BLE_SERVICE_UUID)
                ?: ConnectionConfig.DEFAULT_BLE_SERVICE_UUID,
            bleWriteCharacteristicUuid = prefs.getString(KEY_BLE_WRITE, ConnectionConfig.DEFAULT_BLE_WRITE_UUID)
                ?: ConnectionConfig.DEFAULT_BLE_WRITE_UUID,
            bleNotifyCharacteristicUuid = prefs.getString(KEY_BLE_NOTIFY, ConnectionConfig.DEFAULT_BLE_NOTIFY_UUID)
                ?: ConnectionConfig.DEFAULT_BLE_NOTIFY_UUID,
            watchdogEnabled = prefs.getBoolean(KEY_WATCHDOG_ENABLED, true),
            telemetryEnabled = prefs.getBoolean(KEY_TELEMETRY_ENABLED, true)
        )
    }

    fun updateConfig(context: Context, config: ConnectionConfig) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_WIFI_ENDPOINT, config.wifiEndpoint)
            putInt(KEY_WIFI_HEARTBEAT, config.wifiHeartbeatSeconds)
            putInt(KEY_WIFI_RECONNECT_ATTEMPTS, config.wifiReconnectMaxAttempts)
            putLong(KEY_WIFI_RECONNECT_INITIAL, config.wifiReconnectInitialDelayMs)
            putLong(KEY_WIFI_RECONNECT_MAX, config.wifiReconnectMaxDelayMs)
            putString(KEY_BLE_DEVICE, config.bleDeviceName)
            putString(KEY_BLE_SERVICE, config.bleServiceUuid)
            putString(KEY_BLE_WRITE, config.bleWriteCharacteristicUuid)
            putString(KEY_BLE_NOTIFY, config.bleNotifyCharacteristicUuid)
            putBoolean(KEY_WATCHDOG_ENABLED, config.watchdogEnabled)
            putBoolean(KEY_TELEMETRY_ENABLED, config.telemetryEnabled)
        }
    }
}


