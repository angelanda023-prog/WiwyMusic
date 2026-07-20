/*
 * WiwyMusic — Actualización OTA in-app.
 * Descarga el APK dentro de la app y lanza el instalador de Android
 * (sin abrir el navegador).
 */

package com.wiwymusic.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

object AppUpdateInstaller {

    private const val APK_MIME = "application/vnd.android.package-archive"
    private const val FILE_NAME = "WiwyMusic.apk"

    @Volatile
    private var currentDownloadId: Long = -1L

    /**
     * Descarga el APK [url] con el DownloadManager del sistema y, al completar,
     * abre el instalador de paquetes de Android para actualizar la app.
     */
    fun downloadAndInstall(context: Context, url: String) {
        val appContext = context.applicationContext
        val downloadManager = appContext.getSystemService<DownloadManager>() ?: run {
            Toast.makeText(appContext, "No se pudo iniciar la descarga", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpia un archivo previo para no acumular versiones.
        runCatching {
            appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?.resolve(FILE_NAME)
                ?.delete()
        }

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("WiwyMusic")
            .setDescription("Descargando actualización…")
            .setMimeType(APK_MIME)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, FILE_NAME)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (id != currentDownloadId) return
                runCatching { appContext.unregisterReceiver(this) }
                launchInstaller(appContext, downloadManager, id)
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(receiver, filter)
        }

        currentDownloadId = downloadManager.enqueue(request)
        Toast.makeText(appContext, "Descargando actualización…", Toast.LENGTH_SHORT).show()
    }

    private fun launchInstaller(context: Context, downloadManager: DownloadManager, id: Long) {
        // Verifica que la descarga terminó bien.
        val status = DownloadManager.Query().setFilterById(id).let { query ->
            downloadManager.query(query)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                } else null
            }
        }
        if (status != DownloadManager.STATUS_SUCCESSFUL) {
            Toast.makeText(context, "La descarga de la actualización falló", Toast.LENGTH_LONG).show()
            return
        }

        val apkUri: Uri? = downloadManager.getUriForDownloadedFile(id)
        if (apkUri == null) {
            Toast.makeText(context, "No se encontró el archivo descargado", Toast.LENGTH_LONG).show()
            return
        }

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        runCatching {
            ContextCompat.startActivity(context, installIntent, null)
        }.onFailure {
            Toast.makeText(context, "No se pudo abrir el instalador", Toast.LENGTH_LONG).show()
        }
    }
}
