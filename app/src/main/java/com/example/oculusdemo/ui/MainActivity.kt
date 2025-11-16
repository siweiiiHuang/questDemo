package com.example.oculusdemo.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oculusdemo.IPersistentCommService
import com.example.oculusdemo.ILogCallback
import com.example.oculusdemo.R
import com.example.oculusdemo.databinding.ActivityMainBinding
import com.example.oculusdemo.model.ChannelType
import com.example.oculusdemo.service.PersistentCommService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = LogAdapter()

    private var service: IPersistentCommService? = null
    private var bound = false

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    private val callback = object : ILogCallback.Stub() {
        override fun onLogChanged(logs: MutableList<String>?) {
            runOnUiThread {
                adapter.submitList(logs?.toList().orEmpty().reversed())
            }
        }

        override fun onChannelChanged(channel: String?) {
            val label = when (channel?.let { ChannelType.valueOf(it) }) {
                ChannelType.WIFI -> getString(R.string.connection_state_wifi)
                ChannelType.BLE -> getString(R.string.connection_state_ble)
                else -> getString(R.string.connection_state_idle)
            }
            runOnUiThread {
                binding.tvConnection.text = getString(R.string.service_status_format, label)
            }
        }

        override fun onPayload(payload: String?) {
            runOnUiThread {
                binding.tvPayload.text = payload?.takeIf { it.isNotEmpty() } ?: getString(R.string.placeholder_no_data)
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = IPersistentCommService.Stub.asInterface(binder)
            bound = true
            runCatching { service?.registerCallback(callback) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvConnection.text = getString(
            R.string.service_status_format,
            getString(R.string.connection_state_idle)
        )
        setupRecyclerView()
        setupButtons()
        requestNeededPermissions()
        startAndBindService()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            runCatching { service?.unregisterCallback(callback) }
            unbindService(connection)
        }
    }

    private fun setupRecyclerView() {
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnStart.setOnClickListener {
            startAndBindService()
            service?.startSession()
        }
        binding.btnStop.setOnClickListener {
            service?.stopSession()
        }
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().ifBlank { return@setOnClickListener }
            binding.etMessage.text?.clear()
            service?.sendMessage(message)
        }
    }

    private fun requestNeededPermissions() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (permissions.isNotEmpty()) {
            permissionsLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startAndBindService() {
        val intent = Intent(this, PersistentCommService::class.java)
        ContextCompat.startForegroundService(this, intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
}

