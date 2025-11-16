package com.example.oculusdemo.model

data class ServiceState(
    val activeChannel: ChannelType = ChannelType.IDLE,
    val lastPayload: String? = null,
    val logs: List<String> = emptyList()
)


