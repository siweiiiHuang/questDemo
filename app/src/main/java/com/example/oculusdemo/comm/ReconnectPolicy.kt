package com.example.oculusdemo.comm

import kotlin.math.min
import kotlin.math.pow

data class ReconnectPolicy(
    val maxAttempts: Int = 5,
    val initialDelayMs: Long = 1_000,
    val maxDelayMs: Long = 15_000,
    val multiplier: Double = 2.0
) {

    fun shouldRetry(attempt: Int): Boolean = attempt < maxAttempts

    fun nextDelayMs(attempt: Int): Long {
        if (attempt <= 0) return initialDelayMs
        val delay = initialDelayMs * multiplier.pow(attempt.toDouble())
        return min(delay.toLong(), maxDelayMs)
    }
}


