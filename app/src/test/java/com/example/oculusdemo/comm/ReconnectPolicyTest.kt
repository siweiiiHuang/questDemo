package com.example.oculusdemo.comm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconnectPolicyTest {

    @Test
    fun `should respect max attempts`() {
        val policy = ReconnectPolicy(maxAttempts = 3)
        assertTrue(policy.shouldRetry(0))
        assertTrue(policy.shouldRetry(2))
        assertFalse(policy.shouldRetry(3))
    }

    @Test
    fun `next delay grows exponentially but capped`() {
        val policy = ReconnectPolicy(
            maxAttempts = 5,
            initialDelayMs = 1000,
            maxDelayMs = 5000,
            multiplier = 2.0
        )
        assertEquals(1000, policy.nextDelayMs(0))
        assertEquals(2000, policy.nextDelayMs(1))
        assertEquals(4000, policy.nextDelayMs(2))
        assertEquals(5000, policy.nextDelayMs(3))
    }
}


