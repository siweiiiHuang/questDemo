package com.example.oculusdemo.config

data class ConnectionConfig(
    val wifiEndpoint: String = DEFAULT_WIFI_ENDPOINT,
    val wifiHeartbeatSeconds: Int = 30,
    val wifiReconnectMaxAttempts: Int = 5,
    val wifiReconnectInitialDelayMs: Long = 1000,
    val wifiReconnectMaxDelayMs: Long = 15000,
    val bleDeviceName: String = DEFAULT_BLE_DEVICE_NAME,
    val bleServiceUuid: String = DEFAULT_BLE_SERVICE_UUID,
    val bleWriteCharacteristicUuid: String = DEFAULT_BLE_WRITE_UUID,
    val bleNotifyCharacteristicUuid: String = DEFAULT_BLE_NOTIFY_UUID,
    val watchdogEnabled: Boolean = true,
    val telemetryEnabled: Boolean = true
) {
    companion object {
        const val DEFAULT_WIFI_ENDPOINT = "ws://192.168.1.100:8080"
        const val DEFAULT_BLE_DEVICE_NAME = "QuestPeripheral"
        const val DEFAULT_BLE_SERVICE_UUID = "0000feed-0000-1000-8000-00805f9b34fb"
        const val DEFAULT_BLE_WRITE_UUID = "0000beef-0000-1000-8000-00805f9b34fb"
        const val DEFAULT_BLE_NOTIFY_UUID = "0000beee-0000-1000-8000-00805f9b34fb"
    }
}


