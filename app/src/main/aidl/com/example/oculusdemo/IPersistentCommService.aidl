package com.example.oculusdemo;

import com.example.oculusdemo.ILogCallback;

interface IPersistentCommService {
    void startSession();
    void stopSession();
    void sendMessage(String message);
    void registerCallback(ILogCallback callback);
    void unregisterCallback(ILogCallback callback);
}


