package com.wiwymusic.playback

import com.wiwymusic.constants.AudioQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioQualityNetworkPolicyTest {
    @Test
    fun highestDowngradesToHighOutsideWifiWhenGuardEnabled() {
        assertEquals(
            AudioQuality.HIGH,
            resolvePlaybackAudioQuality(
                selectedQuality = AudioQuality.HIGHEST,
                maximumQualityWifiOnly = true,
                isWifi = false,
            ),
        )
    }

    @Test
    fun highestRemainsHighestOnWifi() {
        assertEquals(
            AudioQuality.HIGHEST,
            resolvePlaybackAudioQuality(
                selectedQuality = AudioQuality.HIGHEST,
                maximumQualityWifiOnly = true,
                isWifi = true,
            ),
        )
    }

    @Test
    fun automaticQualityRemainsAutomatic() {
        assertEquals(
            AudioQuality.AUTO,
            resolvePlaybackAudioQuality(
                selectedQuality = AudioQuality.AUTO,
                maximumQualityWifiOnly = true,
                isWifi = false,
            ),
        )
    }
}
