package com.example.oculusdemo.service

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.oculusdemo.logging.LogRepository
import java.io.File

class LogExportService : IntentService("LogExportService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent?.action != ACTION_EXPORT) return
        val targetDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "logs")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val targetFile = File(targetDir, "persistent_comm_export.txt")
        targetFile.writeText(LogRepository.snapshot().joinToString("\n"))
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            targetFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(Intent.createChooser(shareIntent, "导出日志"))
    }

    companion object {
        const val ACTION_EXPORT = "com.example.oculusdemo.action.EXPORT_LOG"
    }
}


