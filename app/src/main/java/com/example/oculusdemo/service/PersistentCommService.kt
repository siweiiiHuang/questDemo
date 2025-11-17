package com.example.oculusdemo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.RemoteCallbackList
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.oculusdemo.IPersistentCommService
import com.example.oculusdemo.ILogCallback
import com.example.oculusdemo.R
import com.example.oculusdemo.comm.BleChannelManager
import com.example.oculusdemo.comm.WifiChannelClient
import com.example.oculusdemo.logging.LogRepository
import com.example.oculusdemo.model.ChannelType
import com.example.oculusdemo.model.ServiceState
import com.example.oculusdemo.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class PersistentCommService : LifecycleService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(ServiceState())
    val state: StateFlow<ServiceState> = _state

    private val callbacks = RemoteCallbackList<ILogCallback>()
    private val networkCallbackRegistered = AtomicBoolean(false)

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var bleManager: BleChannelManager
    private val wifiClient = WifiChannelClient()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                LogRepository.appendLog(TAG, "检测到 Wi-Fi 网络，可尝试连接 WebSocket")
                tryConnectWifi()
            }
        }

        override fun onLost(network: Network) {
            LogRepository.appendLog(TAG, "Wi-Fi 网络丢失，回退到 BLE")
            fallbackToBle()
        }
    }

    // Quest 真机使用：将 192.168.31.226 替换为你的 PC/手机 IP（示例）
    // 模拟器使用：10.0.2.2
    private var wifiEndpoint: Uri = Uri.parse("ws://192.168.31.226:8080")  // 当前 PC IP
    // private var wifiEndpoint: Uri = Uri.parse("ws://10.0.2.2:8080")  // 模拟器地址
    private val bleConfig = BleChannelManager.Config(
        deviceName = "QuestPeripheral",
        serviceUuid = UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb"),
        writeCharacteristicUuid = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb"),
        notifyCharacteristicUuid = UUID.fromString("0000beee-0000-1000-8000-00805f9b34fb")
    )

    private val binder = object : IPersistentCommService.Stub() {
        override fun startSession() {
            startCommunication()
        }

        override fun stopSession() {
            stopCommunication()
        }

        override fun sendMessage(message: String) {
            when (_state.value.activeChannel) {
                ChannelType.WIFI -> wifiClient.send(message)
                ChannelType.BLE -> bleManager.send(message)
                ChannelType.IDLE -> LogRepository.appendLog(TAG, "发送失败：当前未连接")
            }
        }

        override fun registerCallback(callback: ILogCallback) {
            callbacks.register(callback)
            val snapshot = _state.value
            callback.onChannelChanged(snapshot.activeChannel.name)
            callback.onPayload(snapshot.lastPayload ?: "")
            callback.onLogChanged(ArrayList(LogRepository.snapshot()))
        }

        override fun unregisterCallback(callback: ILogCallback) {
            callbacks.unregister(callback)
        }
    }

    override fun onCreate() {
        super.onCreate()
        LogRepository.appendLog(TAG, "Service onCreate")
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        bleManager = BleChannelManager(this)
        // 设置 BLE 连接成功回调
        bleManager.setOnConnectedCallback {
            updateState { current -> current.copy(activeChannel = ChannelType.BLE) }
            dispatchChannel(ChannelType.BLE)
            updateForeground("已连接 BLE 通道")
            LogRepository.appendLog(TAG, "BLE 通道已就绪")
        }
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("初始化中"))

        lifecycleScope.launch {
            LogRepository.logs.collectLatest {
                updateState { current -> current.copy(logs = it) }
                dispatchLogs(it)
            }
        }
        serviceScope.launch {
            wifiClient.incoming.collect {
                LogRepository.appendLog(TAG, "Wi-Fi 收到：$it")
                updateState { current -> current.copy(lastPayload = it) }
                dispatchPayload(it)
            }
        }
        serviceScope.launch {
            bleManager.incoming.collect {
                LogRepository.appendLog(TAG, "BLE 收到：$it")
                updateState { current -> current.copy(lastPayload = it) }
                dispatchPayload(it)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCommunication()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCommunication()
        callbacks.kill()
        LogRepository.appendLog(TAG, "Service onDestroy")
    }

    private fun startCommunication() {
        if (networkCallbackRegistered.compareAndSet(false, true)) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
        tryConnectWifi()
        LogRepository.appendLog(TAG, "开始通信会话")
        updateForeground("通信会话已启动")
    }

    private fun stopCommunication() {
        if (networkCallbackRegistered.compareAndSet(true, false)) {
            runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        }
        wifiClient.close()
        bleManager.close()
        updateState { ServiceState(ChannelType.IDLE, null, LogRepository.snapshot()) }
        dispatchChannel(ChannelType.IDLE)
        updateForeground("通信会话已停止")
        LogRepository.appendLog(TAG, "通信会话已停止")
    }

    private fun tryConnectWifi() {
        if (_state.value.activeChannel == ChannelType.WIFI) {
            LogRepository.appendLog(TAG, "已在 Wi-Fi 通道，无需重新连接")
            return
        }
        wifiClient.connect(
            uri = wifiEndpoint,
            onConnected = {
                bleManager.close()
                updateState { current -> current.copy(activeChannel = ChannelType.WIFI) }
                dispatchChannel(ChannelType.WIFI)
                updateForeground("已连接 Wi-Fi 通道")
            },
            onClosed = {
                fallbackToBle()
            },
            onError = {
                LogRepository.appendLog(TAG, "Wi-Fi 连接异常: ${it.message}")
                fallbackToBle()
            }
        )
    }

    private fun fallbackToBle() {
        wifiClient.close()
        // 不立即设置状态为 BLE，等待连接成功后再设置
        updateForeground("正在扫描 BLE 设备...")
        LogRepository.appendLog(TAG, "回退至 BLE，开始扫描设备")
        bleManager.startScan(bleConfig)
    }

    private fun updateForeground(content: String) {
        val notification = buildNotification(content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_service)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Persistent Comm",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Oculus 后台通信服务"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateState(transform: (ServiceState) -> ServiceState) {
        val newState = transform(_state.value)
        _state.value = newState
    }

    private fun dispatchLogs(logs: List<String>) {
        val size = callbacks.beginBroadcast()
        repeat(size) {
            try {
                callbacks.getBroadcastItem(it).onLogChanged(ArrayList(logs))
            } catch (_: Exception) {
            }
        }
        callbacks.finishBroadcast()
    }

    private fun dispatchChannel(channel: ChannelType) {
        val size = callbacks.beginBroadcast()
        val value = channel.name
        repeat(size) {
            try {
                callbacks.getBroadcastItem(it).onChannelChanged(value)
            } catch (_: Exception) {
            }
        }
        callbacks.finishBroadcast()
    }

    private fun dispatchPayload(payload: String) {
        val size = callbacks.beginBroadcast()
        repeat(size) {
            try {
                callbacks.getBroadcastItem(it).onPayload(payload)
            } catch (_: Exception) {
            }
        }
        callbacks.finishBroadcast()
    }

    companion object {
        private const val TAG = "PersistentCommService"
        private const val CHANNEL_ID = "persistent_comm_channel"
        private const val NOTIFICATION_ID = 1001
    }
}

