package com.example.oculusdemo

import android.app.Application
import com.example.oculusdemo.logging.LogRepository

class OculusDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LogRepository.initialize(applicationContext)
    }
}


