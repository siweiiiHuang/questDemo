package com.example.oculusdemo.comm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.example.oculusdemo.logging.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("MissingPermission")
class BleChannelManager(
    context: Context,
    private val logTag: String = "BleChannel"
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val appContext = context.applicationContext
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private var gatt: BluetoothGatt? = null
    private var currentScanCallback: ScanCallback? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private val _incoming = MutableSharedFlow<String>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incoming: SharedFlow<String> = _incoming

    private val scanning = AtomicBoolean(false)
    private val connected = AtomicBoolean(false)

    data class Config(
        val deviceName: String,
        val serviceUuid: UUID,
        val writeCharacteristicUuid: UUID,
        val notifyCharacteristicUuid: UUID
    )
    
    private var onConnectedCallback: (() -> Unit)? = null
    
    fun setOnConnectedCallback(callback: () -> Unit) {
        onConnectedCallback = callback
    }

    fun startScan(config: Config) {
        if (scanning.getAndSet(true)) return
        // 只使用 Service UUID 过滤，不要求设备名完全匹配（更宽松）
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(config.serviceUuid))
                .build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                val device = result?.device ?: return
                val deviceName = device.name ?: "未知设备"
                LogRepository.appendLog(logTag, "扫描到设备: $deviceName (${device.address})")
                // 如果指定了设备名，检查是否匹配（不区分大小写）
                if (config.deviceName.isNotEmpty() && 
                    !deviceName.equals(config.deviceName, ignoreCase = true)) {
                    LogRepository.appendLog(logTag, "设备名不匹配，跳过: $deviceName != ${config.deviceName}")
                    return
                }
                stopScan()
                connect(device, config)
            }

            override fun onScanFailed(errorCode: Int) {
                LogRepository.appendLog(logTag, "扫描失败: $errorCode")
                scanning.set(false)
            }
        }
        currentScanCallback = callback
        scanner?.startScan(filters, settings, callback)
        LogRepository.appendLog(logTag, "开始扫描 BLE 设备")
    }

    fun stopScan() {
        if (!scanning.get()) return
        currentScanCallback?.let { scanner?.stopScan(it) }
        currentScanCallback = null
        scanning.set(false)
        LogRepository.appendLog(logTag, "停止扫描")
    }

    private fun connect(device: BluetoothDevice, config: Config) {
        LogRepository.appendLog(logTag, "尝试连接 ${device.address}")
        gatt = device.connectGatt(appContext, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        connected.set(true)
                        LogRepository.appendLog(logTag, "BLE 已连接, 开始发现服务")
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        connected.set(false)
                        LogRepository.appendLog(logTag, "BLE 已断开: $status")
                        cleanup()
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    LogRepository.appendLog(logTag, "服务发现失败: $status")
                    return
                }
                val service: BluetoothGattService? = gatt.getService(config.serviceUuid)
                val write = service?.getCharacteristic(config.writeCharacteristicUuid)
                val notify = service?.getCharacteristic(config.notifyCharacteristicUuid)
                if (service == null || write == null || notify == null) {
                    LogRepository.appendLog(logTag, "未找到所需的特征")
                    return
                }
                writeCharacteristic = write
                gatt.setCharacteristicNotification(notify, true)
                LogRepository.appendLog(logTag, "BLE 服务初始化完成")
                // 通知连接成功
                onConnectedCallback?.invoke()
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                val data = characteristic.getStringValue(0) ?: return
                LogRepository.appendLog(logTag, "收到 BLE 数据: $data")
                scope.launch { _incoming.emit(data) }
            }
        })
    }

    fun send(message: String) {
        scope.launch {
            runCatching {
                val characteristic = writeCharacteristic ?: return@runCatching
                characteristic.value = message.toByteArray()
                gatt?.writeCharacteristic(characteristic)
            }.onFailure {
                LogRepository.appendLog(logTag, "BLE 发送失败: ${it.message}")
            }.onSuccess {
                LogRepository.appendLog(logTag, "BLE 已发送: $message")
            }
        }
    }

    fun close() {
        stopScan()
        cleanup()
    }

    private fun cleanup() {
        scope.launch {
            runCatching {
                gatt?.close()
                gatt = null
                writeCharacteristic = null
                connected.set(false)
            }
        }
    }

    fun isConnected(): Boolean = connected.get()
}

