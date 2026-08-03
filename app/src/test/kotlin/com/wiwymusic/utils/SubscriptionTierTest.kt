package com.wiwymusic.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionTierTest {
    @Test
    fun fromWire_forcesFreeWhenAccessIsDisabled() {
        assertEquals(
            SubscriptionTier.FREE,
            SubscriptionTier.fromWire("premium_plus", isPremium = false),
        )
    }

    @Test
    fun fromWire_preservesPremiumPlusLifetimeTier() {
        assertEquals(
            SubscriptionTier.PREMIUM_PLUS,
            SubscriptionTier.fromWire("premium_plus", isPremium = true),
        )
    }

    @Test
    fun fromWire_fallsBackToPremiumForOlderProfiles() {
        assertEquals(
            SubscriptionTier.PREMIUM,
            SubscriptionTier.fromWire(null, isPremium = true),
        )
    }

    @Test
    fun premiumAndPlusShareAccessGate() {
        assertFalse(SubscriptionTier.FREE.hasPremiumAccess)
        assertTrue(SubscriptionTier.PREMIUM.hasPremiumAccess)
        assertTrue(SubscriptionTier.PREMIUM_PLUS.hasPremiumAccess)
    }
}
