package com.example.oculusdemo.comm

import android.net.Uri
import com.example.oculusdemo.logging.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WifiChannelClient(
    private val logTag: String = "WifiChannel"
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var client: WebSocketClient? = null
    private val connecting = AtomicBoolean(false)
    private val manualClose = AtomicBoolean(false)
    private val reconnectAttempts = AtomicInteger(0)
    private var options: Options? = null
    private var callbacks: Callbacks? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null

    private val _incoming = MutableSharedFlow<String>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incoming: SharedFlow<String> = _incoming

    fun connect(
        options: Options,
        callbacks: Callbacks
    ) {
        if (connecting.getAndSet(true)) return
        this.options = options
        this.callbacks = callbacks
        manualClose.set(false)
        reconnectAttempts.set(0)
        connectInternal()
    }

    fun send(message: String) {
        scope.launch {
            runCatching {
                client?.takeIf { it.isOpen }?.send(message)
            }.onFailure {
                LogRepository.appendLog(logTag, "发送失败: ${it.message}")
            }.onSuccess {
                LogRepository.appendLog(logTag, "已发送: $message")
            }
        }
    }

    fun close() {
        manualClose.set(true)
        stopHeartbeat()
        reconnectJob?.cancel()
        reconnectJob = null
        scope.launch {
            runCatching {
                client?.close()
            }.also {
                client = null
                connecting.set(false)
                manualClose.set(false)
            }
        }
    }

    private fun connectInternal() {
        val currentOptions = options ?: return
        val uri = currentOptions.uri
        scope.launch {
            runCatching {
                val wsClient = object : WebSocketClient(URI(uri.toString())) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        LogRepository.appendLog(logTag, "WebSocket 已连接 $uri")
                        connecting.set(false)
                        reconnectAttempts.set(0)
                        client = this
                        callbacks?.onConnected?.invoke()
                        startHeartbeat()
                    }

                    override fun onMessage(message: String?) {
                        message?.let {
                            LogRepository.appendLog(logTag, "收到消息: $it")
                            scope.launch { _incoming.emit(it) }
                        }
                    }

                    override fun onMessage(bytes: ByteBuffer?) {
                        bytes?.let {
                            val text = String(it.array())
                            LogRepository.appendLog(logTag, "收到二进制消息: $text")
                            scope.launch { _incoming.emit(text) }
                        }
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        LogRepository.appendLog(logTag, "WebSocket 关闭: $code/$reason remote=$remote")
                        handleDisconnect()
                    }

                    override fun onError(ex: Exception?) {
                        LogRepository.appendLog(logTag, "WebSocket 错误: ${ex?.message ?: "未知错误"}")
                        ex?.let { callbacks?.onError?.invoke(it) }
                        handleDisconnect()
                    }
                }
                val connected = wsClient.connectBlocking()
                if (!connected || !wsClient.isOpen) {
                    wsClient.close()
                    throw IllegalStateException("连接未建立")
                }
            }.onFailure {
                LogRepository.appendLog(logTag, "连接失败: ${it.message}")
                connecting.set(false)
                callbacks?.onError?.invoke(it)
                handleDisconnect()
            }
        }
    }

    private fun handleDisconnect() {
        connecting.set(false)
        stopHeartbeat()
        client = null
        if (manualClose.get()) {
            return
        }
        scheduleReconnect()
    }

    private fun scheduleReconnect() {
        val currentOptions = options
        val currentCallbacks = callbacks
        if (currentOptions == null || currentCallbacks == null) return
        val attempt = reconnectAttempts.getAndIncrement()
        val policy = currentOptions.reconnectPolicy
        if (!policy.shouldRetry(attempt)) {
            LogRepository.appendLog(logTag, "达到最大重试次数，放弃重连")
            reconnectAttempts.set(0)
            currentCallbacks.onClosed.invoke()
            return
        }
        val delayMs = policy.nextDelayMs(attempt)
        LogRepository.appendLog(logTag, "计划 ${delayMs}ms 后进行第 ${attempt + 1} 次重连")
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delayMs)
            if (!isActive) return@launch
            connectInternal()
        }
    }

    private fun startHeartbeat() {
        val heartbeatInterval = options?.heartbeatIntervalMs ?: 0
        if (heartbeatInterval <= 0) return
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(heartbeatInterval)
                runCatching {
                    client?.takeIf { it.isOpen }?.send(options?.heartbeatPayload ?: "PING")
                }.onFailure {
                    LogRepository.appendLog(logTag, "心跳发送失败: ${it.message}")
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    data class Options(
        val uri: Uri,
        val heartbeatIntervalMs: Long,
        val heartbeatPayload: String = "PING",
        val reconnectPolicy: ReconnectPolicy = ReconnectPolicy()
    )

    data class Callbacks(
        val onConnected: () -> Unit,
        val onClosed: () -> Unit,
        val onError: (Throwable) -> Unit
    )
}

