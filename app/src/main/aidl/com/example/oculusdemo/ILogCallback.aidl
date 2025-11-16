package com.example.oculusdemo;

import java.util.List;

interface ILogCallback {
    void onLogChanged(in List<String> logs);
    void onChannelChanged(String channel);
    void onPayload(String payload);
}


