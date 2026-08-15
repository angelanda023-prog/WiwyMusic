package com.wiwymusic.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal fun shouldShowPremiumLaunchPromo(
    hasSession: Boolean,
    isPremium: Boolean?,
    foregroundGeneration: Long,
    claimedGeneration: Long,
): Boolean = hasSession &&
    isPremium == false &&
    foregroundGeneration > 0 &&
    foregroundGeneration != claimedGeneration

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

    fun claimIfFree(hasSession: Boolean, isPremium: Boolean?): Boolean {
        val currentGeneration = _foregroundGeneration.value
        if (!hasSession || isPremium == null || currentGeneration <= 0 || currentGeneration == claimedGeneration) {
            return false
        }

        val shouldShow = shouldShowPremiumLaunchPromo(
            hasSession = hasSession,
            isPremium = isPremium,
            foregroundGeneration = currentGeneration,
            claimedGeneration = claimedGeneration,
        )
        claimedGeneration = currentGeneration
        return shouldShow
    }
}
