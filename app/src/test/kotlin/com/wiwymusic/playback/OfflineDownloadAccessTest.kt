package com.wiwymusic.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineDownloadAccessTest {
    @Test
    fun premiumCanUseOfflineDownloads() {
        assertTrue(canUseOfflineDownloads(true))
    }

    @Test
    fun freeCannotUseOfflineDownloads() {
        assertFalse(canUseOfflineDownloads(false))
    }

    @Test
    fun unknownPlanFailsClosed() {
        assertFalse(canUseOfflineDownloads(null))
    }

    @Test
    fun freeNeedsValidatedInternetForRemoteMusic() {
        assertFalse(canPlayRemoteMusic(isPremium = false, hasValidatedInternet = false))
        assertTrue(canPlayRemoteMusic(isPremium = false, hasValidatedInternet = true))
    }

    @Test
    fun premiumCanPlayRemoteMusicOffline() {
        assertTrue(canPlayRemoteMusic(isPremium = true, hasValidatedInternet = false))
    }

    @Test
    fun unknownPlanCannotUseCachedRemoteMusicOffline() {
        assertFalse(canPlayRemoteMusic(isPremium = null, hasValidatedInternet = false))
    }
}
