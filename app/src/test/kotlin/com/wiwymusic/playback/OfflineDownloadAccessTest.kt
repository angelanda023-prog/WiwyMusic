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
}
