/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.wiwymusic.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import com.wiwymusic.BuildConfig
import com.wiwymusic.MainActivity
import com.wiwymusic.R
import com.wiwymusic.constants.LastNotifiedVersionKey

/**
 * WiwyMusic: el aviso de "nueva versión disponible" se decide en un solo
 * lugar — [OtaRealtimeSync] (Supabase Realtime) — para no duplicar
 * notificaciones. Este objeto solo se encarga de mostrarla/cancelarla.
 * El antiguo chequeo periódico basado en GitHub (WorkManager, cada 6h)
 * se quitó porque generaba una segunda notificación independiente.
 */
object UpdateNotificationManager {
    private const val CHANNEL_ID = "update_notification_channel"
    private const val NOTIFICATION_ID = 9999

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.update_notification_channel_name)
            val descriptionText = context.getString(R.string.update_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    suspend fun notifyIfNewVersion(context: Context, latestVersion: String) {
        try {
            val dataStore = context.dataStore
            val lastNotified = dataStore.data.map { it[LastNotifiedVersionKey] ?: "" }.first()

            if (latestVersion != lastNotified && Updater.isNewerVersion(latestVersion, BuildConfig.VERSION_NAME)) {
                showUpdateNotification(context, latestVersion)
                dataStore.edit { it[LastNotifiedVersionKey] = latestVersion }
            }
        } catch (e: Exception) {
            Timber.w(e, "UpdateNotificationManager: notifyIfNewVersion failed")
        }
    }

    private fun showUpdateNotification(context: Context, newVersion: String) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "settings/update")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // WiwyMusic: la actualización se descarga e instala DENTRO de la app,
        // así que el botón abre la pantalla de actualización (no el navegador).
        val downloadIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "settings/update")
            putExtra("start_update_download", true)
        }
        val downloadPendingIntent = PendingIntent.getActivity(
            context,
            1,
            downloadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.opentune)
            .setContentTitle(context.getString(R.string.update_notification_title))
            .setContentText(context.getString(R.string.update_notification_text, newVersion))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.download,
                context.getString(R.string.download),
                downloadPendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }

    fun cancelUpdateNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
