package com.wiwymusic.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal const val PREMIUM_PROMO_COOLDOWN_MILLIS = 6L * 60L * 60L * 1000L

internal fun shouldShowPremiumLaunchPromo(
    hasSession: Boolean,
    isPremium: Boolean?,
    foregroundGeneration: Long,
    claimedGeneration: Long,
    lastShownAtMillis: Long,
    nowMillis: Long,
): Boolean = hasSession &&
    isPremium == false &&
    foregroundGeneration > 0 &&
    foregroundGeneration != claimedGeneration &&
    (lastShownAtMillis <= 0L ||
        (nowMillis >= lastShownAtMillis &&
            nowMillis - lastShownAtMillis >= PREMIUM_PROMO_COOLDOWN_MILLIS))

object PremiumLaunchPromo : DefaultLifecycleObserver {
    private val _foregroundGeneration = MutableStateFlow(0L)
    val foregroundGeneration = _foregroundGeneration.asStateFlow()

    private var initialized = false
    private var claimedGeneration = 0L

    fun initialize() {
        if (initialized) return
        initialized = true

        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        _foregroundGeneration.update { it + 1 }
    }

    fun claimIfEligible(
        foregroundGeneration: Long,
        hasSession: Boolean,
        isPremium: Boolean?,
        lastShownAtMillis: Long,
        nowMillis: Long,
    ): Boolean {
        if (!hasSession || isPremium == null || foregroundGeneration <= 0 ||
            foregroundGeneration != _foregroundGeneration.value ||
            foregroundGeneration == claimedGeneration
        ) {
            return false
        }

        val shouldShow = shouldShowPremiumLaunchPromo(
            hasSession = hasSession,
            isPremium = isPremium,
            foregroundGeneration = foregroundGeneration,
            claimedGeneration = claimedGeneration,
            lastShownAtMillis = lastShownAtMillis,
            nowMillis = nowMillis,
        )
        claimedGeneration = foregroundGeneration
        return shouldShow
    }
}
