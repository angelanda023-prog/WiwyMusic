package com.wiwymusic.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Session-only storage. This file is excluded from Android backup and device transfer so an
 * uninstall cannot restore a previously authenticated Supabase session.
 */
val Context.authSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_session",
)

internal fun shouldMigrateLegacySession(
    firstInstallTime: Long,
    lastUpdateTime: Long,
): Boolean = lastUpdateTime > firstInstallTime

internal fun Context.shouldMigrateLegacySession(): Boolean =
    runCatching {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        shouldMigrateLegacySession(
            firstInstallTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime,
        )
    }.getOrDefault(false)
