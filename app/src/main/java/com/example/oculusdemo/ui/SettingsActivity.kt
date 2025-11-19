package com.example.oculusdemo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.oculusdemo.R
import com.example.oculusdemo.config.ConfigRepository
import com.example.oculusdemo.config.ConnectionConfig
import com.example.oculusdemo.databinding.ActivitySettingsBinding
import com.example.oculusdemo.service.PersistentCommService
import java.util.UUID

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val config = ConfigRepository.getConfig(this)
        bindConfig(config)
        binding.btnSaveConfig.setOnClickListener {
            saveConfig()
        }
    }

    private fun bindConfig(config: ConnectionConfig) {
        binding.etWifiEndpoint.setText(config.wifiEndpoint)
        binding.etHeartbeat.setText(config.wifiHeartbeatSeconds.toString())
        binding.etReconnectAttempts.setText(config.wifiReconnectMaxAttempts.toString())
        binding.etReconnectInitial.setText(config.wifiReconnectInitialDelayMs.toString())
        binding.etReconnectMax.setText(config.wifiReconnectMaxDelayMs.toString())
        binding.etBleName.setText(config.bleDeviceName)
        binding.etBleService.setText(config.bleServiceUuid)
        binding.etBleWrite.setText(config.bleWriteCharacteristicUuid)
        binding.etBleNotify.setText(config.bleNotifyCharacteristicUuid)
        binding.switchWatchdog.isChecked = config.watchdogEnabled
        binding.switchTelemetry.isChecked = config.telemetryEnabled
    }

    private fun saveConfig() {
        val newConfig = collectConfig() ?: return
        ConfigRepository.updateConfig(this, newConfig)
        Toast.makeText(this, getString(R.string.settings_saved_success), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PersistentCommService::class.java).apply {
            action = PersistentCommService.ACTION_APPLY_CONFIG
        }
        ContextCompat.startForegroundService(this, intent)
        finish()
    }

    private fun collectConfig(): ConnectionConfig? {
        val wifiEndpoint = binding.etWifiEndpoint.text?.toString().orEmpty().ifBlank {
            ConnectionConfig.DEFAULT_WIFI_ENDPOINT
        }
        val heartbeatSeconds = binding.etHeartbeat.text?.toString()?.toIntOrNull()
            ?: return showNumberError()
        val reconnectAttempts = binding.etReconnectAttempts.text?.toString()?.toIntOrNull()
            ?: return showNumberError()
        val reconnectInitial = binding.etReconnectInitial.text?.toString()?.toLongOrNull()
            ?: return showNumberError()
        val reconnectMax = binding.etReconnectMax.text?.toString()?.toLongOrNull()
            ?: return showNumberError()
        val bleService = binding.etBleService.text?.toString().orEmpty()
        val bleWrite = binding.etBleWrite.text?.toString().orEmpty()
        val bleNotify = binding.etBleNotify.text?.toString().orEmpty()
        if (!validateUuid(bleService) || !validateUuid(bleWrite) || !validateUuid(bleNotify)) {
            Toast.makeText(this, getString(R.string.settings_invalid_uuid), Toast.LENGTH_SHORT).show()
            return null
        }
        return ConnectionConfig(
            wifiEndpoint = wifiEndpoint,
            wifiHeartbeatSeconds = heartbeatSeconds,
            wifiReconnectMaxAttempts = reconnectAttempts,
            wifiReconnectInitialDelayMs = reconnectInitial,
            wifiReconnectMaxDelayMs = reconnectMax,
            bleDeviceName = binding.etBleName.text?.toString().orEmpty(),
            bleServiceUuid = bleService,
            bleWriteCharacteristicUuid = bleWrite,
            bleNotifyCharacteristicUuid = bleNotify,
            watchdogEnabled = binding.switchWatchdog.isChecked,
            telemetryEnabled = binding.switchTelemetry.isChecked
        )
    }

    private fun showNumberError(): ConnectionConfig? {
        Toast.makeText(this, getString(R.string.settings_invalid_number), Toast.LENGTH_SHORT).show()
        return null
    }

    private fun validateUuid(value: String): Boolean {
        return runCatching { UUID.fromString(value) }.isSuccess
    }
}


