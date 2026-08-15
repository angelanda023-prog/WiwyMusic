package com.wiwymusic.utils

import java.net.URI
import java.net.URLDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumLaunchPromoTest {
    @Test
    fun freeAccountSeesPromoOnFirstEligibleForeground() {
        assertTrue(shouldShowPremiumLaunchPromo(true, false, 2, 1, 0, 1_000))
        assertFalse(shouldShowPremiumLaunchPromo(true, false, 2, 2, 0, 1_000))
    }

    @Test
    fun promoWaitsSixHoursAndRequiresANewForeground() {
        val shownAt = 1_000L
        assertFalse(shouldShowPremiumLaunchPromo(true, false, 3, 2, shownAt, shownAt + PREMIUM_PROMO_COOLDOWN_MILLIS - 1))
        assertTrue(shouldShowPremiumLaunchPromo(true, false, 3, 2, shownAt, shownAt + PREMIUM_PROMO_COOLDOWN_MILLIS))
        assertFalse(shouldShowPremiumLaunchPromo(true, false, 2, 2, shownAt, shownAt + PREMIUM_PROMO_COOLDOWN_MILLIS))
    }

    @Test
    fun clockRollbackDoesNotBypassCooldown() {
        assertFalse(shouldShowPremiumLaunchPromo(true, false, 3, 2, 10_000, 9_000))
    }

    @Test
    fun premiumUnknownAndLoggedOutAccountsDoNotSeePromo() {
        assertFalse(shouldShowPremiumLaunchPromo(true, true, 2, 1, 0, 1_000))
        assertFalse(shouldShowPremiumLaunchPromo(true, null, 2, 1, 0, 1_000))
        assertFalse(shouldShowPremiumLaunchPromo(false, false, 2, 1, 0, 1_000))
    }

    @Test
    fun whatsappLinkContainsCorrectMexicanNumberAndMessage() {
        val uri = URI(PremiumContact.WHATSAPP_URL)
        assertEquals("wa.me", uri.host)
        assertEquals("/528136890880", uri.path)
        assertEquals("/${PremiumContact.PHONE_NUMBER}", uri.path)
        val message = uri.rawQuery.substringAfter("text=")
        assertEquals(PremiumContact.MESSAGE, URLDecoder.decode(message, Charsets.UTF_8.name()))
    }
}
