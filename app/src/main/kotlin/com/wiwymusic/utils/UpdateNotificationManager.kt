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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import com.wiwymusic.BuildConfig
import com.wiwymusic.MainActivity
import com.wiwymusic.R
import com.wiwymusic.constants.EnableUpdateNotificationKey
import com.wiwymusic.constants.LastNotifiedVersionKey
import com.wiwymusic.constants.LastUpdateCheckKey
import com.wiwymusic.constants.UpdateChannel
import com.wiwymusic.constants.UpdateChannelKey
import java.util.concurrent.TimeUnit

object UpdateNotificationManager : DefaultLifecycleObserver {
    private const val CHANNEL_ID = "update_notification_channel"
    private const val NOTIFICATION_ID = 9999
    private const val WORK_NAME = "update_check_work"
    private const val CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L
    private const val FOREGROUND_CHECK_INTERVAL_MS = 180 * 60 * 1000L
    private const val OPEN_CHECK_DEDUP_MS = 10 * 1000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val checkMutex = Mutex()
    private var applicationContext: Context? = null
    private var foregroundJob: Job? = null
    private var foregroundChecksInitialized = false

    @Synchronized
    fun initializeForegroundChecks(context: Context) {
        if (foregroundChecksInitialized) return
        foregroundChecksInitialized = true
        applicationContext = context.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        val context = applicationContext ?: return
        foregroundJob?.cancel()
        foregroundJob = scope.launch {
            checkMutex.withLock { performUpdateCheck(context, forceOpenCheck = true) }
            while (isActive) {
                delay(FOREGROUND_CHECK_INTERVAL_MS)
                checkMutex.withLock { performUpdateCheck(context, forceOpenCheck = true) }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        foregroundJob?.cancel()
        foregroundJob = null
    }

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

    fun schedulePeriodicUpdateCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val updateCheckRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            6, TimeUnit.HOURS,
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateCheckRequest
        )
    }

    fun cancelPeriodicUpdateCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun checkForUpdates(context: Context) {
        scope.launch {
            try {
                checkMutex.withLock { performUpdateCheck(context, forceOpenCheck = false) }
            } catch (e: Exception) {
                Timber.w(e, "UpdateNotificationManager: checkForUpdates failed")
            }
        }
    }

    private suspend fun performUpdateCheck(context: Context, forceOpenCheck: Boolean) {
        val dataStore = context.dataStore

        val isEnabled = dataStore.data.map { it[EnableUpdateNotificationKey] ?: false }.first()
        if (!isEnabled) {
            cancelPeriodicUpdateCheck(context)
            return
        }

        schedulePeriodicUpdateCheck(context)

        val updateChannel = dataStore.data.map {
            it[UpdateChannelKey]?.let { value ->
                try { UpdateChannel.valueOf(value) } catch (_: Exception) { UpdateChannel.STABLE }
            } ?: UpdateChannel.STABLE
        }.first()

        if (updateChannel == UpdateChannel.NIGHTLY) return

        val lastCheck = dataStore.data.map { it[LastUpdateCheckKey] ?: 0L }.first()
        val now = System.currentTimeMillis()
        val minimumInterval = if (forceOpenCheck) OPEN_CHECK_DEDUP_MS else CHECK_INTERVAL_MS

        if (now - lastCheck < minimumInterval) return

        dataStore.edit { it[LastUpdateCheckKey] = now }

        Updater.getLatestVersionName()
            .onSuccess { latestVersion ->
                if (Updater.isNewerVersion(latestVersion, BuildConfig.VERSION_NAME)) {
                    notifyIfNewVersion(context, latestVersion)
                }
            }
            .onFailure { e ->
                Timber.w(e, "UpdateNotificationManager: update check failed")
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
