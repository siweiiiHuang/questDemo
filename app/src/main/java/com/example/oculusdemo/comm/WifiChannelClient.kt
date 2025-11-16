package com.example.oculusdemo.comm

import android.net.Uri
import com.example.oculusdemo.logging.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class WifiChannelClient(
    private val logTag: String = "WifiChannel"
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var client: WebSocketClient? = null
    private val connecting = AtomicBoolean(false)

    private val _incoming = MutableSharedFlow<String>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incoming: SharedFlow<String> = _incoming

    fun connect(uri: Uri, onConnected: () -> Unit, onClosed: () -> Unit, onError: (Throwable) -> Unit) {
        if (connecting.getAndSet(true)) return
        scope.launch {
            runCatching {
                val wsClient = object : WebSocketClient(URI(uri.toString())) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        LogRepository.appendLog(logTag, "WebSocket 已连接 ${uri}")
                        onConnected()
                        connecting.set(false)
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
                        connecting.set(false)
                        onClosed()
                    }

                    override fun onError(ex: Exception?) {
                        try {
                            LogRepository.appendLog(logTag, "WebSocket 错误: ${ex?.message ?: "未知错误"}")
                            ex?.let { 
                                try {
                                    onError(it)
                                } catch (e: Exception) {
                                    LogRepository.appendLog(logTag, "onError 回调异常: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            LogRepository.appendLog(logTag, "onError 处理异常: ${e.message}")
                        }
                    }
                }
                // 使用阻塞连接，但添加异常处理
                try {
                    val connected = wsClient.connectBlocking()
                    if (connected) {
                        // 检查连接状态，添加异常处理
                        try {
                            if (wsClient.isOpen) {
                                client = wsClient
                            } else {
                                wsClient.close()
                                throw IllegalStateException("连接未建立")
                            }
                        } catch (e: Exception) {
                            try {
                                wsClient.close()
                            } catch (closeEx: Exception) {
                                // 忽略关闭时的异常
                            }
                            throw IllegalStateException("连接状态检查失败: ${e.message}")
                        }
                    } else {
                        wsClient.close()
                        throw IllegalStateException("连接未建立")
                    }
                } catch (e: Exception) {
                    try {
                        // 尝试关闭连接，忽略异常
                        wsClient.close()
                    } catch (closeEx: Exception) {
                        // 忽略关闭时的异常
                    }
                    throw e
                }
            }.onFailure {
                LogRepository.appendLog(logTag, "连接失败: ${it.message}")
                onError(it)
                connecting.set(false)
            }
        }
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
        scope.launch {
            runCatching {
                client?.close()
                client = null
                connecting.set(false)
            }
        }
    }
}

