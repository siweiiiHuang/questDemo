package com.example.oculusdemo.logging

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

object LogRepository {
    private const val MAX_IN_MEMORY = 20
    private const val LOG_FILE_NAME = "persistent_comm.log"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    private lateinit var logFile: File
    private val initialized = AtomicBoolean(false)
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun initialize(context: Context) {
        if (initialized.getAndSet(true)) return
        logFile = File(context.filesDir, LOG_FILE_NAME)
        if (!logFile.exists()) {
            logFile.createNewFile()
        } else {
            val cached = logFile.readLines().takeLast(MAX_IN_MEMORY)
            _logs.value = cached
        }
    }

    fun appendLog(tag: String, message: String) {
        if (!initialized.get()) return
        val time = formatter.format(Date())
        val entry = "[$time][$tag] $message"
        scope.launch {
            logFile.appendText("$entry\n")
            val updated = (_logs.value + entry).takeLast(MAX_IN_MEMORY)
            _logs.value = updated
        }
    }

    fun snapshot(): List<String> = _logs.value

    fun logFilePath(): String = logFile.absolutePath
}


